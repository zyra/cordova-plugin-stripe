#import <Cordova/CDV.h>

@interface CordovaStripe : CDVPlugin

- (void) setPublishableKey:(CDVInvokedUrlCommand*)command;
- (void) createCardToken:(CDVInvokedUrlCommand*)command;

@end
