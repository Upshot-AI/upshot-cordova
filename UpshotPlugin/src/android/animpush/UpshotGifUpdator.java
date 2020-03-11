package cordova_plugin_upshotplugin.animpush;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.os.SystemClock;

import java.io.File;

/**
 * Created by PurpleTalk on 15/4/16.
 */
public class UpshotGifUpdator implements Runnable {

    private final Movie movie;
    private final String filePath;
    private long startTime;

    private final Bitmap bitmap;
    private final Canvas canvas;
    private final Paint paint;

    private boolean stopped;


    private GifUpdatorListener gifUpdatorListener;

    public UpshotGifUpdator(String filePath) {
        this.filePath = filePath;
        movie = Movie.decodeFile(filePath);

        paint = new Paint();
        paint.setAntiAlias(true);

        bitmap = Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        stopped = true;
    }


    @Override
    public void run() {
        stopped = false;


        while (!stopped) {
            long now = SystemClock.uptimeMillis();
            if (startTime == 0) {
                startTime = now;
            }

            int relTime = (int) ((now - startTime) % movie.duration());
            movie.setTime(relTime);

            movie.draw(canvas, movie.width(), movie.height(), paint);

            if (gifUpdatorListener != null && !stopped) {
                gifUpdatorListener.onGifRefereshed(bitmap);
            }

            SystemClock.sleep(1000);

        }


    }

    public boolean isStopped() {
        return stopped;
    }

    public void start() {
        if (!stopped) {
            return;
        }

        new Thread(this, "UpshotGifUpdator-" + new File(filePath).getName()).start();
    }

    public void stop() {
        stopped = true;

    }

    public void setGifUpdatorListener(GifUpdatorListener gifUpdatorListener) {
        this.gifUpdatorListener = gifUpdatorListener;
    }

    public interface GifUpdatorListener {
        /**
         * Will be invoked from worker  thread.
         *
         * @param bitmap
         */
        void onGifRefereshed(Bitmap bitmap);
    }
}
