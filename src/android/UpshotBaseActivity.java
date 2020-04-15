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
import android.os.Handler;
import android.util.Log;

import org.apache.cordova.CordovaActivity;
import org.json.JSONObject;

import cordova_plugin_upshotplugin.animpush.UpshotGifNotificationDeleteReceiver;

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
                performUpshotPushOperation(intent);
            }
        }
        createNotificationChannels();
    }

    private void performUpshotPushOperation(Intent intent) {
        try {

            Log.i("test push", "bundle is not empty");
            String payload = intent.getStringExtra("payload");

            cordova_plugin_upshotplugin.UpshotPlugin.sendPushPayload(payload);
            JSONObject jsonObject = new JSONObject(payload);
            String layoutType = jsonObject.getString("layoutType");
            if (layoutType != null && layoutType.equals("animated-msg")) {
                int notificationId = jsonObject.getInt("gifNotificationId");
//                        int notificationId = intent.getIntExtra("gifNotificationId", -1);
                Log.i("jsonObject", jsonObject.toString() + ",gifNotificationId :  " + notificationId);

                UpshotGifNotificationDeleteReceiver.removeAnimator(notificationId);

                final int finalNotificationId = notificationId;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(finalNotificationId);
                    }
                }, 250);
            }
        } catch (Exception e) {
        }
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
            performUpshotPushOperation(intent);
        }
    }
}
