//
//  UpshotWebRedirectController.h
//  CordovaPlugins
//
//  Created by Vinod K on 6/1/21.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UpshotWebRedirectController : UIViewController

@property(nonatomic, strong) NSURL *redirectURL;

@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicator;
@property (nonatomic, weak) IBOutlet UIButton *skip;
@property (nonatomic, weak) IBOutlet UIButton *done;
@property (nonatomic, weak) IBOutlet UIView *contentView;


@end

NS_ASSUME_NONNULL_END
