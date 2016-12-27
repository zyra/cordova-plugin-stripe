var exec = require('cordova/exec');

function trimArgs(args) {
    args = Array.from(args);
    args.splice(args.length-2, 2);
    return args;
}

module.exports = {

    setPublishableKey: function(key, success, error) {
        exec(success, error, "CordovaStripe", "setPublishableKey", trimArgs(arguments));
    },

    validateCardNumber: function(cardNumber, success, error) {
        exec(success, error, "CordovaStripe", "validateCardNumber", trimArgs(arguments));
    },

    validateExpiry: function(month, year, success, error) {
        exec(success, error, "CordovaStripe", "validateExpiry", trimArgs(arguments));
    },

    validateCVC: function(cvc, success, error) {
        exec(success, error, "CordovaStripe", "validateCVC", trimArgs(arguments));
    },

    createCardToken: function(creditCard, success, error) {
        exec(success, error, "CordovaStripe", "createCardToken", trimArgs(arguments));
    }

};