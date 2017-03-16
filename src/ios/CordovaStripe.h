#import <Cordova/CDV.h>
@import Stripe;

@interface CordovaStripe : CDVPlugin
@property (nonatomic, retain) STPAPIClient *client;

- (void) setPublishableKey:(CDVInvokedUrlCommand *) command;
- (void) createCardToken:(CDVInvokedUrlCommand *) command;
- (void) validateCardNumber: (CDVInvokedUrlCommand *) command;
- (void) validateExpiryDate: (CDVInvokedUrlCommand *) command;
- (void) validateCVC: (CDVInvokedUrlCommand *) command;
- (void) getCardType: (CDVInvokedUrlCommand *) command;
- (void) createBankAccountToken: (CDVInvokedUrlCommand *) command;

@end
