/********* UpshotPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import "UpshotPlugin.h"
#import "UpshotShareViewController.h"
#import "UpshotWebRedirectController.h"
#import "UpshotWindowManager.h"
#import "UpshotStoreViewController.h"

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
     if (_pushDetails != nil) {
        
        NSString *message = [self getJsonStringFromDict:_pushDetails];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_pushDetailsCommandId];

    }
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

- (void)redirectionCallback:(CDVInvokedUrlCommand*)command {
    
    NSString *jsonString = command.arguments[0];
    NSDictionary *jsonData = [self getJsonFromString:jsonString];
    NSInteger type = [jsonData[@"type"] integerValue];
    NSString *deeplink = jsonData[@"deeplink"];
    if (type == 5 || type == 4) {
        [self customRedirection:deeplink];
    } else if(type == 3) {
        [self webRedirection:deeplink];
    } else if (type == 1) {
        [self storeRedirection:deeplink];
    }
}

- (void)shareCallback:(CDVInvokedUrlCommand*)command {
    
    NSString *jsonString = command.arguments[0];
    [self share:jsonString];
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
        _pushDetails = nil;
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

- (NSDictionary *)getJsonFromString:(NSString *)jsonString {
    
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableLeaves error:nil];
    return json;
}

- (void)webRedirection:(NSString *)webUrl {
    
    NSURL *url = [NSURL URLWithString:webUrl];
    UpshotWebRedirectController *vc = [[UpshotWebRedirectController alloc] initWithNibName:@"UpshotWebRedirectController" bundle:[NSBundle mainBundle]];
    vc.redirectURL = url;
    [[UpshotWindowManager defaultManager] showUpshotWindow:vc];
}

- (void)storeRedirection:(NSString *)storeId {
    
    UpshotStoreViewController *vc = [[UpshotStoreViewController alloc] initWithNibName:@"UpshotStoreViewController" bundle:[NSBundle mainBundle]];
    vc.storeId = [NSNumber numberWithDouble:[storeId doubleValue]];
    [[UpshotWindowManager defaultManager] showUpshotWindow:vc];
}

- (void)customRedirection:(NSString *)redirectionUrl {
    
    NSURL *url = [NSURL URLWithString:redirectionUrl];
    if (url != nil && [[UIApplication sharedApplication] canOpenURL:url]) {
        [[UIApplication sharedApplication] openURL:url options:@{} completionHandler: nil];
    }
}

- (void)share:(NSString *)shareData {
    
    NSData *data = [shareData dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
    UpshotShareViewController *vc = [[UpshotShareViewController alloc] init];
    vc.shareData = json;
    [[UpshotWindowManager defaultManager] showUpshotWindow:vc];
}

@end
