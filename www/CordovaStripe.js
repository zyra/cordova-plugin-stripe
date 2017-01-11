var exec = require('cordova/exec');

module.exports = {

    /**
     * Set publishable key
     * @param key {string} Publishable key
     * @param success {Function} Success callback
     * @param error {Function} Error callback
     */
    setPublishableKey: function(key, success, error) {
        exec(success, error, "CordovaStripe", "setPublishableKey", [key]);
    },

    /**
     * Create a credit card token
     * @param creditCard {Object} Credit card information
     * @param success {Function} Success callback
     * @param error {Function} Error callback
     */
    createCardToken: function(creditCard, success, error) {
        exec(success, error, "CordovaStripe", "createCardToken", [creditCard]);
    }

};