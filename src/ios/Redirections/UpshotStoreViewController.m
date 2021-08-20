//
//  UpshotStoreViewController.m
//  CordovaPlugins
//
//  Created by Vinod K on 6/1/21.
//

#import "UpshotStoreViewController.h"
#import "UpshotWindowManager.h"

@import StoreKit;

@interface UpshotStoreViewController ()<SKStoreProductViewControllerDelegate> {
    
    SKStoreProductViewController *storeVC;
}

@end

@implementation UpshotStoreViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];    
    [self presentStoreController];    
}

- (void)presentStoreController {
    
    NSDictionary *productParameters = @{ SKStoreProductParameterITunesItemIdentifier :_storeId};
    storeVC = [[SKStoreProductViewController alloc] init];
    storeVC.delegate = self;
    [storeVC loadProductWithParameters:productParameters completionBlock:^(BOOL result, NSError * _Nullable error) {
        
        if (error != nil) {
            [[UpshotWindowManager defaultManager] removeWindow];
        }
    }];
    [self presentViewController:storeVC animated:YES completion:nil];
}

- (void)productViewControllerDidFinish:(SKStoreProductViewController *)viewController {
    
    [[UpshotWindowManager defaultManager] removeWindow];
}

@end
