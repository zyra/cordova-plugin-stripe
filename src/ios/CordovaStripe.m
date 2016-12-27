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

- (void)createCardToken:(CDVInvokedUrlCommand *)command
{
    
    NSDictionary* cardInfo = [[command arguments] objectAtIndex:0];
    
    STPCardParams* cardParams = [[STPCardParams alloc] init];
    
    cardParams.number = cardInfo[@"number"];
    cardParams.expMonth = [cardInfo[@"expMonth"] intValue];
    cardParams.expYear = [cardInfo[@"expYear"] intValue];
    cardParams.cvc = cardInfo[@"cvc"];
    cardParams.name = cardInfo[@"name"];
    cardParams.currency = cardInfo[@"currency"];
    
    STPAddress* address = [[STPAddress alloc] init];
    address.line1 = cardInfo[@"address_line1"];
    address.line2 = cardInfo[@"address_line2"];
    address.city = cardInfo[@"address_city"];
    address.state = cardInfo[@"address_state"];
    address.postalCode = cardInfo[@"postalCode"];
    address.country = cardInfo[@"address_country"];
    
    cardParams.address = address;
    
    STPAPIClient* client = [[STPAPIClient alloc] init];
    [client createTokenWithCard:cardParams completion:^(STPToken * _Nullable token, NSError * _Nullable error) {
        CDVPluginResult* result;
        if (error != nil) {
             result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: error.localizedDescription];
        } else {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:token.tokenId];
        }
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
    
}

@end
