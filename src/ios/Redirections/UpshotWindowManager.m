//
//  UpshotWindowManager.m
//  CordovaPlugins
//
//  Created by Vinod K on 6/1/21.
//

#import "UpshotWindowManager.h"


@implementation UpshotWindowManager

static UpshotWindowManager *singleton = nil;

+(UpshotWindowManager *)defaultManager {
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (singleton == nil) {
            singleton = [[[self class] alloc] init];
        }
    });
    return singleton;
}

- (void)showUpshotWindow:(UIViewController *)rootController {
    
    _window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    [_window setRootViewController:rootController];
    [_window setHidden:NO];
}

- (void)removeWindow {
    
    [_window setHidden:YES];
    _window = nil;
}


@end
