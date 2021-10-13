package cordova_plugin_upshotplugin;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipInputStream;

import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import cordova_plugin_upshotplugin.animpush.UpshotEnhancedPushUtils;
import cordova_plugin_upshotplugin.animpush.UpshotGifNotification;


public class UpshotFirebaseMessagingService extends FirebaseMessagingService {


  private static final String TAG = UpshotFirebaseMessagingService.class.getSimpleName();
  private static String activityDirPath = "";

  private final String BIG_IMAGE = "big-image";
  private final String BANNER = "banner";
  public static final String ANIMATED = "animated-msg";
  public static final String ICON = "icon";

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {

    Bundle bundle = new Bundle();
    for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
      bundle.putString(entry.getKey(), entry.getValue());
    }

    Map<String, String> data = remoteMessage.getData();
    if (data != null) {
      sendNotification(remoteMessage.getData(), bundle);
    }
  }

  private Resources getApplicationResources() {
    Application app = getApplication();
    Resources resources = app.getResources();
    return resources;
  }

  private String getApplicationPackageName() {
    Application app = getApplication();
    String packageName = app.getPackageName();
    return packageName;
  }

  //This method is only generating push notification
  private void sendNotification(Map<String, String> messageBody, Bundle pushPayload) {

    try {
      String title = "";
      String text = "";
      if (pushPayload.containsKey("bk")) {
        title = pushPayload.containsKey("bk") ? pushPayload.getString("title") : "";
        text = pushPayload.containsKey("bk") ? pushPayload.getString("alert") : messageBody.toString();
      } else {
        try {
          title = pushPayload.getString("title");
          text = pushPayload.getString("body");
        } catch (Exception e) {
        }
      }

      if (pushPayload.containsKey("bundle_url") && pushPayload.containsKey("fileName")) {

        String fileName = pushPayload.getString("fileName");
        String url = pushPayload.getString("bundle_url");
        if (fileName.isEmpty()) {
          displayPushNotification(messageBody, title, text, null, null, null);
          return;
        }

        downloadEnhancedPushPackage(fileName, url, title, text, messageBody);
        return;
      }

      displayPushNotification(messageBody, title, text, null, null, null);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void displayPushNotification(Map<String, String> messageBody, String title, String text, Bitmap imageBitmap, String layoutFileType, String imageFilePath) {

    Class mainActivity = null;
    Context context = getApplicationContext();
    String packageName = context.getPackageName();
    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
    String className = launchIntent.getComponent().getClassName();
    try { //loading the Main Activity to not import it in the plugin
      mainActivity = Class.forName(className);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (mainActivity == null) {
      return;
    }

    int notificationId = UpshotEnhancedPushUtils.getIdFromTimestamp();
    JSONObject jsonObject = new JSONObject(messageBody);
    try {

      jsonObject.put("notificationId", notificationId);
      jsonObject.put("layoutType", layoutFileType);
    } catch (Exception e) {

    }

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      if (layoutFileType != null && layoutFileType.equals(ANIMATED)) {

        showGifNotification(notificationManager, imageFilePath, context, title, text, mainActivity, jsonObject, imageBitmap);
        return;
      } else if (layoutFileType != null && ((layoutFileType.equals(BIG_IMAGE)) || layoutFileType.equals(BANNER))) {
        showBigImageNotification(notificationManager, imageFilePath, context, title, text, mainActivity, jsonObject, imageBitmap);
        return;
      }

    Intent notifyIntent = new Intent(this, mainActivity);
    notifyIntent.putExtra("push", true);
    notifyIntent.putExtra("payload", jsonObject.toString());
// Create the PendingIntent
    PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    int applicationIcon = UpshotEnhancedPushUtils.getApplicationIcon(this);
    Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), applicationIcon);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "notifications")
      .setSmallIcon(applicationIcon)
      .setContentTitle(title)
      .setContentText(text)
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setContentIntent(pendingIntent)
      .setLargeIcon(iconBitmap);

    addChannelSupport(this, notificationBuilder);

    if (layoutFileType != null && layoutFileType.equals(ICON)) {
      notificationBuilder.setLargeIcon(imageBitmap);
      notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
    } else {
      notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
    }
    notificationManager.notify(notificationId, notificationBuilder.build());
  }

  private void showBigImageNotification(NotificationManager notificationManager, String imageFilePath, Context context, String title, String contentMsg, Class mainActivity, JSONObject jsonObject, Bitmap imageBitMap) {

    Resources resources = getApplicationResources();
    String packageName = getApplicationPackageName();
    int layout = resources.getIdentifier("upshot_big_image", "layout", packageName);
    int push_image_id = resources.getIdentifier("push_image", "id", packageName);

    RemoteViews remoteViews = new RemoteViews(packageName, layout);
    remoteViews.setImageViewBitmap(push_image_id, imageBitMap);

    int notificationId = UpshotEnhancedPushUtils.getIdFromTimestamp();

    Intent notifyIntent = new Intent(this, mainActivity);
    notifyIntent.putExtra("push", true);
    notifyIntent.putExtra("payload", jsonObject.toString());
// Create the PendingIntent
    PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    int color = 0x008000;
    int applicationIcon = UpshotEnhancedPushUtils.getApplicationIcon(this);
    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifications")
      .setSmallIcon(applicationIcon)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .setContentTitle(title)
      .setContentText(contentMsg)
      .setSound(defaultSoundUri)
      .setOngoing(false);

    builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(imageBitMap));
    builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder.setCategory(Notification.CATEGORY_MESSAGE);
    }
    addChannelSupport(this, builder);

    int push_title_text_Id = resources.getIdentifier("push_title_text", "id", packageName);
    int push_msg_text_id = resources.getIdentifier("push_msg_text", "id", packageName);

    remoteViews.setTextViewText(push_title_text_Id, title);
    remoteViews.setTextViewText(push_msg_text_id, contentMsg);

    Bitmap appIconBitmap = getApplicationIcon(context);
    if (appIconBitmap != null) {
      int app_icon_image_Id = resources.getIdentifier("app_icon_image", "id", packageName);
      remoteViews.setImageViewBitmap(app_icon_image_Id, appIconBitmap);
    }
    builder.setContent(remoteViews);
    builder.setCustomBigContentView(remoteViews);
    notificationManager.notify(notificationId, builder.build());
  }
  private void showGifNotification(NotificationManager notificationManager, String imageFilePath, Context context, String title, String contentMsg, Class mainActivity, JSONObject jsonObject, Bitmap imageBitMap) {

    UpshotGifNotification gifNotification = new UpshotGifNotification(notificationManager) {
      @Override
      protected RemoteViews onCreateRemoteViews() {

        Resources resources = getApplicationResources();
        String packageName = getApplicationPackageName();
        int layout = resources.getIdentifier("upshot_big_image", "layout", packageName);
        return new RemoteViews(getPackageName(), layout);
      }

      @Override
      protected Notification onCreateNotification(RemoteViews remoteViews, int notificationId) {

        Resources resources = getApplicationResources();
        String packageName = getApplicationPackageName();
        int push_title_text_Id = resources.getIdentifier("push_title_text", "id", packageName);
        int push_msg_text_id = resources.getIdentifier("push_msg_text", "id", packageName);

        remoteViews.setTextViewText(push_title_text_Id, title);
        remoteViews.setTextViewText(push_msg_text_id, contentMsg);
        if (title.isEmpty()) {
          remoteViews.setViewVisibility(push_title_text_Id, View.GONE);
        }

        Bitmap appIconBitmap = getApplicationIcon(context);
        if (appIconBitmap != null) {
          int app_icon_image_Id = resources.getIdentifier("app_icon_image", "id", packageName);
          remoteViews.setImageViewBitmap(app_icon_image_Id, appIconBitmap);
        }

        try {
          jsonObject.put("gifNotificationId", notificationId);
        } catch (JSONException e) {
        }

        Intent notifyIntent = new Intent(context, mainActivity);
        notifyIntent.putExtra("push", true);
        notifyIntent.putExtra("payload", jsonObject.toString());
// Create the PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
          context, notificationId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        int root_push_layout_Id = resources.getIdentifier("root_push_layout", "id", packageName);
        remoteViews.setOnClickPendingIntent(root_push_layout_Id, pendingIntent);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int applicationIcon = UpshotEnhancedPushUtils.getApplicationIcon(context);
        Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), applicationIcon);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "notifications")
          .setSmallIcon(applicationIcon)
          .setContentTitle(title)
          .setContentText(contentMsg)
          .setAutoCancel(true)
          .setSound(defaultSoundUri)
          .setContentIntent(pendingIntent)
          .setStyle(new NotificationCompat.DecoratedCustomViewStyle() {
          });

        addChannelSupport(context, notificationBuilder);

        notificationBuilder.setContent(remoteViews);
        notificationBuilder.setCustomBigContentView(remoteViews);
        notificationBuilder.setDeleteIntent(UpshotEnhancedPushUtils.getPendingIntent("gifDelete", notificationId, context));
        return notificationBuilder.build();
      }
    };
    try {
      Resources resources = getApplicationResources();
      String packageName = getApplicationPackageName();
      int push_image_id = resources.getIdentifier("push_image", "id", packageName);
      gifNotification.showNotification(imageFilePath, push_image_id);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public String getApplicationExternalDirPath(Context context) {

    String externalDirectory = context.getFilesDir().getAbsolutePath();
    String appName = applicationLabel(context);
    appName = removeHyphenSuffix(appName);
    String appDirPath = externalDirectory + File.separator + "." + appName;
    File appDir = new File(appDirPath);
    if (!appDir.exists() || !appDir.isDirectory()) {
      appDir.mkdirs();
    }
    return appDirPath;
  }

  public String removeHyphenSuffix(String fileName) {
    String filename = fileName;
    int hyphenIndex = filename.lastIndexOf('-');
    if (hyphenIndex != -1) {
      filename = filename.substring(0, hyphenIndex);
    }
    return filename;
  }

  private String applicationLabel(Context con) {
    ApplicationInfo info = con.getApplicationInfo();
    PackageManager p = con.getPackageManager();
    return p.getApplicationLabel(info).toString();
  }

  public void downloadEnhancedPushPackage(final String fileName, String url, String title, String contentMessage, Map<String, String> messageBody) {
    if (fileName.isEmpty() || url.isEmpty()) {
      displayPushNotification(messageBody, title, contentMessage, null, null, null);
    } else {
      String bkEnhancedPushDirPath = getApplicationExternalDirPath(getApplicationContext());
      File file = new File(bkEnhancedPushDirPath);
      if (!file.exists()) {
        boolean mkdirs = file.mkdirs();
        Log.v(TAG, "file directory status:::" + mkdirs);
      }
      activityDirPath = bkEnhancedPushDirPath + File.separator + fileName;
      Log.v(TAG, "activityDirpath:" + activityDirPath);
      String filePath = activityDirPath + ".zip";
      File activityDir = new File(activityDirPath);
      if (!activityDir.exists() || !activityDir.isDirectory()) {
        ZipDownloaderTask zipDownloaderTask = new ZipDownloaderTask(url, filePath, bkEnhancedPushDirPath, fileName, "json.txt") {
          @Override
          public void onZipExtracted() {
            Log.v(TAG, " zip extracted");
            showImageBasedNotification(fileName, title, contentMessage, messageBody);
          }

          @Override
          public void onZipFailed() {
          }
        };

        new Thread(zipDownloaderTask, fileName).start();
      } else {
        showImageBasedNotification(fileName, title, contentMessage, messageBody);
      }
    }
  }

  private void showImageBasedNotification(String fileName, String title, String contentMessage, Map<String, String> messageBody) {

    String txtFilePath = activityDirPath + File.separator + "json.txt";
    String imageName = "";
    JSONObject unitdata = null;
    JSONArray actionButtons = null;
    String layoutType = "";
    File file = new File(txtFilePath);
    Log.v(TAG, "notification data:::" + activityDirPath + ":" + txtFilePath + ":" + file.exists());
    if (file.exists()) {
      try {

        String jsonContent;
        try {
          jsonContent = cordova_plugin_upshotplugin.UpshotFileUtil.readString(new FileInputStream(file));
          JSONObject actData = cordova_plugin_upshotplugin.UpshotJsonUtil.isValidJsonObject(jsonContent);
          if (actData != null) {
            Log.v(TAG, "notification data:::" + actData);
            unitdata = actData.getJSONObject("Unit");
            if (unitdata != null) {
              Log.v(TAG, "notification unit data:::" + unitdata);
              layoutType = unitdata.getString("layoutType");
              imageName = unitdata.getString("imageName");
              actionButtons = unitdata.getJSONArray("actionButtons");
            }
          }
        } catch (FileNotFoundException | JSONException e) {
          Log.v(TAG, "Exception::", e);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        String imagePath = activityDirPath + File.separator + imageName;
        Bitmap bitmap = null;
        if (!layoutType.equals(ANIMATED)) {
          bitmap = BitmapFactory.decodeFile(imagePath, options);
        }
        displayPushNotification(messageBody, title, contentMessage, bitmap, layoutType, imagePath);
      } catch (Exception e) {
      }
    }
  }

  public void addChannelSupport(Context context, NotificationCompat.Builder notificationBuilder) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      return;
    }
    CharSequence name = "notifications";
    int importance = NotificationManager.IMPORTANCE_HIGH;
    NotificationChannel mChannel = new NotificationChannel("notifications", name, importance);

    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.createNotificationChannel(mChannel);
    notificationBuilder.setChannelId("notifications");
  }

  private static abstract class ZipDownloaderTask extends cordova_plugin_upshotplugin.UpshotDownloaderTask implements cordova_plugin_upshotplugin.UpshotDownloaderTask.DownloaderTaskListener {

    private final String appDir;
    private final String activityDirName;
    private final String txtFileName;


    public ZipDownloaderTask(String url, String filePath, String appDir, String activityDirName, String txtFileName) {
      super(url, filePath);
      this.appDir = appDir;
      this.activityDirName = activityDirName;
      this.txtFileName = txtFileName;
      setDownloaderTaskListener(this);
    }

    @Override
    public void onDownloadStarted(String url, boolean resumed) {
      Log.v(TAG, "onDownloadStarted");
    }

    @Override
    public void onDownloadProgress(String url, int downloaded, int contentLength) {
      Log.v(TAG, "onDownloadProgress: downloaded=" + downloaded + " contentLen=" + contentLength);
    }

    public abstract void onZipExtracted();

    public abstract void onZipFailed();


    @Override
    public void onDownloadCompleted(String url, String filePath) {
      Log.v(TAG, "onDownloadCompleted");
      //extract filePath
      try {
        cordova_plugin_upshotplugin.UpshotDecompress.unzip(new ZipInputStream(new FileInputStream(filePath)), appDir);
        // success, no exception
        new File(filePath).delete();

        onZipExtracted();

      } catch (IOException e) {
        e.printStackTrace();
      }

      Log.v(TAG, "ZipDownloaderTask completed");
    }

    @Override
    public void onError(Exception e) {
      Log.v(TAG, "onDownload onError =" + e.getLocalizedMessage());
      onZipFailed();
    }
  }

  public static Bitmap getApplicationIcon(Context context) {
    PackageManager p = context.getPackageManager();
    try {
      if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return AppIconHelperV26.getAppIcon(p, context.getPackageName());
      } else {
        Bitmap appIconBitmap = BitmapFactory.decodeResource(context.getResources(), p.getApplicationInfo(context.getPackageName(), 0).icon);
        return appIconBitmap;
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static class AppIconHelperV26 {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Bitmap getAppIcon(PackageManager mPackageManager, String packageName) {


      try {
        Drawable drawable = mPackageManager.getApplicationIcon(packageName);
        if (drawable instanceof BitmapDrawable) {
          Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
          return bitmap;
        } else if (drawable instanceof AdaptiveIconDrawable) {
          Drawable backgroundDr = ((AdaptiveIconDrawable) drawable).getBackground();
          Drawable foregroundDr = ((AdaptiveIconDrawable) drawable).getForeground();

          Drawable[] drr = new Drawable[2];
          drr[0] = backgroundDr;
          drr[1] = foregroundDr;

          LayerDrawable layerDrawable = new LayerDrawable(drr);

          int width = layerDrawable.getIntrinsicWidth();
          int height = layerDrawable.getIntrinsicHeight();

          Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

          Canvas canvas = new Canvas(bitmap);

          layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
          layerDrawable.draw(canvas);

          return bitmap;
        }
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      return null;
    }
  }
}
