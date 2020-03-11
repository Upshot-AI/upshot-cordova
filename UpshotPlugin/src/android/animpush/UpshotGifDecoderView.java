
package cordova_plugin_upshotplugin.animpush;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.io.FileNotFoundException;
import java.io.InputStream;


@RemoteViews.RemoteView
public class UpshotGifDecoderView extends ImageView {

    private boolean mIsPlayingGif = false;

    private UpshotGifDecoder mGifDecoder;

    private Bitmap mTmpBitmap;

    final Handler mHandler = new Handler();

    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
                UpshotGifDecoderView.this.setImageBitmap(mTmpBitmap);
            }
        }
    };

    public UpshotGifDecoderView(Context context) {
        super(context);

    }

    public UpshotGifDecoderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UpshotGifDecoderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public GifDecoderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    private void playGif(InputStream stream) {
        mGifDecoder = new UpshotGifDecoder();
        mGifDecoder.read(stream);

        mIsPlayingGif = true;

        new Thread(new Runnable() {
            public void run() {
                final int n = mGifDecoder.getFrameCount();
                final int ntimes = mGifDecoder.getLoopCount();
                int repetitionCounter = 0;
                do {
                    for (int i = 0; i < n; i++) {
                        mTmpBitmap = mGifDecoder.getFrame(i);
                        int t = mGifDecoder.getDelay(i);
                        mHandler.post(mUpdateResults);
                        try {
                            Thread.sleep(t);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(ntimes != 0) {
                        repetitionCounter ++;
                    }
                } while (mIsPlayingGif && (repetitionCounter <= ntimes));
            }
        }).start();
    }
    
    public void stopRendering() {
        mIsPlayingGif = true;
    }

    @Override
    public void setImageURI(Uri uri) {
        //super.setImageURI(uri);
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
//            InputStream inputStream = new FileInputStream(new File(uri.toString()));

            playGif(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
