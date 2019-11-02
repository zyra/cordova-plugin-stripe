#import "AppDelegate+CordovaStripe.h"
#import "CordovaStripe.h"
@import Stripe;

@implementation AppDelegate (CordovaStripe)
static NSString* const PLUGIN_NAME = @"CordovaStripe";
- (void)paymentAuthorizationViewController:(PKPaymentAuthorizationViewController *)controller didAuthorizePayment:(PKPayment *)payment completion:(void (^)(PKPaymentAuthorizationStatus))completion {
    CordovaStripe* pluginInstance = [self.viewController getCommandInstance:PLUGIN_NAME];
    if (pluginInstance != nil) {
        // Send token back to plugin
        [pluginInstance processPayment:controller didAuthorizePayment:payment completion:completion];
    } else {
        // Discard payment
        NSLog(@"Unable to get plugin instsnce, discarding payment.");
        completion(PKPaymentAuthorizationStatusFailure);
    }
}

- (void)paymentAuthorizationViewControllerDidFinish:(PKPaymentAuthorizationViewController *)controller {
    
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
}

@end
