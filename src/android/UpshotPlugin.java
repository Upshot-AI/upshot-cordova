package cordova_plugin_upshotplugin;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.upshot.push.lib.BrandKinesis;
import com.upshot.push.lib.callbacks.UpshotFBTokenCallback;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.StrictMode;

import androidx.core.app.ActivityCompat;

import java.util.Iterator;

/**
 * This class echoes a string called from JavaScript.
 */
public class UpshotPlugin extends CordovaPlugin {

  private static final String TAG = UpshotPlugin.class.getName();
  private static CallbackContext tokenCallbackContext;
  private static CallbackContext pushCallbackContext;
  private static CallbackContext carouselCallbackContext;
  private static String pushPayload;
  private static String carouselPushPayload;

  long lastClickTime = 0;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    if (action.equals("getDeviceToken")) {
      this.getDeviceToken(callbackContext);
      return true;
    }

    if (action.equals("getPushPayload")) {
      this.getPushPayload(callbackContext);
      return true;
    }

    if (action.equals("registerForPushNotifications")) {
      this.registerChannels();
      this.requestForNotificationPermissions();
    }

    if (action.equals("redirectionCallback")) {
      this.redirectionCallback(args.getString(0));
    }

    if (action.equals("shareCallback")) {
      this.shareCallback(args.getString(0));
    }

    if (action.equals("getCarouselDeeplink")) {
      getCarouselDeeplink(callbackContext);
      return true;
    }

    if (action.equals("getDefaultAccountAndUserDetails")) {
      this.getAccountDetails(args.getString(0));
    }

    if (action.equals("ratingStoreRedirectionCallback")) {
      this.ratingStoreRedirection(args.getString(0));
    }

    if (action.equals("getDeviceDetails")) {
      this.getDeviceInfoDetails(callbackContext);
    }

    return false;
  }

  // Plugin Methods

  private void getDeviceInfoDetails(CallbackContext callbackContext) {

    String osVersion = "" + android.os.Build.VERSION.SDK_INT;
    String deviceModel = "" + android.os.Build.MODEL;
    String manufacturer = "" + android.os.Build.MANUFACTURER;

    JSONObject deviceDetails = new JSONObject();
    try {
      deviceDetails.put("deviceName", deviceModel);
      deviceDetails.put("osVersion", osVersion);
      deviceDetails.put("manufacturer", manufacturer);

      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, deviceDetails.toString());
      pluginResult.setKeepCallback(true);
      callbackContext.sendPluginResult(pluginResult);
    } catch (JSONException e) {

    }
  }

  private void requestForNotificationPermissions() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ActivityCompat.checkSelfPermission(this.cordova.getActivity().getApplicationContext(),
          Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

        this.cordova.getActivity()
            .requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, 1);
      }
    }
  }

  private void getAccountDetails(String data) {
    try {
      JSONObject accountDetails = new JSONObject(data);
      Bundle jsonBundle = jsonToBundle(accountDetails);
      jsonBundle.putString("UpshotPlatform", "Hybrid_Android");
      Context context = this.cordova.getActivity().getApplicationContext();
      BrandKinesis.initialiseBrandKinesis(context, jsonBundle);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
    Bundle bundle = new Bundle();
    Iterator iter = jsonObject.keys();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      String value = jsonObject.getString(key);
      bundle.putString(key, value);
    }
    return bundle;
  }

  private void getDeviceToken(CallbackContext callbackContext) {
    tokenCallbackContext = callbackContext;
    fetchTokenFromFirebaseSdk();
  }

  private void getPushPayload(CallbackContext callbackContext) {
    pushCallbackContext = callbackContext;
    if (!TextUtils.isEmpty(pushPayload)) {
      sendPushPayload(pushPayload);
    }
  }

  private void redirectionCallback(String redirectionData) {
    try {
      JSONObject data = new JSONObject(redirectionData);
      int type = data.getInt("type");
      String deeplink = data.getString("deeplink");

      if (type == 1) {
        redirectionToStore(deeplink);
      } else if (type == 3) {
        redirectionToWeb(deeplink);
      } else if (type == 5) {
        redirectToCustomUri(deeplink);
      } else if (type == 4 || type == 2) {
        redirectToCall(deeplink);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void shareCallback(String shareData) {
    try {
      JSONObject data = new JSONObject(shareData);
      String text = data.optString("text");
      String imageData = data.optString("image");
      redirectionToShare(text, imageData);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void registerChannels() {

    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
    StrictMode.setVmPolicy(builder.build());
    createNotificationChannels();
  }

  private void getCarouselDeeplink(CallbackContext callbackContext) {
    carouselCallbackContext = callbackContext;
    if (!TextUtils.isEmpty(carouselPushPayload)) {
      sendCarouselPayload(carouselPushPayload);
    }
  }

  private void ratingStoreRedirection(String redirectionData) {
    try {
      JSONObject data = new JSONObject(redirectionData);
      int type = data.getInt("ratingType");
      String url = data.getString("url");
      if (type == 0) {
        Context context = this.cordova.getActivity().getApplicationContext();
        UpshotReviewManager reviewManager = new UpshotReviewManager(context);
        reviewManager.showRateApp(this.cordova.getActivity());
      } else {

        Context context = this.cordova.getActivity().getApplicationContext();
        final String storePrefix = "https://play.google.com/store/apps/details?id=";
        if (url.contains(storePrefix)) {
          String appPackageName = url.substring(storePrefix.length());

          try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
          } catch (android.content.ActivityNotFoundException anfe) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
          }
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  // Utility Methods

  @TargetApi(Build.VERSION_CODES.O)
  private void createNotificationChannels() {
    String notificationsChannelId = "notifications";
    Context context = this.cordova.getActivity().getApplicationContext();
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);

    List<NotificationChannel> channels = notificationManager.getNotificationChannels();
    NotificationChannel existingChanel = null;
    int count = 0;
    for (NotificationChannel channel : channels) {
      String fullId = channel.getId();
      if (fullId.contains(notificationsChannelId)) {
        existingChanel = channel;
        String[] numbers = extractRegexMatches(fullId, "\\d+");
        if (numbers.length > 0) {
          count = Integer.valueOf(numbers[0]);
        }
        break;
      }
    }
    if (existingChanel != null) {
      if (existingChanel.getImportance() < NotificationManager.IMPORTANCE_DEFAULT) {
        notificationManager.deleteNotificationChannel(existingChanel.getId());
      }
    }

    String newId = existingChanel == null ? notificationsChannelId + '_' + (count + 1) : existingChanel.getId();
    NotificationChannel channel = new NotificationChannel(
        newId, notificationsChannelId, NotificationManager.IMPORTANCE_HIGH);
    channel.setLightColor(Color.GREEN);
    channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
    notificationManager.createNotificationChannel(channel);
  }

  public String[] extractRegexMatches(String source, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(source);

    ArrayList<String> matches = new ArrayList<>();
    while (matcher.find()) {
      matches.add(matcher.group());
    }

    String[] result = new String[matches.size()];
    matches.toArray(result);
    return result;
  }

  public static void sendToken(String token) {

    if (tokenCallbackContext != null) {
      if (!TextUtils.isEmpty(token)) {
        try {
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, token);
          pluginResult.setKeepCallback(true);
          tokenCallbackContext.sendPluginResult(pluginResult);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        tokenCallbackContext.error("Expected one non-empty string argument.");
      }
    }
  }

  public static void sendPushPayload(String payload) {
    pushPayload = payload;
    if (pushCallbackContext != null) {
      pushPayload = null;
      if (!TextUtils.isEmpty(payload)) {
        try {
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, payload);
          pluginResult.setKeepCallback(true);
          pushCallbackContext.sendPluginResult(pluginResult);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void sendCarouselPayload(String payload) {

    carouselPushPayload = payload;
    if (carouselCallbackContext != null) {
      carouselPushPayload = null;
      if (!TextUtils.isEmpty(payload)) {
        try {
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, payload);
          pluginResult.setKeepCallback(true);
          carouselCallbackContext.sendPluginResult(pluginResult);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void fetchTokenFromFirebaseSdk() {
    BrandKinesis.getBKInstance().fetchTokenFromFirebaseSdk(new UpshotFBTokenCallback() {
      @Override
      public void onTokenReceivedSuccess(String s) {
        sendToken(s);
      }

      @Override
      public void onTokenReceivedFail() {

      }
    });
  }

  private void redirectionToStore(String url) {
    if (TextUtils.isEmpty(url)) {
      Log.i(TAG, "Invalid store redirection url, should not be empty");
      return;
    }
    url = url.trim();
    Context context = this.cordova.getActivity().getApplicationContext();

    final String storePrefix = "https://play.google.com/store/apps/details?id=";
    if (url.contains(storePrefix)) {
      Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      sendIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
      sendIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(sendIntent);
    } else {
      redirectToCustomUri(url);
    }
  }

  private void redirectionToShare(String textToShare, String encodedImage) {
    Context context = this.cordova.getActivity().getApplicationContext();
    Intent sendIntent = new Intent();

    if (!TextUtils.isEmpty(encodedImage)) {
      Uri bmpUri = getImageUri(context, encodedImage);
      sendIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
      sendIntent.setType("image/*");
    }

    if (!TextUtils.isEmpty(textToShare)) {
      sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
      sendIntent.setType("text/plain");
    }
    sendIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
    sendIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
    sendIntent.setAction(Intent.ACTION_SEND);
    context.startActivity(sendIntent);
  }

  public Uri getImageUri(Context inContext, String imageInBase64) {
    final String pureBase64Encoded = imageInBase64.substring(imageInBase64.indexOf(",") + 1);

    final byte[] imgBytesData = android.util.Base64.decode(pureBase64Encoded, android.util.Base64.DEFAULT);
    Bitmap bitmap = BitmapFactory.decodeByteArray(imgBytesData, 0, imgBytesData.length);

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

    File file = new File(inContext.getExternalCacheDir(), "iam_image.png");
    if (file.exists()) {
      file.delete();
    }
    try {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return Uri.fromFile(file);
  }

  private void redirectToCustomUri(String actionValue) {
    Context context = this.cordova.getActivity().getApplicationContext();

    try {
      if (TextUtils.isEmpty(actionValue)) {
        Log.i(TAG, "Invalid custom redirection value, should not be empty");
        return;
      } else {
        actionValue = actionValue.trim();
      }

      Intent redirectionIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(actionValue));
      redirectionIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
      redirectionIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
      if (redirectionIntent.resolveActivity(context.getPackageManager()) != null) {
        context.startActivity(redirectionIntent);
      }
    } catch (ActivityNotFoundException exp) {
      Log.i(TAG, "Exception:", exp);
    }
  }

  private static boolean isValidMobile(String phone) {
    return android.util.Patterns.PHONE.matcher(phone).matches();
  }

  private boolean redirectToCall(String contactNumber) {
    Context context = this.cordova.getActivity().getApplicationContext();
    if (context == null) {
      return false;
    }
    if (TextUtils.isEmpty(contactNumber)) {
      Log.i(TAG, "Invalid contact specified in redirection, should not be empty");
      return false;
    } else {
      contactNumber = contactNumber.trim();
      try {
        contactNumber = contactNumber.replaceAll("\\s", "");
      } catch (Exception e) {
      }
    }

    String telPrefix = "tel://";
    if (contactNumber.contains(telPrefix)) {
      contactNumber = contactNumber.substring(telPrefix.length());
    }
    if (!isValidMobile(contactNumber)) {
      // show log or toast.
      Log.i(TAG, "Invalid contact specified in redirection, unable to call:" + contactNumber);
      return false;
    }

    final boolean featureAvailable = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    if (featureAvailable) {
      try {
        String uri = "tel://" + contactNumber;
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
        dialIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        dialIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(dialIntent);
        return true;
      } catch (ActivityNotFoundException exp) {
        Log.i(TAG, "Activity not found to call:" + contactNumber, exp);
      }
    } else {
      // show log or toast.
      Log.i(TAG, "system feature telephony manager not available, unable to call:" + contactNumber);
    }
    return false;
  }

  private void redirectionToWeb(String actionValue) {
    Context context = this.cordova.getActivity().getApplicationContext();
    if (TextUtils.isEmpty(actionValue)) {
      return;
    }
    if (SystemClock.elapsedRealtime() - lastClickTime < 500)
      return;
    lastClickTime = SystemClock.elapsedRealtime();

    actionValue = actionValue.trim();
    Bundle bundle = new Bundle();
    bundle.putString("url", actionValue);
    Intent intent = new Intent(context, UpshotWebRedirection.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtras(bundle);
    (context).startActivity(intent);
  }
}
