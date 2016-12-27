#import "CordovaStripe.h"
@import Stripe;

@implementation CordovaStripe

- (void)setPublishableKey:(CDVInvokedUrlCommand*)command
{
    NSString* publishableKey = [[command arguments] objectAtIndex:0];
    NSLog(@"Setting publishable key to %@", publishableKey);
    [[STPPaymentConfiguration sharedConfiguration] setPublishableKey:publishableKey];
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus: CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)greet:(CDVInvokedUrlCommand*)command
{
    
    NSString* name = [[command arguments] objectAtIndex:0];
    NSString* msg = [NSString stringWithFormat: @"Hello, %@", name];
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

@end
