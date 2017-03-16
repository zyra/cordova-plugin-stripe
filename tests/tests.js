var onError = function(done) {
    return function(e) {
        done('Error', e);
    }
};
exports.defineAutoTests = function() {

    describe('Stripe (cordova.plugins.stripe)', function() {

        it('should exist', function() {
            expect(cordova.plugins.stripe).toBeDefined();
        });

        it('should set publishable key', function(done) {
           cordova.plugins.stripe.setPublishableKey('pk_test_Nb1qGaMaOyoxlzHrTyaB2Le7', done, done);
        });

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
           }, done);
        });

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
            }, done);
        });

        it('should validate card number', function(done) {
            stripe.validateCardNumber('4242424242424242', done, onError);
        });

        it('should validate CVC', function(done) {
            stripe.validateCVC('242', done, onError);
        });

        it('should validate expiry date', function(done) {
            stripe.validateExpiryDate('12', '2019', done, onError);
        });

        it('should get visa card type', function(done) {
            stripe.getCardType('4242424242424242', function(type) {
                expect(type).toBe('Visa');
                done();
            });
        });

        it('should get master card type', function(done) {
            stripe.getCardType('5555555555554444', function(type) {
                expect(type).toBe('MasterCard');
                done();
            });
        });

        it('should get amex card type', function(done) {
            stripe.getCardType('378282246310005', function(type) {
                expect(type).toBe('American Express');
                done();
            });
        });

        it('should get discover card type', function(done) {
            stripe.getCardType('6011111111111117', function(type) {
                expect(type).toBe('Discover');
                done();
            });
        });

        it('should get diners club card type', function(done) {
            stripe.getCardType('30569309025904', function(type) {
                expect(type).toBe('Diners');
                done();
            });
        });

        it('should get JCB card type', function(done) {
            stripe.getCardType('3530111333300000', function(type) {
                expect(type).toBe('JCB');
                done();
            });
        });

    });

};