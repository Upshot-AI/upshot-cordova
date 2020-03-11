/********* UpshotPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import "UpshotPlugin.h"
@import UserNotifications;

@interface UpshotPlugin () <UNUserNotificationCenterDelegate>

@end

@implementation UpshotPlugin

#pragma mark Plugin Methods

- (void)getDeviceToken:(CDVInvokedUrlCommand*)command
{
    self.deviceTokenCommandId = command.callbackId;
    
    if (self.deviceToken != nil) {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:self.deviceToken];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)getPushPayload:(CDVInvokedUrlCommand*)command {
    
    self.pushDetailsCommandId = command.callbackId;
}

- (void)registerForPushWithForeground:(CDVInvokedUrlCommand*)command {
    
    if (command.arguments.count == 1) {
        self.allowForegroundPush = command.arguments[0];
    }
    if ([[[UIDevice currentDevice] systemVersion]floatValue] >= 10.0 ) {
        
        UNUserNotificationCenter *notificationCenter = [UNUserNotificationCenter currentNotificationCenter];
        [notificationCenter requestAuthorizationWithOptions:(UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound ) completionHandler:^(BOOL granted, NSError * _Nullable error) {

            NSDictionary *status = @{@"status": [NSNumber numberWithBool:granted]};
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[self getJsonStringFromDict:status]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            if (granted) {
                [notificationCenter setDelegate:self];
            }
        }];        
    } else {
        
        UIUserNotificationType types = (UIUserNotificationTypeAlert|
                                        UIUserNotificationTypeSound|
                                        UIUserNotificationTypeBadge);        
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication] registerForRemoteNotifications];    
    });    
}

#pragma mark Internal Methods

- (void)didRegisterForRemoteNotificationWithDeviceToken:(NSString *)token {
    
    self.deviceToken = token;
    if (_deviceTokenCommandId != nil) {
        
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:token];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_deviceTokenCommandId];
    }
}

- (void)didFailToRegisterForRemoteNotificationWithError {
    
    if (_deviceTokenCommandId != nil) {        
        
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_deviceTokenCommandId];
    }
}

- (void)didReceiveRemoteNotification:(NSDictionary *)pushDetails {
    
    self.pushDetails = pushDetails;
    if (_pushDetailsCommandId != nil) {
        
        NSString *message = [self getJsonStringFromDict:pushDetails];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_pushDetailsCommandId];

    }
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    
    if (self.allowForegroundPush) {

        completionHandler(UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound);
    }
}

- (NSString *)getJsonStringFromDict:(NSDictionary *)dict {
    
    NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:nil];
    NSString *json = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    return json;
}
@end
