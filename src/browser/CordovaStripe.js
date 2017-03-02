// injects Stripe.js into the browser
var secureStripeScript = document.createElement('script');
secureStripeScript.setAttribute('src','https://js.stripe.com/v2/');
document.getElementsByTagName('head')[0].appendChild(secureStripeScript);

module.exports = {

    setPublishableKey: function(key, successCallback, errorCallback){
        try {
            Stripe.setPublishableKey(key);
            successCallback();
        } catch (error) {
            errorCallback(error);
        }
    },

    createCardToken: function(cardObject, successCallback, errorCallback){
        try {
            Stripe.card.createToken(cardObject,function(status,response){
                if(response.error){
                    errorCallback(response.error);
                } else {
                    successCallback(response.id);
                }
            })
        } catch (error) {
            errorCallback(error);
        }
    },

    validateCardNumber: function(cardNumber, successCallback, errorCallback) {
        if (Stripe.card.validateCardNumber(cardNumber)) {
            successCallback();
        } else {
            errorCallback('Invalid card number');
        }
    },

    validateExpiryDate: function(expMonth, expYear, successCallback, errorCallback) {
        if (Stripe.card.validateExpiry(expMonth, expYear)) {
            successCallback();
        } else {
            errorCallback('Invalid expiry date');
        }
    },

    validateCVC: function(cvc, successCallback, errorCallback) {
        if (Stripe.card.validateCVC(cvc)) {
            successCallback();
        } else {
            errorCallback('Invalid CVC');
        }
    },

    getCardType: function(cardNumber, successCallback) {
        successCallback(Stripe.card.cardType(cardNumber));
    }



};

require('cordova/exec/proxy').add('CordovaStripe', module.exports);