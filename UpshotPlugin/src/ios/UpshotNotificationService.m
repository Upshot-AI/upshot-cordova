//
//  UpshotNotificationService.m
//  Upshot
//
//  Created by Vinod on 15/12/17.
//  Copyright © 2017 Upshot. All rights reserved.
//

#import "UpshotNotificationService.h"

@interface UpshotNotificationService ()

@property (nonatomic, strong) void (^contentHandler)(UNNotificationContent *contentToDeliver);
@property (nonatomic, strong) UNMutableNotificationContent *bestAttemptContent;

@end

@implementation UpshotNotificationService 

- (void)didReceiveNotificationRequest:(UNNotificationRequest *)request withContentHandler:(void (^)(UNNotificationContent * _Nonnull))contentHandler {
    
    self.contentHandler = contentHandler;
    self.bestAttemptContent = [request.content mutableCopy];
    // Modify the notification content here...
    self.bestAttemptContent.title = self.bestAttemptContent.title;

    NSDictionary *notificationData = _bestAttemptContent.userInfo;
    NSString *urlString = notificationData[@"attachment-url"];
    NSURL *url = [NSURL URLWithString:urlString];
    NSString *identifier = notificationData[@"attachment-Type"];
    
    if (url != nil) {
        NSURLSessionDownloadTask *datatask = [[NSURLSession sharedSession] downloadTaskWithURL:url completionHandler:^(NSURL * _Nullable location, NSURLResponse * _Nullable response, NSError * _Nullable error) {
            
            NSString *tempDirectory = NSTemporaryDirectory();
            NSString *tempFile = [NSString stringWithFormat:@"file:%@%@",tempDirectory,[url lastPathComponent]];
            NSURL *tempUrl = [NSURL URLWithString:tempFile];
            [[NSFileManager defaultManager] moveItemAtURL:location toURL:tempUrl error:nil];
            UNNotificationAttachment *attachment = [UNNotificationAttachment attachmentWithIdentifier:identifier URL:tempUrl options:nil error:nil];
            self.bestAttemptContent.attachments = @[attachment];
            self.contentHandler(self.bestAttemptContent);
            
        }];
        [datatask resume];
    }
    
    
}

- (void)serviceExtensionTimeWillExpire {
    // Called just before the extension will be terminated by the system.
    // Use this as an opportunity to deliver your "best attempt" at modified content, otherwise the original push payload will be used.
    self.contentHandler(self.bestAttemptContent);
}

@end

