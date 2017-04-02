var exec = require('cordova/exec');
var noop = function(){};

/**
 * @namespace cordova.plugins
 */

/**
 * Parameters to create a credit card token
 * @typedef module:stripe.CreditCardTokenParams
 * @type {Object}
 * @property {string} number Card number
 * @property {number} expMonth Expiry month
 * @property {number} expYear Expiry year
 * @property {string} [cvc] CVC/CVV
 * @property {string} [name] Cardholder name
 * @property {string} [address_line1] Address line 1
 * @property {string} [address_line2] Address line 2
 * @property {string} [address_city] Address line 2
 * @property {string} [address_state] State/Province
 * @property {string} [address_country] Country
 * @property {string} [postal_code] Postal/Zip code
 * @property {string} [currency] 3-letter code for currency
 */

/**
 * Parameters to create a bank account token
 * @typedef module:stripe.BankAccountTokenParams
 * @type {object}
 * @property {string} routing_number Routing number
 * @property {string} account_number Account number
 * @property {string} currency Currency code. Example: `CAD`.
 * @property {string} country Country code. Example: `CA`.
 * @property {string} [account_holder_name] Account holder name
 * @property {string} [account_holder_type] Account holder type. This can be `individual` or `company`.
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
     * @param creditCard {module:stripe.CreditCardTokenParams} Credit card information
     * @param success {Function} Success callback
     * @param error {Function} Error callback
     */
    createCardToken: function(creditCard, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "createCardToken", [creditCard]);
    },

    /**
     * Create a bank account token
     * @param bankAccount {module:stripe.BankAccountTokenParams} Bank account information
     * @param {Function} success Success callback
     * @param {Function} error Error callback
     */
    createBankAccountToken: function(bankAccount, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "createBankAccountToken", [bankAccount]);
    },

    /**
     * Validates card number
     * @param cardNumber {String} Credit card number
     * @param {Function} success  Success callback that will be called if card number is valid
     * @param {Function} error  Error callback that will be called if card number is invalid
     */
    validateCardNumber: function(cardNumber, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "validateCardNumber", [cardNumber]);
    },

    /**
     * Validates the expiry date of a card
     * @param {number} expMonth Expiry month
     * @param {number} expYear Expiry year
     * @param {Function} success
     * @param {Function} error
     */
    validateExpiryDate: function(expMonth, expYear, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "validateExpiryDate", [expMonth, expYear]);
    },

    /**
     * Validates a CVC of a card
     * @param {string} cvc CVC/CVV
     * @param {Function} success
     * @param {Function} error
     * @example
     * function onSuccess() {
     *   console.log('isValid');
     * }
     *
     * function onError() {
     *   console.log('invalid');
     * }
     *
     * cordova.plugin.stripe.validateCVC('424', onSuccess, onError);
     */
    validateCVC: function(cvc, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "validateCVC", [cvc]);
    },

    /**
     * Gets a card type from a card number
     * @param {string} cardNumber Credit card number
     * @param {Function} success
     * @param {Function} error
     * @example
     * cordova.plugins.stripe.getCardType('4242424242424242', function(cardType) {
     *   console.log(cardType); // visa
     * });
     */
    getCardType: function(cardNumber, success, error) {
        success = success || noop;
        error = error || noop;
        exec(success, error, "CordovaStripe", "getCardType", [cardNumber]);
    }

};