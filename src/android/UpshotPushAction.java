package cordova_plugin_upshotplugin;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;
import java.util.List;

public class UpshotPushAction extends BroadcastReceiver {
  @Override
  public void onReceive(final Context context, Intent intent) {

    final Bundle bundle = intent.getExtras();
    if (bundle != null && bundle.containsKey("bk")) {

      String actionData = bundle.getString("actionData");
      String bkPushPayload = bundle.getString("bkPushPayload");

      try {
        JSONObject bkJson = new JSONObject(bkPushPayload);
        cordova_plugin_upshotplugin.UpshotPlugin.sendPushPayload(bkJson.toString());
      } catch (JSONException e) {
        e.printStackTrace();
      }

      try {
        JSONObject jsonObject = new JSONObject(actionData);
        cordova_plugin_upshotplugin.UpshotPlugin.sendCarouselPayload(actionData);
      } catch (Exception e) {

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
      if (isAppForeground(context) == false) {
        context.startActivity(launchIntent);
      }
    }
  }

  public static String bundleToJsonString(Bundle bundle) {

    JSONObject json = new JSONObject();
    Set<String> keys = bundle.keySet();
    for (String key : keys) {
      try {
        String newKey = key;
        if (key.equals("bkMetadata")) {
          newKey = "bk_mdata";
        }
        json.put(newKey, JSONObject.wrap(bundle.get(key)));
      } catch (JSONException e) {

      }
    }
    return json.toString();
  }

  public static boolean isAppForeground(Context context) {

    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> services = activityManager.getRunningAppProcesses();

    return services != null && services.size() > 0
        && services.get(0).processName.equalsIgnoreCase(context.getPackageName())
        && services.get(0).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

  }
}
