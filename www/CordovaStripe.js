var exec = require('cordova/exec');

function trimArgs(args) {
    args = Array.from(args);
    args.splice(args.length-2, 2);
    return args;
}

module.exports = {

    /**
     * Set publishable key
     * @param key {string} Publishable key
     * @param success {Function} Success callback
     * @param error {Function} Error callback
     */
    setPublishableKey: function(key, success, error) {
        exec(success, error, "CordovaStripe", "setPublishableKey", trimArgs(arguments));
    },

    /**
     * Create a credit card token
     * @param creditCard {Object} Credit card information
     * @param success {Function} Success callback
     * @param error {Function} Error callback
     */
    createCardToken: function(creditCard, success, error) {
        exec(success, error, "CordovaStripe", "createCardToken", trimArgs(arguments));
    }

};