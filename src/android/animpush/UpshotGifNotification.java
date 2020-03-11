package cordova_plugin_upshotplugin.animpush;

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class UpshotGifNotification {
    private static final String TAG = "UpshotGifNotification";

    private final NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews remoteViews;
    private int notificationId;
    private UpshotGifAnimator gifAnimator;

    public UpshotGifNotification(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }


    protected abstract RemoteViews onCreateRemoteViews();

    protected abstract Notification onCreateNotification(RemoteViews remoteViews,int notificationId);

    /**
     * creates and starts animation
     *
     * @param gifPath
     * @param remoteImageViewId
     * @throws FileNotFoundException
     */
    public void showNotification(String gifPath, int remoteImageViewId) throws FileNotFoundException {
        showNotification(new FileInputStream(gifPath),remoteImageViewId);
    }

    public void showNotification(InputStream stream, final int remoteImageViewId){
        gifAnimator = new UpshotGifAnimator(UpshotGifDecoder.decode(stream)) {

            @Override
            protected void updateFrame(int frameIndex, Bitmap bmp, int delay, int frameCount) {
                Log.d(TAG, "updateFrame() called with: " + "frameIndex = [" + frameIndex + "], delay = [" + delay + "], frameCount = [" + frameCount + "]");
                remoteViews.setImageViewBitmap(remoteImageViewId,bmp);
                notificationManager.notify(notificationId,notification);
            }
        };

        remoteViews = onCreateRemoteViews();
        notificationId = (int) (System.currentTimeMillis() %100); // uniq id for the notification
        notification = onCreateNotification(remoteViews,notificationId);
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        notificationManager.notify(notificationId, notification);


        gifAnimator.start();

        UpshotGifNotificationDeleteReceiver.addAnimator(notificationId,gifAnimator);

    }

    /**
     * this should be the called first on contentIntent and deleteIntent  receiver.
     */
    public void stopAnimation(){
        gifAnimator.stop();
    }

    /**
     * explicit start if required.
     */
    public void startAnimation(){
        gifAnimator.start();
    }
}
