package cordova_plugin_upshotplugin;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


/**
 * Created by PurpleTalk on 30/12/16.
 */
public class UpshotFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public static String TAG = "FirebaseInstanceIDServ";
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "gcm token: " + refreshedToken);

        cordova_plugin_upshotplugin.UpshotPlugin.sendToken(refreshedToken);


    }
}
