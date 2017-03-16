var onSuccess = function(done) {
    return function() {
        expect(true).toBe(true);
        done();
    }
}
var onError = function(done) {
    return function(e) {
        if (e) {
            expect(false).toBe(true, e);
        } else {
            expect(false).toBe(true);
        }
        done();
    }
};
exports.defineAutoTests = function() {

    describe('Stripe (cordova.plugins.stripe)', function() {

        it('should exist', function() {
            expect(cordova.plugins.stripe).toBeDefined();
        });

        describe('setPublishableKey', function() {

            it('should exist', function() {
               expect(typeof cordova.plugins.stripe.setPublishableKey).toBe('function');
            });

            it('should set publishable key', function(done) {
                cordova.plugins.stripe.setPublishableKey('pk_test_Nb1qGaMaOyoxlzHrTyaB2Le7', onSuccess(done), onError(done));
            });

        });

        describe('createCardToken', function() {

            it('should create a card token', function(done) {
                var card = {
                    number: '4242424242424242',
                    cvc: '242',
                    expMonth: 12,
                    expYear: 2025
                };

                cordova.plugins.stripe.createCardToken(card, function(token) {
                    expect(token).toBeDefined();
                    done();
                }, onError(done));
            });

        });

        describe('createBankAccountToken', function() {

            it('should create a bank account token', function(done) {
                var bankAccount = {
                    routing_number: '11000000',
                    account_number: '000123456789',
                    account_holder_name: 'Ibby Hadeed',
                    account_holder_type: 'individual',
                    currency: 'CAD',
                    country_code: 'CA'
                };

                cordova.plugins.stripe.createBankAccountToken(bankAccount, function(token) {
                    expect(token).toBeDefined();
                    done();
                }, onError(done));
            });

        });

        describe('validateCardNumber', function() {

            it('should be defined', function() {
               expect(typeof cordova.plugins.stripe.validateCardNumber).toBe('function');
            });

            it('should accept valid card number', function(done) {
                cordova.plugins.stripe.validateCardNumber('4242424242424242', onSuccess(done), onError(done));
            });

            it('should reject invalid card number', function(done) {
                cordova.plugins.stripe.validateCardNumber('123123', onError(done), onSuccess(done));
            });

        });

        describe('validateCVC', function() {

            it('should be defined', function() {
                expect(typeof cordova.plugins.stripe.validateCVC).toBe('function');
            });


            it('should accept valid CVC', function(done) {
                cordova.plugins.stripe.validateCVC('242', onSuccess(done), onError(done));
            });

            it('should reject invalid CVC', function(done) {
               cordova.plugins.stripe.validateCVC('123123123', onError(done), onSuccess(done));
            });

        });

        describe('validateExpiryDate', function() {

            it('should be defined', function() {
                expect(typeof cordova.plugins.stripe.validateExpiryDate).toBe('function');
            });


            it('should accept valid expiry date', function(done) {
                cordova.plugins.stripe.validateExpiryDate('12', '2025', onSuccess(done), onError(done));
            });

            it('should reject invalid expiry date', function(done) {
                cordova.plugins.stripe.validateExpiryDate('12', '2015', onError(done), onSuccess(done));
            });

        });

        describe('getCardType', function() {

            it('should be defined', function() {
                expect(typeof cordova.plugins.stripe.getCardType).toBe('function');
            });


            it('should return Visa', function(done) {
                cordova.plugins.stripe.getCardType('4242424242424242', function(type) {
                    expect(type).toBe('Visa');
                    done();
                });
            });

            it('should return MasterCard', function(done) {
                cordova.plugins.stripe.getCardType('5555555555554444', function(type) {
                    expect(type).toBe('MasterCard');
                    done();
                });
            });

            it('should return American Express', function(done) {
                cordova.plugins.stripe.getCardType('378282246310005', function(type) {
                    expect(type).toBe('American Express');
                    done();
                });
            });

            it('should return Discover', function(done) {
                cordova.plugins.stripe.getCardType('6011111111111117', function(type) {
                    expect(type).toBe('Discover');
                    done();
                });
            });

            it('should return Diners Club', function(done) {
                cordova.plugins.stripe.getCardType('30569309025904', function(type) {
                    expect(type).toBe('Diners Club');
                    done();
                });
            });

            it('should return JCB', function(done) {
                cordova.plugins.stripe.getCardType('3530111333300000', function(type) {
                    expect(type).toBe('JCB');
                    done();
                });
            });

        });

    });

};