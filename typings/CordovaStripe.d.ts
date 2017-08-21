
interface Window {
    plugin: any;
}

interface CordovaPlugins {
    stripe: ICordovaStripe;
}

interface ICordovaStripe {

    /**
    * Create a credit card token
    * @param creditCard {module:stripe.CreditCardTokenParams} Credit card information
    * @param success {Function} Success callback
    * @param error {Function} Error callback
    */
    createCardToken: (card: ICreateStripeCardTokenRequest, success: (token: IStripeToken) => void, err: (err) => void) => void;

    /**
     * Set publishable key
     * @param key {string} Publishable key
     * @param [success] {Function} Success callback
     * @param [error] {Function} Error callback
     */
    setPublishableKey: (key: string, success?: () => void, error?: (err) => void) => void;

    /**
     * Create a bank account token
     * @param bankAccount {module:stripe.BankAccountTokenParams} Bank account information
     * @param {Function} success Success callback
     * @param {Function} error Error callback
     */
    createBankAccountToken: (bankAccount: ICreateStripeBankAccountTokenRequest, success: (bankAccount: ICreateStripeBankAccountTokenResponse) => void, error: (err) => void) => void;

    /**
     * Validates card number
     * @param cardNumber {String} Credit card number
     * @param {Function} success  Success callback that will be called if card number is valid
     * @param {Function} error  Error callback that will be called if card number is invalid
     */
    validateCardNumber: (cardNumber, success: () => void, error: () => void) => void;

    /**
     * Validates the expiry date of a card
     * @param {number} expMonth Expiry month
     * @param {number} expYear Expiry year
     * @param {Function} success
     * @param {Function} error
     */
    validateExpiryDate: (expMonth: number, expYear: number, success: () => void, error: () => void) => void;

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
    validateCVC: (cvc: string, success: () => void, error: () => void) => void;

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
    getCardType: (cardNumber: string, success: () => void, error: (err) => void) => void;
}

interface ICreateStripeBankAccountTokenRequest {
    country: string,
    currency: string,
    account_holder_name: string,
    account_holder_type: string,
    routing_number: string,
    account_number: stirng
}

interface ICreateStripeCardTokenRequest {
    number: string,
    expMonth: number,
    expYear: number,
    cvc: string,
    name?: string,
    address_line1?: string,
    address_line2?: string,
    address_city?: string,
    address_state?: string,
    address_country?: string,
    postal_code?: string,
    currency?: string
}

interface IStripeToken {
    id: string;
    object: string;
    card: IStripeCardResponse;
    client_ip?: string;
    created: number;
    livemode: boolean;
    type: string;
    used: boolean;
}

interface IStripeCardResponse {
    id: string;
    object: string;
    address_city?: any;
    address_country?: any;
    address_line1?: any;
    address_line1_check?: any;
    address_line2?: any;
    address_state?: any;
    address_zip?: any;
    address_zip_check?: any;
    brand: string;
    country: string;
    cvc_check?: any;
    dynamic_last4?: any;
    exp_month: number;
    exp_year: number;
    fingerprint: string;
    funding: string;
    last4: string;
    metadata: Metadata;
    name?: any;
    tokenization_method?: any;
}

export interface StripeBankAccount {
    id: string;
    object: string;
    account_holder_name: string;
    account_holder_type: string;
    bank_name: string;
    country: string;
    currency: string;
    fingerprint: string;
    last4: string;
    routing_number: string;
    status: string;
}

export interface ICreateStripeBankAccountTokenResponse {
    id: string;
    object: string;
    bank_account: StripeBankAccount;
    client_ip?: any;
    created: number;
    livemode: boolean;
    type: string;
    used: boolean;
}