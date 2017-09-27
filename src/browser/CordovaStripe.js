// injects Stripe.js into the browser
var secureStripeScript = document.createElement('script');
secureStripeScript.setAttribute('src','https://js.stripe.com/v2/');
document.getElementsByTagName('head')[0].appendChild(secureStripeScript);

var stripe = {

  setPublishableKey: function(successCallback, errorCallback, args) {
    try {
      Stripe.setPublishableKey(args[0]);
      successCallback();
    } catch (error) {
      errorCallback(error);
    }
  },

  createCardToken: function(successCallback, errorCallback, args) {
    mapNativeToJS(args[0]);
    try {
      Stripe.card.createToken(args[0], function(status, response){
        if(response.error){
          errorCallback(response.error);
        } else {
          successCallback(response);
        }
      });
    } catch (error) {
      errorCallback(error);
    }
  },

  createBankAccountToken: function(successCallback, errorCallback, args) {
    Stripe.bankAccount.createToken(args[0], function(status, response){
      if(response.error){
        errorCallback(response.error);
      } else {
        successCallback(response);
      }
    });
  },

  validateCardNumber: function(successCallback, errorCallback, args) {
    if (Stripe.card.validateCardNumber(args[0])) {
      successCallback();
    } else {
      errorCallback('Invalid card number');
    }
  },

  validateExpiryDate: function(successCallback, errorCallback, args) {
    if (Stripe.card.validateExpiry(args[0], args[1])) {
      successCallback();
    } else {
      errorCallback('Invalid expiry date');
    }
  },

  validateCVC: function(successCallback, errorCallback, args) {
    if (Stripe.card.validateCVC(args[0])) {
      successCallback();
    } else {
      errorCallback('Invalid CVC');
    }
  },

  getCardType: function(successCallback, errorCallback, args) {
    successCallback(Stripe.card.cardType(args[0]));
  }



};

function mapNativeToJS (arg) {
  /**
   * Add any other API discrepancies between Native SDK and StripJS
   * here
   */

  /**
   * In the Native SDK 'postal_code' is the key used to represent zip code.
   * In StripeJS however, 'address_zip' is the key used. Map 'postal_code'
   * to 'address_zip' here to avoid 400 (Bad Request) from Stripe's API
   */
  if (arg.postalCode) {
    arg.address_zip = arg.postalCode;
    delete arg.postalCode;
  }
}

require('cordova/exec/proxy').add('CordovaStripe', stripe);