//
//  UpshotPlugin.h
//  PushTest
//
//  Created by Vinod on 8/22/19.
//

#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import <Foundation/Foundation.h>


@interface UpshotPlugin : CDVPlugin

@property (nonatomic, strong) NSString *deviceToken;
@property (nonatomic, strong) NSString *deviceTokenCommandId;
@property (nonatomic, strong) NSString *pushDetailsCommandId;
@property (nonatomic, strong) NSString *carouselDeeplinkCommandId;
@property (nonatomic, strong) NSDictionary *pushDetails;
@property (nonatomic, strong) NSString *resgiterCommandId;

//Internal Methods
- (void)didRegisterForRemoteNotificationWithDeviceToken:(NSString *)token;
- (void)didFailToRegisterForRemoteNotificationWithError;
- (void)didReceiveRemoteNotification:(NSDictionary *)pushDetails;

// Plugin Methods
- (void)getDeviceToken:(CDVInvokedUrlCommand*)command;
- (void)getPushPayload:(CDVInvokedUrlCommand*)command;
- (void)registerForPushNotifications:(CDVInvokedUrlCommand*)command;



@end
