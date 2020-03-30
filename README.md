# Upshot Cordova Plugin

Upshot provides a Cordova Plugin that enable you to track events, send notifications, In-apps messages to your users

## Installation

Add Upshot Plugin to your project as show below

```bash
cordova plugin add cordova_plugin_upshotplugin
```

## Plugin Setup
### Android Specific Instructions

*  Add `com.google.gms:google-services` to dependencies in project level gradle


```bash
classpath "com.google.gms:google-services:3.1.1"
```
![image](https://s3.amazonaws.com/bk-purpletalk/shared_folder/media/images/1585591189056.png)


* apply `com.google.gms.google-services` to app level gradle

```bash
apply plugin: 'com.google.gms.google-services'
```

![image](https://s3.amazonaws.com/bk-purpletalk/shared_folder/media/images/1585591188451.png)

* Upshot provides multiple templates for PushNotifications. To enable / use those extend your Launcher class with UpshotBaseActivity

![image](https://s3.amazonaws.com/bk-purpletalk/shared_folder/media/images/1585591188701.png)

* If you already integrated push notification module using some other providers/plugins, you need to configure your app to work with Upshot push notification.

```Android

public class FcmMessageListenerService extends FirebaseMessagingService {
    
    @Override
    public void onMessageReceived(RemoteMessage message) {
        try {
            if (message.getData().size() > 0) {
                
                Bundle pushPayload = new Bundle();
                for (Map.Entry entry : message.getData().entrySet()) {
                    pushPayload.putString(entry.getKey(), entry.getValue());
                }
                if (pushPayload.containsKey("bk")) {
                    
                    UpshotPushPresenter.getInstance().sendNotification(remoteMessage.getData(), pushPayload, context);
                } else {
                    // not from Upshot handle yourself or pass to another provider
                }
            }
        } catch (Throwable t) {
        }
    }
}

```

## iOS Specific Instructions

### Rich push notifications:

iOS 10 brings us push notifications with Rich Media Attachment which includes viewing photos, videos, gifs and audio right there, within the notification. Having these media attachments as a part of your notification is achieved with the use of the new Notification Service Extension.

#### Create Notification Service Extension

In Xcode, select project > click on + > search Notification Service Extension and click on next.

![image](https://s3.amazonaws.com/bk-purpletalk/shared_folder/media/images/1585591446898.png)

#### If you want to use rich push notifications via Upshot implement below changes

* Add UpshotNotificationService.m class to Notification Service target

![image](https://s3.amazonaws.com/bk-purpletalk/shared_folder/media/images/1585591188199.png)

* import UpshotNotificationService.h class to NotificationService.h and make UpshotNotificationService as super class

```iOS
#import "UpshotNotificationService.h"
```

![image](https://s3.amazonaws.com/bk-purpletalk/shared_folder/media/images/1585591189533.png)

* Remove all the code and your NotificationService.m class should be like this.

![image](https://s3.amazonaws.com/bk-purpletalk/shared_folder/media/images/1585591189756.png)


## Usage

```js
cordova.plugins.UpshotPlugin.initialize(params, callback); 
```

Integrate Upshot Cordova to your project by using this [link](http://google.com)


