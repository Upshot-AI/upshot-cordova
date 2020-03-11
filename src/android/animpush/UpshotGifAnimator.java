package cordova_plugin_upshotplugin.animpush;

import android.graphics.Bitmap;
import android.os.SystemClock;

public  abstract class UpshotGifAnimator implements Runnable{

    private final UpshotGifDecoder decoder;

    public UpshotGifAnimator(UpshotGifDecoder decoder) {
        this.decoder = decoder;
    }


    private boolean stopped;


    @Override
    public void run() {
        stopped = false;
        int frameIndex = 0;

        while (!stopped) {
            //TODO
            Bitmap frame = decoder.getFrame(frameIndex);
            int delay = decoder.getDelay(frameIndex);
            int frameCount = decoder.getFrameCount();

            if(frameCount==0){
                SystemClock.sleep(250);
                continue;
            }

            updateFrame(frameIndex, frame, delay, frameCount);

            frameIndex++;
            if (frameIndex >= decoder.getFrameCount()) {
                frameIndex = 0;
            }
            SystemClock.sleep(delay);//intentional delay

        }
        stopped = true;

    }


    protected abstract void updateFrame(int frameIndex, Bitmap bmp, int delay, int frameCount);

    public void stop() {
        stopped = true;
    }

    public void start() {

//        Log.i(TAG, "start: ---");
//        if (isRunning()) {
//            Log.d(TAG, "start: already running");
//            return;
//        }


        new Thread(this, getClass().getName()).start();

    }

    public boolean isRunning() {
        return !stopped;
    }



}
