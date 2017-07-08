#import "CordovaStripe.h"
@import Stripe;

@implementation CordovaStripe

@synthesize client;

- (void)setPublishableKey:(CDVInvokedUrlCommand*)command
{
    
    NSString* publishableKey = [[command arguments] objectAtIndex:0];
    [[STPPaymentConfiguration sharedConfiguration] setPublishableKey:publishableKey];
    
    if (self.client == nil) {
        // init client if doesn't exist
        client = [[STPAPIClient alloc] init];
    } else {
        [self.client setPublishableKey:publishableKey];
    }
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus: CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    
}

- (void)throwNotInitializedError:(CDVInvokedUrlCommand *) command
{
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"You must call setPublishableKey method before executing this command."];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void (^)(STPToken * _Nullable token, NSError * _Nullable error))handleTokenCallback: (CDVInvokedUrlCommand *) command
{
    return ^(STPToken * _Nullable token, NSError * _Nullable error) {
        CDVPluginResult* result;
        if (error != nil) {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: error.localizedDescription];
        } else {
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:token.allResponseFields];
        }
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    };
}



- (void)createCardToken:(CDVInvokedUrlCommand *)command
{
    if (self.client == nil) {
        [self throwNotInitializedError:command];
        return;
    }
    
    [self.commandDelegate runInBackground:^{
        
        NSDictionary* const cardInfo = [[command arguments] objectAtIndex:0];
        
        STPCardParams* cardParams = [[STPCardParams alloc] init];
        
        STPAddress* address = [[STPAddress alloc] init];
        address.line1 = cardInfo[@"address_line1"];
        address.line2 = cardInfo[@"address_line2"];
        address.city = cardInfo[@"address_city"];
        address.state = cardInfo[@"address_state"];
        address.country = cardInfo[@"address_country"];
        address.postalCode = cardInfo[@"postalCode"];
        
        cardParams.address = address;
        
        cardParams.number = cardInfo[@"number"];
        cardParams.expMonth = [cardInfo[@"expMonth"] intValue];
        cardParams.expYear = [cardInfo[@"expYear"] intValue];
        cardParams.cvc = cardInfo[@"cvc"];
        cardParams.name = cardInfo[@"name"];
        cardParams.currency = cardInfo[@"currency"];
        
        [self.client createTokenWithCard:cardParams completion:[self handleTokenCallback:command]];
        
    }];
    
}

- (void) createBankAccountToken:(CDVInvokedUrlCommand *)command
{
    if (self.client == nil) {
        [self throwNotInitializedError:command];
        return;
    }

    
    [self.commandDelegate runInBackground:^{
        
        NSDictionary* const bankAccountInfo = [command.arguments objectAtIndex:0];
        STPBankAccountParams* params = [[STPBankAccountParams alloc] init];
        
        params.accountNumber = bankAccountInfo[@"account_number"];
        params.country = bankAccountInfo[@"country"];
        params.currency = bankAccountInfo[@"currency"];
        params.routingNumber = bankAccountInfo[@"routing_number"];
        params.accountHolderName = bankAccountInfo[@"account_holder_name"];
        
        NSString* accountType = bankAccountInfo[@"account_holder_type"];
        if ([accountType  isEqualToString: @"individual"]) {
            params.accountHolderType = STPBankAccountHolderTypeIndividual;
        } else if([accountType isEqualToString: @"company"]) {
            params.accountHolderType = STPBankAccountHolderTypeCompany;
        }
        
        [self.client createTokenWithBankAccount:params completion:[self handleTokenCallback:command]];
        
    }];
    
}

- (void)validateCardNumber:(CDVInvokedUrlCommand *)command
{
    CDVCommandStatus status;
    STPCardValidationState state = [STPCardValidator validationStateForNumber:[command.arguments objectAtIndex:0] validatingCardBrand:YES];
    
    if (state == STPCardValidationStateValid) {
        status = CDVCommandStatus_OK;
    } else {
        status = CDVCommandStatus_ERROR;
    }
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:status];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)validateExpiryDate:(CDVInvokedUrlCommand *)command
{
    CDVCommandStatus status;
    NSString *expMonth = [command.arguments objectAtIndex:0];
    NSString *expYear = [command.arguments objectAtIndex:1];
    
    if (expYear.length == 4) {
        expYear = [expYear substringFromIndex:2];
    }
    
    STPCardValidationState state = [STPCardValidator validationStateForExpirationYear:expYear inMonth:expMonth];
    
    if (state == STPCardValidationStateValid) {
        status = CDVCommandStatus_OK;
    } else {
        status = CDVCommandStatus_ERROR;
    }
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:status];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)validateCVC:(CDVInvokedUrlCommand *)command
{
    CDVCommandStatus status;
    STPCardValidationState state = [STPCardValidator validationStateForCVC:[command.arguments objectAtIndex:0] cardBrand:STPCardBrandUnknown];
    
    if (state == STPCardValidationStateValid) {
        status = CDVCommandStatus_OK;
    } else {
        status = CDVCommandStatus_ERROR;
    }
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:status];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)getCardType:(CDVInvokedUrlCommand *)command
{
    STPCardBrand brand = [STPCardValidator brandForNumber:[command.arguments objectAtIndex:0]];
    NSArray *brands =  [[NSArray alloc] initWithObjects: @"Visa", @"American Express", @"MasterCard", @"Discover", @"JCB", @"Diners Club", @"Unknown", nil];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:brands[brand]];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

@end
