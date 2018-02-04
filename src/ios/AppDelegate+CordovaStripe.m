#import "AppDelegate+CordovaStripe.h"
#import "CordovaStripe.h"
@import Stripe;

@implementation AppDelegate (CordovaStripe)
static NSString* const PLUGIN_NAME = @"CordovaStripe";
- (void)paymentAuthorizationViewController:(PKPaymentAuthorizationViewController *)controller didAuthorizePayment:(PKPayment *)payment completion:(void (^)(PKPaymentAuthorizationStatus))completion {
    CordovaStripe* pluginInstance = [self.viewController getCommandInstance:PLUGIN_NAME];
    if (pluginInstance != nil) {
        // Send token back to plugin
        pluginInstance.processPayment(controller, payment, completion);
    } else {
        // Discard payment
    }
    
    [[STPAPIClient sharedClient] createTokenWithPayment:payment completion:^(STPToken *token, NSError *error) {
        if (token == nil || error != nil) {
            // Present error to user...
            return;
        }
        
        CordovaStripe* pluginInstance = [self.viewController getCommandInstance:PLUGIN_NAME];
        if (pluginInstance != nil) {
            // Send token back to plugin
        }
        
//        [self submitTokenToBackend:token completion:^(NSError *error) {
//            if (error) {
//                // Present error to user...
//
//                // Notify payment authorization view controller
//                completion(PKPaymentAuthorizationStatusFailure);
//            }
//            else {
//                // Save payment success
//                self.paymentSuceeded = YES;
//
//                // Notify payment authorization view controller
//                completion(PKPaymentAuthorizationStatusSuccess);
//            }
//        }];
    }];
}

- (void)paymentAuthorizationViewControllerDidFinish:(PKPaymentAuthorizationViewController *)controller {
    
}

@end
