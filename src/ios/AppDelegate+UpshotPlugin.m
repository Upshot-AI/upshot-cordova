//
//  AppDelegate+UpshotPlugin.m
//  PushTest
//
//  Created by Vinod on 3/3/20.
//

#import "AppDelegate+UpshotPlugin.h"
#import "UpshotPlugin.h"
#import <Cordova/CDVAppDelegate.h>

@implementation AppDelegate (UpshotPlugin)

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    
    NSString *pushToken = [self getTokenFromdata:deviceToken];
    UpshotPlugin *plugin = [self getCommandInstance:@"UpshotPlugin"];
    [plugin didRegisterForRemoteNotificationWithDeviceToken:pushToken];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
    
       UpshotPlugin *plugin = [self getCommandInstance:@"UpshotPlugin"];
       [plugin didRegisterForRemoteNotificationWithDeviceToken:@""];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    
    UpshotPlugin *plugin = [self getCommandInstance:@"UpshotPlugin"];
    [plugin didReceiveRemoteNotification:userInfo];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    
    completionHandler(UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound);
}

- (NSString *)getTokenFromdata:(NSData *)data {

  NSUInteger dataLength = data.length;
  if (dataLength == 0) {
    return nil;
  }

  const unsigned char *dataBuffer = (const unsigned char *)data.bytes;
  NSMutableString *hexString  = [NSMutableString stringWithCapacity:(dataLength * 2)];
  for (int i = 0; i < dataLength; ++i) {
    [hexString appendFormat:@"%02x", dataBuffer[i]];
  }
  return [hexString copy];
}

- (id)getCommandInstance:(NSString *)className {
    
    return [self.viewController getCommandInstance:className];
}

@end
