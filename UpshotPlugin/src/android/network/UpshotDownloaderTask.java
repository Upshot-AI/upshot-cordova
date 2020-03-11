/*
 * //***************************************************************************************************
 * //***************************************************************************************************
 * //      Project adUnitName                    	: BrandKinesis
 * //      Class Name                              :
 * //      Author                                  : PurpleTalk
 * //***************************************************************************************************
 * //     Class Description:
 * //
 * //***************************************************************************************************
 * //***************************************************************************************************
 */

package cordova_plugin_upshotplugin;/**
 * Created by PurpleTalk on 19/8/15.
 */



import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpshotDownloaderTask implements Runnable {


    private static final String TAG = UpshotDownloaderTask.class.getSimpleName();

    public interface DownloaderTaskListener {
        void onDownloadStarted(String url, boolean resumed);

        void onDownloadProgress(String url, int downloaded, int contentLength);

        void onDownloadCompleted(String url, String filePath);

        void onError(Exception e);

    }


    private final String url;
    private final String filePath;

    private volatile  boolean stoped;

    private DownloaderTaskListener downloaderTaskListener;

    public UpshotDownloaderTask(String url, String filePath) {
        this.url = url;
        this.filePath = filePath;
    }

    public void setDownloaderTaskListener(DownloaderTaskListener downloaderTaskListener) {
        this.downloaderTaskListener = downloaderTaskListener;
    }

    public void stop() {
        stoped = true;
    }

    public boolean isStoped() {
        return stoped;
    }

    @Override
    public void run() {
        HttpURLConnection httpURLConnection = null;
        Exception ex = null;
        try {
            URL url = new URL(this.url);

            Log.v(TAG, "test combined getContentLength before ");
            int orgContentLength = getContentLength(url);
            Log.v(TAG,"test combined getContentLength after ");


            httpURLConnection = UpshotHttpHelper.createConnection(url);


            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Accept-Encoding", "identity");

            File file = new File(this.filePath);
            //httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
            httpURLConnection.setRequestProperty("Accept","*/*");

            Log.v(TAG,"test combined run ");

            int downloaded = 0;
            int alreadyDownloaded = 0;
            if (file.exists()) {

                if( orgContentLength==file.length()){
                    Log.v(TAG, "file already downloaded");
                    notifyDownloaded();
                    return;//???
                }

                alreadyDownloaded = downloaded = (int) file.length();
                httpURLConnection.setRequestProperty("Range", "bytes=" + downloaded + "-");
                Log.v(TAG, "resuming download from " + downloaded);
            }

//            httpURLConnection.setDoOutput(true);
            //connect
            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            if(responseCode !=HttpURLConnection.HTTP_OK && responseCode !=HttpURLConnection.HTTP_PARTIAL){
                throw new IOException("ResponseCode:"+responseCode+ " for URL:"+ this.url);
            }


            FileOutputStream fileOutput = downloaded == 0 ? new FileOutputStream(file) : new FileOutputStream(file, true);

            //Stream used for reading the data from the internet
            InputStream inputStream = httpURLConnection.getInputStream();
            if (!url.getHost().equals(httpURLConnection.getURL().getHost())) {
                //XXX we were redirected! Kick the user out to the browser to sign on?

                notifyError(new Exception(" redicted link, authentication might be required for " + url.toString()));
                return;
            }


            //this is the total size of the file which we are downloading
            int contentLength = httpURLConnection.getContentLength();


            //create a buffer...
            byte[] buffer = new byte[1024*2]; // 2kb
            int read;

            notifyDownloadStarted(downloaded != 0);

            while (!stoped && (read = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, read);
                downloaded += read;
                // update the progress
                notifyDownloadProgress(downloaded, contentLength);

            }
            //close the output stream when complete //
            fileOutput.close();

            if (stoped) {
                ex = new Exception("Explitly stopped; " + url);
            } else if (contentLength == -1 || contentLength == (downloaded-alreadyDownloaded)) {
                notifyDownloaded();
            } else {
                //FIXME partially downloaded
            }




        } catch (MalformedURLException e) {
            Log.v(TAG,"test combined run MalformedURLException :" + e.getMessage());

            e.printStackTrace();
            ex = e;
        } catch (IOException e) {
            Log.v(TAG,"test combined run IOException :" + e.getMessage());

            e.printStackTrace();
            ex = e;
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        if (ex != null) {
            notifyError(ex);
        }

    }

    private int getContentLength(URL url) {


        int contentLength = 0;
        try {
            HttpURLConnection httpURLConnection = UpshotHttpHelper.createConnection (url);
            httpURLConnection.setRequestProperty("Accept-Encoding", "identity");
            contentLength = httpURLConnection.getContentLength();
            if (contentLength == -1)
                Log.v(TAG,"getContentLength Content length unavailable."+url);
            else
                Log.v(TAG,"getContentLength Content-Length: " + contentLength);
            httpURLConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentLength;
    }

    private void notifyError(Exception ex) {
        if (downloaderTaskListener != null) {
            downloaderTaskListener.onError(ex);
        }
    }

    private void notifyDownloaded() {
        if (downloaderTaskListener != null) {
            downloaderTaskListener.onDownloadCompleted(url, filePath);
        }
    }

    private void notifyDownloadProgress(int downloaded, int contentLength) {
        if (downloaderTaskListener != null) {
            downloaderTaskListener.onDownloadProgress(url, downloaded, contentLength);
        }
    }

    private void notifyDownloadStarted(boolean resumed) {
        if (downloaderTaskListener != null) {
            downloaderTaskListener.onDownloadStarted(url, resumed);
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
