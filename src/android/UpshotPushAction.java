package cordova_plugin_upshotplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.json.JSONObject;

public class UpshotPushAction extends BroadcastReceiver {
  @Override
  public void onReceive(final Context context, Intent intent) {

    final Bundle bundle = intent.getExtras();
    if (bundle != null && bundle.containsKey("bk")) {
      String payload = bundle.toString();
      String actionData = bundle.getString("actionData");
      cordova_plugin_upshotplugin.UpshotPlugin.sendPushPayload(payload);
      try {
        JSONObject jsonObject = new JSONObject(actionData);
        cordova_plugin_upshotplugin.UpshotPlugin.sendCarouselPayload(actionData);
      } catch (Exception e) {

      }

      Class mainActivity = null;
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
      launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |
        Intent.FLAG_ACTIVITY_CLEAR_TASK);
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |
        Intent.FLAG_ACTIVITY_CLEAR_TASK);
      // context.startActivity(launchIntent);
  }
  }
}