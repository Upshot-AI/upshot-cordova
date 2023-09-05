/********* UpshotPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import "UpshotPlugin.h"
@import UpshotCordovaPlugin;
@import UserNotifications;
@import StoreKit;

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

- (void)getCarouselDeeplink:(CDVInvokedUrlCommand*)command {
    
    self.carouselDeeplinkCommandId = command.callbackId;     
}

- (void)registerForPushNotifications:(CDVInvokedUrlCommand*)command {
       
    if ([[[UIDevice currentDevice] systemVersion]floatValue] >= 10.0 ) {
        
        id delegate = [UIApplication sharedApplication].delegate;
        
        UNUserNotificationCenter *notificationCenter = [UNUserNotificationCenter currentNotificationCenter];
        [notificationCenter requestAuthorizationWithOptions:(UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound ) completionHandler:^(BOOL granted, NSError * _Nullable error) {

            if (granted) {
                if(delegate != nil) {
                    [notificationCenter setDelegate:delegate];
                }
            }
            NSDictionary *status = @{@"status": [NSNumber numberWithBool:granted]};
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[self getJsonStringFromDict:status]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            
        }];
        dispatch_async(dispatch_get_main_queue(), ^{
            [[UIApplication sharedApplication] registerForRemoteNotifications];
        });
    }        
}

- (void)redirectionCallback:(CDVInvokedUrlCommand*)command {
    
    NSString *jsonString = command.arguments[0];
    NSDictionary *jsonData = [self getJsonFromString:jsonString];
    if(jsonData == nil) {
        return;
    }
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

- (void)ratingStoreRedirectionCallback:(CDVInvokedUrlCommand *)command {
        
    NSString *jsonString = command.arguments[0];
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
    
    if(json != nil) {
        
        NSInteger type = [json[@"ratingType"] integerValue];
        NSString *url = json[@"url"];
        
        if (type == 1) {
            if (@available(iOS 10.3,*)) {
                [SKStoreReviewController requestReview];
            } else {
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(.2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    [self customRedirection:url];
                });
            }
        } else {
            [self customRedirection:url];
        }
    }
}

- (void)sendPushPayload:(CDVInvokedUrlCommand*)command {
    
    NSString *jsonString = command.arguments[0];
    if(jsonString != nil && ![jsonString isEqualToString:@""]) {
        
        NSDictionary *userinfo = [self getJsonFromString: jsonString];
        NSBundle *bundle = [NSBundle mainBundle];
        NSString *bundleIdentifier = [bundle bundleIdentifier];
        NSString *groupName = [NSString stringWithFormat:@"group.%@.Upshot",bundleIdentifier];
        NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:groupName];
        NSDictionary *payload = [defaults valueForKey:@"UpshotPush_SelectedIndex"];
        if ([userinfo objectForKey:@"ios_deeplink"]) {
            if (payload != nil && [payload objectForKey:@"index"]) {
                NSInteger selectedIndex = [payload[@"index"] integerValue];
                NSArray *deeplinks = userinfo[@"ios_deeplink"];
                if (deeplinks.count > 0) {
                    NSDictionary *deeplinkData = @{};
                    if (selectedIndex <= deeplinks.count - 1) {
                        deeplinkData = deeplinks[selectedIndex];
                        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[self getJsonStringFromDict:deeplinkData]];
                        [pluginResult setKeepCallbackAsBool:YES];
                        [self.commandDelegate sendPluginResult:pluginResult callbackId:_carouselDeeplinkCommandId];
                    }
                }
            }
        }
    }
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
    
   dispatch_async(dispatch_get_main_queue(), ^{
        NSBundle *bundle = [NSBundle bundleForClass:[UpshotWebRedirectController class]];

        NSURL *url = [NSURL URLWithString:webUrl];
        UpshotWebRedirectController *vc = [[UpshotWebRedirectController alloc] initWithNibName:@"UpshotWebRedirectController" bundle:bundle];
        vc.redirectURL = url;
        [[UpshotWindowManager defaultManager] showUpshotWindow:vc];
    });
}

- (void)storeRedirection:(NSString *)storeUrl {
    
    dispatch_async(dispatch_get_main_queue(), ^{
        NSBundle *bundle = [NSBundle bundleForClass:[UpshotStoreViewController class]];
        UpshotStoreViewController *vc = [[UpshotStoreViewController alloc] initWithNibName:@"UpshotStoreViewController" bundle:bundle];
        
        NSString *storeId = @"";
        if ([storeUrl containsString:@"id"]) {
            NSArray *splitData = [storeUrl componentsSeparatedByString:@"id"];
            if (splitData.count ==2) {
                storeId = splitData[1];
            }
        } else {
            storeId = storeUrl;
        }
        vc.storeId = [NSNumber numberWithDouble:[storeId doubleValue]];
        [[UpshotWindowManager defaultManager] showUpshotWindow:vc];
    });
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

- (void)getDefaultAccountAndUserDetails:(CDVInvokedUrlCommand*)command {
    
    NSString *jsonString = command.arguments[0];
    NSDictionary *jsonData = [self getJsonFromString:jsonString];
    if(jsonData == nil) {
        return;
    }
    NSString *bundleId = [[NSBundle mainBundle] bundleIdentifier];
    NSString *groupName = [NSString stringWithFormat:@"group.%@.Upshot",bundleId];
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:groupName];
    [defaults setObject:[self validateString:jsonData[@"UpshotApplicationID"]] forKey:@"upshot_appId"];
    [defaults setObject:[self validateString:jsonData[@"UpshotApplicationOwnerID"]] forKey:@"upshot_ownerId"];
    [defaults setObject:[self validateString:jsonData[@"UpshotAppUID"]] forKey:@"upshot_appuid"];
    [defaults setObject:[self validateString:jsonData[@"UpshotSessionID"]] forKey:@"upshot_sessionId"];
    [defaults setObject:[self validateString:jsonData[@"UpshotUserID"]] forKey:@"upshot_userId"];
    [defaults setObject:[self validateString:jsonData[@"UpshotVersion"]] forKey:@"upshot_sdkVersion"];
    [defaults synchronize];
}

- (NSString *)validateString:(NSString *)value {
    
    if (value == nil || ![value isKindOfClass:[NSString class]]) {
        return @"";
    } else {
        return value;
    }
}

@end
