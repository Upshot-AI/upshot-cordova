/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */
package cordova_plugin_upshotplugin;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CordovaActivity;

public class UpshotBaseActivity extends CordovaActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            Log.i("test push", "bundle is not empty");
        }

        Log.i("test push", "bmain activity");
        Intent intent = getIntent();
        if (intent != null) {
            boolean push = intent.getBooleanExtra("push", false);
            if (push) {
                Log.i("test push", "bundle is not empty");
                cordova_plugin_upshotplugin.UpshotPlugin.sendPushPayload(intent.getStringExtra("payload"));
            }
        }
        createNotificationChannels();
    }
    
    private void createNotificationChannels() {
    try {
    
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
    
    NotificationChannel notificationChannel = new NotificationChannel("notifications", "notifications", NotificationManager.IMPORTANCE_HIGH);
    notificationChannel.enableLights(true);
    notificationChannel.enableVibration(true);
    
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    
    if (notificationManager != null) {
    notificationManager.createNotificationChannel(notificationChannel);
    }
    }
    } catch (Exception e) {
    }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean push = intent.getBooleanExtra("push", false);
        if (push) {
            cordova_plugin_upshotplugin.UpshotPlugin.sendPushPayload(intent.getStringExtra("payload"));
        }
    }
}
