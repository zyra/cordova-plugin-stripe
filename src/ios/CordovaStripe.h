#import <Cordova/CDV.h>

@interface CordovaStripe : CDVPlugin

- (void) setPublishableKey:(CDVInvokedUrlCommand*)command;
- (void) greet:(CDVInvokedUrlCommand*)command;

@end