var exec = require('cordova/exec');
var noop = function(){};

/**
 * @namespace cordova.plugins
 */

/**
 * Parameters to create a credit card token
 * @typedef module:stripe.CardTokenParams
 * @type {Object}
 * @property {string} number Card number
 * @property {number} expMonth Expiry month
 * @property {number} expYear Expiry year
 * @property {string} [cvc] CVC/CVV
 * @property {string} [name] Cardholder name
 * @property {string} [address_line1] Address line 1
 * @property {string} [address_line2] Address line 2
 */

/**
 * @exports stripe
 */
module.exports = {

    /**
     * Set publishable key
     * @param key {string} Publishable key
     * @param [success] {Function} Success callback
     * @param [error] {Function} Error callback
     */
    setPublishableKey: function(key, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "setPublishableKey", [key]);
    },

    /**
     * Create a credit card token
     * @param creditCard {module:stripe.CardTokenParams} Credit card information
     * @param [success] {Function} Success callback
     * @param [error] {Function} Error callback
     */
    createCardToken: function(creditCard, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "createCardToken", [creditCard]);
    },

    /**
     * Create a bank account token
     * @param bankAccount {Object} Bank account information
     * @param success {Function} Success callback
     * @param error {Function} Error callback
     */
    createBankAccountToken: function(bankAccount, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "createBankAccountToken", [bankAccount]);
    },

    /**
     * Validates card number
     * @param cardNumber {String} Credit card number
     * @param success {Function} Success callback that will be called if card number is valid
     * @param error {Function} Error callback that will be called if card number is invalid
     */
    validateCardNumber: function(cardNumber, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "validateCardNumber", [cardNumber]);
    },

    validateExpiryDate: function(expMonth, expYear, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "validateExpiryDate", [expMonth, expYear]);
    },

    validateCVC: function(cvc, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "validateCVC", [cvc]);
    },

    getCardType: function(cardNumber, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "getCardType", [cardNumber]);
    }

};