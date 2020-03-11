package cordova_plugin_upshotplugin.animpush;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

public class UpshotGifNotificationDeleteReceiver extends BroadcastReceiver {
    private static final String TAG = "GifDeleteReceiver";

    private static final SparseArray<UpshotGifAnimator> animators = new SparseArray<>();

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("gifDelete")) {
            Log.d(TAG, "onReceive: gifDelete received");
            final int gifNotificationId = intent.getIntExtra("gifNotificationId", -1);

            removeAnimator(gifNotificationId);

            //force delete the notificaiton if required.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(gifNotificationId);
                }
            }, 250);
        }
    }

    public static void addAnimator(int notificationId, UpshotGifAnimator animator) {
        animators.put(notificationId, animator);
    }

    public static void removeAnimator(int notificationId) {
        UpshotGifAnimator animator = animators.get(notificationId);
        if (animator != null) {
            animator.stop();
        }
        animators.remove(notificationId);
    }
}
