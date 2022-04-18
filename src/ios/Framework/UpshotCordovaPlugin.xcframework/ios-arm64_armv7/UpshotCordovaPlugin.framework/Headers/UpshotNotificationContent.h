//
//  UpshotNotificationContent.h
//  Upshot
//
//  Created by Vinod K on 11/19/20.
//  Copyright © 2020 [x]cube LABS. All rights reserved.
//

#import <Foundation/Foundation.h>
@import UIKit;
@import UserNotifications;

NS_ASSUME_NONNULL_BEGIN

@interface UpshotNotificationContent : NSObject

- (void)displayEnhancePush:(UIViewController *)controller content:(UNNotification *)notification API_AVAILABLE(ios(10.0));
@end

NS_ASSUME_NONNULL_END
