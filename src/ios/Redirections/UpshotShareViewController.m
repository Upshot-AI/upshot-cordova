//
//  UpshotShareViewController.m
//  CordovaPlugins
//
//  Created by Vinod K on 6/1/21.
//

#import "UpshotShareViewController.h"
#import "UpshotWindowManager.h"

@import Photos;

@interface UpshotShareViewController ()

@end

@implementation UpshotShareViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self showActivityController];
}

- (void)showActivityController {
    
    PHAuthorizationStatus status = [PHPhotoLibrary authorizationStatus];
    NSArray *shareItems = [self sharedItems];
    if (shareItems.count <= 0) {
        [[UpshotWindowManager defaultManager] removeWindow];
        return;
    }
    
    UIActivityViewController *activityController = [[UIActivityViewController alloc] initWithActivityItems:shareItems  applicationActivities:nil];
    if (status == PHAuthorizationStatusNotDetermined || (status == PHAuthorizationStatusDenied)) {
        [activityController setExcludedActivityTypes:@[UIActivityTypeSaveToCameraRoll]];
    }
    [activityController setCompletionWithItemsHandler:^(NSString *activityType,
                                                        BOOL completed,
                                                        NSArray *returnedItems,
                                                        NSError *error){
        [[UpshotWindowManager defaultManager] removeWindow];
    }];
    [self presentViewController:activityController animated:YES completion:nil];
}

- (NSArray *)sharedItems {
    
    NSMutableArray *sharedItems = [[NSMutableArray alloc] init];
    NSString *text = _shareData[@"text"];
    NSString *base64String = _shareData[@"image"];
    NSURL *url = [NSURL URLWithString:base64String];
    NSData *decodedImageData = [NSData dataWithContentsOfURL:url];
    if (decodedImageData != nil) {
        [sharedItems addObject:decodedImageData];
    }
    
    if (text != nil && ![text isEqual:@""]) {
        [sharedItems addObject:text];
    }
 
    return sharedItems;
}
@end
