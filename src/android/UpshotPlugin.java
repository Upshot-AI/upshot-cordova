package cordova_plugin_upshotplugin;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class UpshotPlugin extends CordovaPlugin {


    private static final String TAG = UpshotPlugin.class.getName();
    private static CallbackContext tokenCallbackContext;
    private static CallbackContext pushCallbackContext;
    private static String pushPayload;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getDeviceToken")) {
            this.getDeviceToken(callbackContext);            
            return true;
        }

        if (action.equals("getPushPayload")) {
            this.getPushPayload(callbackContext);
            return true;
        }
        return false;
    }

    public static void sendToken(String token) {

        if (tokenCallbackContext != null) {

            if (!TextUtils.isEmpty(token)) {
                
                try {
                    tokenCallbackContext.success(token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "sendToken in plugin 3");

                tokenCallbackContext.error("Expected one non-empty string argument.");
            }
        }
        
    }

    public static void sendPushPayload(String payload) {

        pushPayload = payload;
        if (pushCallbackContext != null) {
            pushPayload = null;
            if (!TextUtils.isEmpty(payload)) {

                try {
                    pushCallbackContext.success(payload);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //Plugin Methods
    private void getDeviceToken(CallbackContext callbackContext) {

        tokenCallbackContext = callbackContext;
        sendToken(FirebaseInstanceId.getInstance().getToken());
        Log.d(TAG, "getDeviceToken in plugin");

    }

    private void getPushPayload(CallbackContext callbackContext) {

        pushCallbackContext = callbackContext;
        if (!TextUtils.isEmpty(pushPayload)) {
            Log.d(TAG, "sendPushPayload in plugin 2");
            sendPushPayload(pushPayload);
        }

    }
}

