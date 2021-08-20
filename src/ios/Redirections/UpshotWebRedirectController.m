//
//  UpshotWebRedirectController.m
//  CordovaPlugins
//
//  Created by Vinod K on 6/1/21.
//

#import "UpshotWebRedirectController.h"
#import "UpshotWindowManager.h"
@import WebKit;

@interface UpshotWebRedirectController ()<WKNavigationDelegate> {
    
    WKWebView *webView;
}

@end

@implementation UpshotWebRedirectController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    if (_redirectURL == nil) {
        [self dismissViewControllerAnimated:NO completion:nil];
    }
    [_activityIndicator startAnimating];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self configureWebView];
    [self.view bringSubviewToFront:_skip];
}

- (void)configureWebView {
    
    NSURLRequest *request = [NSURLRequest requestWithURL:_redirectURL];
    webView = [[WKWebView alloc] initWithFrame:_contentView.bounds];
    [webView loadRequest:request];
    [webView setNavigationDelegate:self];
    [_contentView addSubview:webView];
}

- (IBAction)skip:(id)sender {
    
    [[UpshotWindowManager defaultManager] removeWindow];
}

- (IBAction)done:(id)sender {
    
    [[UpshotWindowManager defaultManager] removeWindow];
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation {
    [_activityIndicator stopAnimating];
}

@end
