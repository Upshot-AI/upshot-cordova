package cordova_plugin_upshotplugin;

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

import com.purpletalk.upshot.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class UpshotPushPresenter {

    private static final String TAG = UpshotPushPresenter.class.getSimpleName();


    private static UpshotPushPresenter instance;

    private static String activityDirPath = "";


    public static void initInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new UpshotPushPresenter();
        }
    }

    public static UpshotPushPresenter getInstance()
    {
        initInstance();
        // Return the instance
        return instance;
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    public void sendNotification(Map<String, String> messageBody, Bundle pushPayload, Context context) {

        try {
            String title = "";
            String text = "";
            if (pushPayload.containsKey("bk")) {
                title = pushPayload.containsKey("bk") ? pushPayload.getString("title") : "";
                text = pushPayload.containsKey("bk") ? pushPayload.getString("alert") : messageBody.toString();

            if (pushPayload.containsKey("bundle_url") && pushPayload.containsKey("fileName")) {

                String fileName = pushPayload.getString("fileName");
                String url = pushPayload.getString("bundle_url");

                downloadEnhancedPushPackage(fileName, url, title, text, messageBody, context);
                return;
            }

            displayPushNotification(messageBody, title, text, null, context);
            }
        } catch (Exception e) {
        }

    }

    private  void displayPushNotification(Map<String, String> messageBody, String title, String text, Bitmap imageBitmap, Context context) {
        JSONObject jsonObject = new JSONObject(messageBody);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("push", true);
        intent.putExtra("payload", jsonObject.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_add)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_add));

        if (imageBitmap != null) {
            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(imageBitmap));

        }
        addChannelSupport(context, notificationBuilder);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private  String getApplicationExternalDirPath(Context context) {
        //FIXME inbox data being stored in external storage.
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

    public  void downloadEnhancedPushPackage(final String fileName, String url, String title, String contentMessage, Map<String, String> messageBody, Context context) {
        if (fileName.isEmpty() || url.isEmpty()) {
            displayPushNotification(messageBody, title, contentMessage, null, context);
        }else {
            String bkEnhancedPushDirPath = getApplicationExternalDirPath(context);
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
                        showImageBasedNotification(fileName, title, contentMessage, messageBody,context );
                    }

                    @Override
                    public void onZipFailed() {
                    }
                };

                new Thread(zipDownloaderTask, fileName).start();
            } else {
                showImageBasedNotification(fileName, title, contentMessage, messageBody, context);
            }
        }
    }

    private void showImageBasedNotification(String fileName, String title, String contentMessage, Map<String, String> messageBody, Context context) {
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
                    jsonContent = UpshotFileUtil.readString(new FileInputStream(file));
                    JSONObject actData = UpshotJsonUtil.isValidJsonObject(jsonContent);
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
                Bitmap bitmap = BitmapFactory.decodeFile(activityDirPath + File.separator + imageName, options);
                displayPushNotification(messageBody, title, contentMessage, bitmap, context);
            } catch (Exception e) {
            }
        }


    }

    private  void addChannelSupport(Context context, NotificationCompat.Builder notificationBuilder) {
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

    private  abstract class ZipDownloaderTask extends UpshotDownloaderTask implements UpshotDownloaderTask.DownloaderTaskListener {

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
                UpshotDecompress.unzip(new ZipInputStream(new FileInputStream(filePath)), appDir);
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
