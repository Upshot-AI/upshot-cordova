package cordova_plugin_upshotplugin.animpush;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

public class UpshotEnhancedPushUtils {


    public static int getApplicationIcon(Context context) {
        PackageManager p = context.getPackageManager();
        try {
            return p.getApplicationInfo(context.getPackageName(), 0).icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getIdFromTimestamp() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE) % 1000;
    }

    public static String getApplicationLabel(Context con) {
        ApplicationInfo info = con.getApplicationInfo();
        PackageManager p = con.getPackageManager();
        return p.getApplicationLabel(info).toString();
    }

    public static String getTitle(Bundle mBundle, Context mContext) {
        String title1 = getApplicationLabel(mContext);
        if (mBundle.containsKey("title") && (!TextUtils.isEmpty(mBundle.getString("title")))) {
            title1 = mBundle.getString("title");
            return title1;
        }
        return title1;
    }

    //Delete intent for AnimatedPush

    public static PendingIntent getPendingIntent(String action, int notificationId, Context mContext) {

        Intent intent = new Intent(mContext, UpshotGifNotificationDeleteReceiver.class);
        intent.setAction(action);
        intent.putExtra("gifNotificationId", notificationId);
        return PendingIntent.getBroadcast(mContext, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


}
