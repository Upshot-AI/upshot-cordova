//
//  UpshotWindowManager.h
//  CordovaPlugins
//
//  Created by Vinod K on 6/1/21.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface UpshotWindowManager : NSObject
@property (nonatomic, strong) UIWindow *window;

+(UpshotWindowManager *)defaultManager;
- (void)showUpshotWindow:(UIViewController *)rootController;
- (void)removeWindow;

@end

NS_ASSUME_NONNULL_END
