package cordova_plugin_upshotplugin;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.os.Bundle;
import android.os.SystemClock;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import androidx.annotation.NonNull;
/**
 * This class echoes a string called from JavaScript.
 */
public class UpshotPlugin extends CordovaPlugin {


    private static final String TAG = UpshotPlugin.class.getName();
    private static CallbackContext tokenCallbackContext;
    private static CallbackContext pushCallbackContext;
    private static String pushPayload;
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

        if(action.equals("redirectionCallback")) {
            this.redirectionCallback(args.getString(0));
        }

        if(action.equals("shareCallback")) {
            this.shareCallback(args.getString(0));
        }
        return false;
    }

//Plugin Methods
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
      } else if (type == 4) {
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

    private void fetchTokenFromFirebaseSdk() {

        try {
          FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(new OnCompleteListener<String>() {
              @Override
              public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                  return;
                }

                String token = task.getResult();
                sendToken(token);
              }
            });
        } catch (Exception e) {

        }
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
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
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
          sendIntent.putExtra(Intent.EXTRA_TEXT,
            textToShare);
          sendIntent.setType("text/plain");
        }
    
        sendIntent.setAction(Intent.ACTION_SEND);
        context.startActivity(sendIntent);
      }
    
      public Uri getImageUri(Context inContext, String imageInBase64) {
        final String pureBase64Encoded = imageInBase64.substring(imageInBase64.indexOf(",")  + 1);

        final byte[] imgBytesData = android.util.Base64.decode(pureBase64Encoded,
          android.util.Base64.DEFAULT);
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
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) return;
          lastClickTime = SystemClock.elapsedRealtime();
          
        actionValue = actionValue.trim();
        Bundle bundle = new Bundle();
        bundle.putString("url", actionValue);
        (context).startActivity(new Intent(context, UpshotWebRedirection.class).putExtras(bundle));
      }

    

}

