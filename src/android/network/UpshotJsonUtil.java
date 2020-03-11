package cordova_plugin_upshotplugin;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by PurpleTalk on 18/8/17.
 */

public class UpshotJsonUtil {

    private static final String TAG = UpshotJsonUtil.class.getSimpleName();

    public static JSONObject isValidJsonObject(String data) {

        if (data == null || data.isEmpty()) {
            return new JSONObject();
        }

        try {
            return new JSONObject(data);
        } catch (JSONException ex) {
            Log.v(TAG,"invalid json object:", ex);
            return null;
        }
    }

}
