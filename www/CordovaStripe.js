"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var cordova_1 = require("cordova");
var NOOP = function () { };
var CordovaStripe;
(function (CordovaStripe) {
    var SourceType;
    (function (SourceType) {
        SourceType["ThreeDeeSecure"] = "3ds";
        SourceType["GiroPay"] = "giropay";
        SourceType["iDEAL"] = "ideal";
        SourceType["SEPADebit"] = "sepadebit";
        SourceType["Sofort"] = "sofort";
        SourceType["AliPay"] = "alipay";
        SourceType["AliPayReusable"] = "alipayreusable";
        SourceType["P24"] = "p24";
        SourceType["VisaCheckout"] = "visacheckout";
    })(SourceType = CordovaStripe.SourceType || (CordovaStripe.SourceType = {}));
    var SourceTypeArray = Object.keys(SourceType).map(function (key) { return SourceType[key]; });
    var Plugin = /** @class */ (function () {
        function Plugin() {
        }
        /**
         * Set publishable key
         * @param {string} key
         * @param {Function} success
         * @param {Function} error
         */
        Plugin.setPublishableKey = function (key, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'setPublishableKey', [key]);
        };
        /**
         * Create a credit card token
         * @param {CordovaStripe.CardTokenRequest} creditCard
         * @param {CordovaStripe.CardTokenCallback} success
         * @param {CordovaStripe.ErrorCallback} error
         */
        Plugin.createCardToken = function (creditCard, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'createCardToken', [creditCard]);
        };
        /**
         * Create a bank account token
         * @param {CordovaStripe.BankAccountTokenRequest} bankAccount
         * @param {Function} success
         * @param {Function} error
         */
        Plugin.createBankAccountToken = function (bankAccount, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'createBankAccountToken', [bankAccount]);
        };
        /**
         * Validates card number
         * @param cardNumber Card number
         * @param {(isValid: boolean) => void} [success]
         * @param {Function} [error]
         */
        Plugin.validateCardNumber = function (cardNumber, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'validateCardNumber', [cardNumber]);
        };
        /**
         * Validates the expiry date of a card
         * @param {number} expMonth
         * @param {number} expYear
         * @param {(isValid: boolean) => void} [success]
         * @param {Function} [error]
         */
        Plugin.validateExpiryDate = function (expMonth, expYear, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'validateExpiryDate', [expMonth, expYear]);
        };
        /**
         * Validates a CVC of a card
         * @param {string} cvc
         * @param {(isValid: boolean) => void} [success]
         * @param {Function} [error]
         */
        Plugin.validateCVC = function (cvc, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'validateCVC', [cvc]);
        };
        /**
         * Gets a card type from a card number
         * @param {string | number} cardNumber
         * @param {(type: string) => void} [success]
         * @param {Function} [error]
         */
        Plugin.getCardType = function (cardNumber, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'getCardType', [String(cardNumber)]);
        };
        /**
         * Pay with ApplePay
         * @param {CordovaStripe.ApplePayOptions} options
         * @param {(token: string, callback: (paymentProcessed: boolean) => void) => void} success
         * @param {Function} error
         */
        Plugin.payWithApplePay = function (options, success, error) {
            if (error === void 0) { error = NOOP; }
            if (!options || !options.merchantId || !options.country || !options.currency || !options.items || !options.items.length) {
                error({
                    message: 'Missing one or more payment options.'
                });
                return;
            }
            options.items = options.items.map(function (item) {
                item.amount = String(item.amount);
                return item;
            });
            cordova_1.exec(function (token) {
                success(token, function (paymentProcessed) {
                    cordova_1.exec(NOOP, NOOP, 'CordovaStripe', 'finalizeApplePayTransaction', [Boolean(paymentProcessed)]);
                });
            }, error, 'CordovaStripe', 'initializeApplePayTransaction', [
                options.merchantId,
                options.country,
                options.currency,
                options.items
            ]);
        };
        Plugin.initGooglePay = function (success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'initGooglePay');
        };
        Plugin.payWithGooglePay = function (options, success, error) {
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'payWithGooglePay', [options.amount, options.currencyCode]);
        };
        Plugin.createSource = function (type, params, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'createSource', [SourceTypeArray.indexOf(type.toLowerCase()), params]);
        };
        Plugin.createPiiToken = function (personalId, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'createPiiToken', [personalId]);
        };
        Plugin.createAccountToken = function (accountParams, success, error) {
            if (success === void 0) { success = NOOP; }
            if (error === void 0) { error = NOOP; }
            cordova_1.exec(success, error, 'CordovaStripe', 'createAccountToken', [accountParams]);
        };
        return Plugin;
    }());
    CordovaStripe.Plugin = Plugin;
})(CordovaStripe = exports.CordovaStripe || (exports.CordovaStripe = {}));
