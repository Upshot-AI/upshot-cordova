package cordova_plugin_upshotplugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.xcubelabs.upshot.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipInputStream;

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
        Log.i("test push", "onMessageReceived");


        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        Map<String, String> data = remoteMessage.getData();
        if (data != null) {
            sendNotification(remoteMessage.getData(), bundle);
        }
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
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

                downloadEnhancedPushPackage(fileName, url, title, text, messageBody);
                return;
            }

            displayPushNotification(messageBody, title, text, null, null, null);
        } catch (Exception e) {
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



        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (layoutFileType != null && layoutFileType.equals(ANIMATED)) {
            showGifNotification(notificationManager, imageFilePath, context, title, text, mainActivity, jsonObject);
            return;
        }


        Intent intent = new Intent(this, mainActivity);
        intent.putExtra("push", true);
        intent.putExtra("payload", jsonObject.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int applicationIcon = UpshotEnhancedPushUtils.getApplicationIcon(this);
        Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), applicationIcon);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
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
        } else if (imageBitmap != null) {
            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(imageBitmap));
        }
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void showGifNotification(NotificationManager notificationManager, String imageFilePath, Context context, String title, String contentMsg, Class mainActivity, JSONObject jsonObject) {

        UpshotGifNotification gifNotification = new UpshotGifNotification(notificationManager) {
            @Override
            protected RemoteViews onCreateRemoteViews() {
                return new RemoteViews(getPackageName(), R.layout.upshot_big_image);
            }

            @Override
            protected Notification onCreateNotification(RemoteViews remoteViews, int notificationId) {

                remoteViews.setTextViewText(R.id.push_title_text, title);
                remoteViews.setTextViewText(R.id.push_msg_text, contentMsg);

                try {
                    jsonObject.put("gifNotificationId" , notificationId);
                } catch (JSONException e) {
                }

                Intent intent = new Intent(context, mainActivity);
                intent.putExtra("push", true);
                intent.putExtra("payload", jsonObject.toString());

                Log.i("jsonObject", jsonObject.toString() + ",gifNotificationId :  " + notificationId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT);
                remoteViews.setOnClickPendingIntent(R.id.root_push_layout, pendingIntent);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                int applicationIcon = UpshotEnhancedPushUtils.getApplicationIcon(context);
                Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), applicationIcon);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(applicationIcon)
                        .setContentTitle(title)
                        .setContentText(contentMsg)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setLargeIcon(iconBitmap);


                addChannelSupport(context, notificationBuilder);

                notificationBuilder.setContent(remoteViews);
                notificationBuilder.setDeleteIntent(UpshotEnhancedPushUtils.getPendingIntent("gifDelete", notificationId, context));
                return notificationBuilder.build();
            }
        };
        try {
            gifNotification.showNotification(imageFilePath, R.id.push_image);
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
}