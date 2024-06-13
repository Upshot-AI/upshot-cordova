package cordova_plugin_upshotplugin;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class UpshotPushAction extends BroadcastReceiver {
  @Override
  public void onReceive(final Context context, Intent intent) {

    final Bundle bundle = intent.getExtras();

    if (bundle != null && bundle.containsKey("bk")) {
      String actionData = bundle.getString("actionData");
      String bkPushPayload = intent.getStringExtra("bkPushPayload");
      String carouselAction = intent.getStringExtra("carouselAction");
      if (bkPushPayload == null || bkPushPayload.isEmpty()) {
        bkPushPayload = "";
      }
      try {
        JSONObject bkJson = new JSONObject(bkPushPayload);
        if (bkJson.has("bk_mdata")) {
          String bk_mdata = bkJson.getString("bk_mdata");
          if (!TextUtils.isEmpty(bk_mdata)) {
            JSONObject bk_mdataJson = new JSONObject(bk_mdata);
            bkJson.put("bk_mdata", bk_mdataJson);
          }
        }

        if (!TextUtils.isEmpty(actionData)) {
          bkJson.put("action_click", actionData);
        }
        cordova_plugin_upshotplugin.UpshotPlugin.sendPushPayload(bkJson.toString());
        if (!TextUtils.isEmpty(carouselAction)) {
          cordova_plugin_upshotplugin.UpshotPlugin.sendCarouselPayload(carouselAction);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

      Class mainActivity = null;
      String packageName = context.getPackageName();
      Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
      String className = launchIntent.getComponent().getClassName();
      try { // loading the Main Activity to not import it in the plugin
        mainActivity = Class.forName(className);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (mainActivity == null) {
        return;
      }
      launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |
          Intent.FLAG_ACTIVITY_CLEAR_TASK);
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |
          Intent.FLAG_ACTIVITY_CLEAR_TASK);
      Handler h = new Handler();
      h.postDelayed(new Runnable() {
        public void run() {
          if (isAppInForeground(context) == false) {
            context.startActivity(launchIntent);
          }
        }
      }, 500);
    }
  }

  public static boolean isAppInForeground(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
      if (appProcesses != null) {
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
          if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
              appProcess.processName.equals(packageName)) {
            return true;
          }
        }
      }
    } else {
      List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
      if (taskInfo != null && !taskInfo.isEmpty()) {
        return taskInfo.get(0).topActivity.getPackageName().equals(context.getPackageName());
      }
    }
    return false;
  }
}
