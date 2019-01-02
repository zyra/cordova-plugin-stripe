#import <Cordova/CDV.h>
#import "BongloyAPIClient.h"

@interface CordovaStripe : CDVPlugin
@property (nonatomic, retain) BongloyAPIClient *client;

- (void) setPublishableKey:(CDVInvokedUrlCommand *) command;
- (void) createCardToken:(CDVInvokedUrlCommand *) command;
- (void) validateCardNumber: (CDVInvokedUrlCommand *) command;
- (void) validateExpiryDate: (CDVInvokedUrlCommand *) command;
- (void) validateCVC: (CDVInvokedUrlCommand *) command;
- (void) getCardType: (CDVInvokedUrlCommand *) command;
- (void) createBankAccountToken: (CDVInvokedUrlCommand *) command;

@end
