package cordova_plugin_upshotplugin;



import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by PurpleTalk on 20/4/17.
 */

public class UpshotHttpHelper {


    public static BkUserAgentProvider bkUserAgentProvider;

    private UpshotHttpHelper() {
    }

    /**
     * Create HTTP Connection
     *
     * @param url API Url
     * @return HttpURLConnection
     * @throws IOException
     */
    public static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        //String bk_http_agent = BKSingleton.getInstance().BK_HTTP_AGENT;
        if(bkUserAgentProvider!=null && bkUserAgentProvider.getUserAgent()!=null){
            httpURLConnection.setRequestProperty("User-Agent", bkUserAgentProvider.getUserAgent());
        }

        return httpURLConnection;
    }

    public interface BkUserAgentProvider{
        String getUserAgent();
    }
}
