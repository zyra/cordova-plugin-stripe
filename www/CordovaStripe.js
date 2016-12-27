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

    createCardToken: function(creditCard, success, error) {
        exec(success, error, "CordovaStripe", "createCardToken", trimArgs(arguments));
    }

};