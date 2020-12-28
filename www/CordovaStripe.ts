import { exec } from 'cordova';


const execP = <Opts, Response>(methodName: string, opts?: Opts): Promise<Response> => {
  return new Promise<Response>((resolve, reject) => {
    exec(resolve, reject, 'CordovaStripe', methodName, [opts]);
  });
};

const NOOP: any = () => {
};

export interface Window {
  cordova: Cordova;
}

export interface Cordova {
  plugins: CordovaPlugins;
}

export interface CordovaPlugins {
  stripe: typeof CordovaStripe.Plugin;
}

export namespace CordovaStripe {

  export interface CommonIntentOptions {
    clientSecret: string;
    /**
     * If provided, the payment intent will be confirmed using this card as a payment method.
     */
    card?: Card;
    /**
     * If provided, the payment intent will be confirmed using this payment method
     */
    paymentMethodId?: string;
    redirectUrl: string;
  }

  export type ConfirmSetupIntentOptions = CommonIntentOptions;

  export interface ConfirmPaymentIntentOptions extends CommonIntentOptions {
    /**
     * Whether you intend to save the payment method to the customer's account after this payment
     */
    saveMethod?: boolean;
    /**
     * If provided, the payment intent will be confirmed using a card provided by Apple Pay
     */
    applePayOptions?: ApplePayOptions;

    /**
     * If provided, the payment intent will be confirmed using a card provided by Google Pay
     */
    googlePayOptions?: GooglePayOptions;
  }

  export type SetPublishableKeyOptions = {
    key: string
  };

  export type ValidateCardNumberOptions = {
    number: string
  };

  export type ValidateExpiryDateOptions = {
    exp_month: number,
    exp_year: number
  };

  export type ValidateCVCOptions = {
    cvc: string
  };

  export type IdentifyCardBrandOptions = {
    number: string
  };

  export type CreatePiiTokenOptions = {
    pii: string
  };

  export type CreateSourceTokenOptions = {
    type: SourceType,
    params: SourceParams
  };

  export type FinalizeApplePayTransactionOptions = {
    success: boolean
  };

  export type ValidityResponse = { valid: boolean }
  export type AvailabilityResponse = { available: boolean }

  export type CardBrandResponse = { brand: CardBrand };

  export interface PaymentMethod {
    created?: number;
    customerId?: string;
    id?: string;
    livemode: boolean;
    type?: string;
    card?: Card;
  }

  export enum UIButtonType {
    SUBMIT = 'submit',
    CONTINUE = 'continue',
    NEXT = 'next',
    CANCEL = 'cancel',
    RESEND = 'resend',
    SELECT = 'select'
  }

  export interface UIButtonCustomizationOptions {
    type: UIButtonType;
    backgroundColor?: string;
    textColor?: string;
    fontName?: string;
    cornerRadius?: number;
    fontSize?: number;
  }

  export interface UICustomizationOptions {
    accentColor?: string;
    buttonCustomizations?: UIButtonCustomizationOptions[];
  }

  export interface BankAccount {
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

  export interface Card {
    id: string;
    object: string;
    address_city: any;
    address_country: any;
    address_line1: any;
    address_line1_check: any;
    address_line2: any;
    address_state: any;
    address_zip: any;
    address_zip_check: any;
    brand: CardBrand;
    country: string;
    cvc_check: any;
    dynamic_last4: any;
    exp_month: number;
    exp_year: number;
    fingerprint: string;
    funding: string;
    last4: string;
    metadata: any;
    name: any;
    tokenization_method: any;
    phone: string;
    email: string;
  }

  export interface BankAccountTokenRequest {
    country: string;
    currency: string;
    account_holder_name: string;
    account_holder_type: string;
    routing_number: string;
    account_number: string;
  }

  export interface BankAccountTokenResponse extends TokenResponse {
    bank_account: BankAccount;
  }

  export interface CardTokenRequest {
    number: string;
    exp_month: number;
    exp_year: number;
    cvc: string;
    name?: string;
    address_line1?: string;
    address_line2?: string;
    address_city?: string;
    address_state?: string;
    address_country?: string;
    address_zip?: string;
    currency?: string;
    /**
     * iOS only
     */
    phone?: string;
    /**
     * iOS only
     */
    email?: string;
  }

  export interface CardTokenResponse extends TokenResponse {
    card: Card;
  }

  export interface TokenResponse {
    id: string;
    type: string;
    created: Date;
  }

  export interface ApplePayItem {
    label: string;
    amount: number | string;
  }

  export interface ApplePayOptions {
    merchantId: string;
    country: string;
    currency: string;
    items: ApplePayItem[];
  }

  export interface GooglePayOptions {
    allowedCardNetworks: CardBrand[];
    allowedAuthMethods: Array<'PAN_ONLY' | 'CRYPTOGRAM_3DS'>;
    totalPrice: string;
    totalPriceStatus: 'final';
    currencyCode: string;
    merchantName: string;
    emailRequired?: boolean;
    allowPrepaidCards?: boolean;
    billingAddressRequired?: boolean;
    billingAddressParams?: {
      format?: 'MIN'; // TODO copy from google
      phoneNumberRequired?: boolean;
    }
    shippingAddressRequired?: boolean;
    shippingAddressParameters?: {
      // TODO copy form google
    };
  }

  export interface ThreeDeeSecureParams {
    /**
     * Amount
     */
    amount: number;
    /**
     * Currency code
     */
    currency: string;
    /**
     * URL to redirect to after successfully verifying the card
     */
    returnURL: string;
    /**
     * Card source ID
     */
    card: string;
  }

  export interface GiroPayParams {
    amount: number;
    name: string;
    returnURL: string;
    statementDescriptor: string;
  }

  export interface iDEALParams {
    amount: number;
    name: string;
    returnURL: string;
    statementDescriptor: string;
    bank: string;
  }

  export interface SEPADebitParams {
    name: string;
    iban: string;
    addressLine1: string;
    city: string;
    postalCode: string;
    country: string;
  }

  export interface SofortParams {
    amount: number;
    returnURL: string;
    country: string;
    statementDescriptor: string;
  }

  export interface AlipayParams {
    amount: number;
    currency: string;
    returnURL: string;
  }

  export interface AlipayReusableParams {
    currency: string;
    returnURL: string;
  }

  export interface P24Params {
    amount: number;
    currency: string;
    email: string;
    name: string;
    returnURL: string;
  }

  export interface VisaCheckoutParams {
    callId: string;
  }

  export type SourceParams =
    ThreeDeeSecureParams
    | GiroPayParams
    | iDEALParams
    | SEPADebitParams
    | SofortParams
    | AlipayParams
    | AlipayReusableParams
    | P24Params
    | VisaCheckoutParams;

  export enum SourceType {
    ThreeDeeSecure = '3ds',
    GiroPay = 'giropay',
    iDEAL = 'ideal',
    SEPADebit = 'sepadebit',
    Sofort = 'sofort',
    AliPay = 'alipay',
    AliPayReusable = 'alipayreusable',
    P24 = 'p24',
    VisaCheckout = 'visacheckout',
  }

  export enum CardBrand {
    AMERICAN_EXPRESS = 'AMERICAN_EXPRESS',
    DISCOVER = 'DISCOVER',
    JCB = 'JCB',
    DINERS_CLUB = 'DINERS_CLUB',
    VISA = 'VISA',
    MASTERCARD = 'MASTERCARD',
    UNIONPAY = 'UNIONPAY',
    UNKNOWN = 'UNKNOWN'
  }

  const SourceTypeArray: SourceType[] = Object.keys(SourceType).map((key: any) => SourceType[key] as any as SourceType);

  export interface Address {
    line1: string;
    line2: string;
    city: string;
    postal_code: string;
    state: string;
    country: string;
  }

  export interface LegalEntity {
    address?: Address;
    dob?: {
      day: number;
      month: number;
      year: number;
    },
    first_name?: string;
    last_name?: string;
    gender?: 'male' | 'female';
    personal_address?: Address;
    business_name?: string;
    business_url?: string;
    business_tax_id_provided?: boolean;
    business_vat_id_provided?: string;
    country?: string;
    tos_acceptance?: {
      date: number;
      ip: string;
    },
    personal_id_number_provided?: boolean;
    phone_number?: string;
    ssn_last_4_provided?: boolean;
    tax_id_registrar?: string;
    type?: 'individual' | 'company';
    verification?: any;
  }

  export interface AccountParams {
    tosShownAndAccepted: boolean;
    legalEntity: LegalEntity;
  }

  export interface Error {
    message: string;
  }

  export class Plugin {
    static addCustomerSource(opts: { sourceId: string; type?: string }): Promise<void> {
      return execP('addCustomerSource', opts);
    }

    static cancelApplePay(): Promise<void> {
      return execP('cancelApplePay');
    }

    static confirmPaymentIntent(opts: CordovaStripe.ConfirmPaymentIntentOptions): Promise<void> {
      return execP('confirmPaymentIntent', opts);
    }

    static confirmSetupIntent(opts: CordovaStripe.CommonIntentOptions): Promise<void> {
      return execP('confirmSetupIntent', opts);
    }

    static createAccountToken(account: CordovaStripe.AccountParams): Promise<CordovaStripe.TokenResponse> {
      return execP('createAccountToken', account);
    }

    static createBankAccountToken(bankAccount: CordovaStripe.BankAccountTokenRequest): Promise<CordovaStripe.BankAccountTokenResponse> {
      return execP('createBankAccountToken', bankAccount);
    }

    static validateCard(card: CordovaStripe.CardTokenRequest): Promise<string> {
      return execP('validateCard', card);
    }

    static createCardToken(card: CordovaStripe.CardTokenRequest): Promise<CordovaStripe.CardTokenResponse> {
      return execP('createCardToken', card);
    }

    static createPiiToken(opts: CordovaStripe.CreatePiiTokenOptions): Promise<CordovaStripe.TokenResponse> {
      return execP('createPiiToken', opts);
    }

    static createSourceToken(opts: CordovaStripe.CreateSourceTokenOptions): Promise<CordovaStripe.TokenResponse> {
      return execP('createSourceToken', opts);
    }

    static customerPaymentMethods(): Promise<{ paymentMethods: CordovaStripe.PaymentMethod[] }> {
      return execP('customerPaymentMethods');
    }

    static customizePaymentAuthUI(opts: any): Promise<void> {
      return execP('', opts);
    }

    static deleteCustomerSource(opts: { sourceId: string }): Promise<void> {
      return execP('', opts);
    }

    static echo(options: { value: string }): Promise<{ value: string }> {
      return execP('echo');
    }

    static finalizeApplePayTransaction(opts: CordovaStripe.FinalizeApplePayTransactionOptions): Promise<void> {
      return execP('', opts);
    }

    static identifyCardBrand(opts: CordovaStripe.IdentifyCardBrandOptions): Promise<CordovaStripe.CardBrandResponse> {
      return execP('identifyCardBrand', opts);
    }

    static initCustomerSession(opts: { id: string; object: 'ephemeral_key'; associated_objects: Array<{ type: 'customer'; id: string }>; created: number; expires: number; livemode: boolean; secret: string; apiVersion?: string }): Promise<void> {
      return execP('initCustomerSession', opts);
    }

    static isApplePayAvailable(): Promise<CordovaStripe.AvailabilityResponse> {
      return execP('isApplePayAvailable');
    }

    static  isGooglePayAvailable(): Promise<CordovaStripe.AvailabilityResponse> {
      return execP('isGooglePayAvailable');
    }

    static payWithApplePay(options: CordovaStripe.ApplePayOptions): Promise<CordovaStripe.TokenResponse> {
      return execP('payWithApplePay');
    }

    static setCustomerDefaultSource(opts: { sourceId: string; type?: string }): Promise<void> {
      return execP('setCustomerDefaultSource', opts);
    }

    static setPublishableKey(opts: CordovaStripe.SetPublishableKeyOptions): Promise<void> {
      return execP('setPublishableKey', opts);
    }

    static startGooglePayTransaction(): Promise<void> {
      return execP('startGooglePayTransaction');
    }

    static validateCVC(opts: CordovaStripe.ValidateCVCOptions): Promise<CordovaStripe.ValidityResponse> {
      return execP('validateCVC', opts);
    }

    static validateCardNumber(opts: CordovaStripe.ValidateCardNumberOptions): Promise<CordovaStripe.ValidityResponse> {
      return execP('validateCardNumber', opts);
    }

    static validateExpiryDate(opts: CordovaStripe.ValidateExpiryDateOptions): Promise<CordovaStripe.ValidityResponse> {
      return execP('validateExpiryDate', opts);
    }

    // /**
    //  * Set publishable key
    //  * @param {string} key
    //  * @param {Function} success
    //  * @param {Function} error
    //  */
    // static setPublishableKey(key: string, success: BlankCallback = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'setPublishableKey', [key]);
    // }
    //
    // /**
    //  * Create a credit card token
    //  * @param {CordovaStripe.CardTokenRequest} creditCard
    //  * @param {CordovaStripe.CardTokenCallback} success
    //  * @param {CordovaStripe.ErrorCallback} error
    //  */
    // static createCardToken(creditCard: CardTokenRequest, success: CardTokenCallback = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'createCardToken', [creditCard]);
    // }
    //
    // /**
    //  * Create a bank account token
    //  * @param {CordovaStripe.BankAccountTokenRequest} bankAccount
    //  * @param {Function} success
    //  * @param {Function} error
    //  */
    // static createBankAccountToken(bankAccount: BankAccountTokenRequest, success: BankAccountTokenCallback = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'createBankAccountToken', [bankAccount]);
    // }
    //
    // /**
    //  * Validates card number
    //  * @param cardNumber Card number
    //  * @param {(isValid: boolean) => void} [success]
    //  * @param {Function} [error]
    //  */
    // static validateCardNumber(cardNumber, success: (isValid: boolean) => void = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'validateCardNumber', [cardNumber]);
    // }
    //
    // /**
    //  * Validates the expiry date of a card
    //  * @param {number} expMonth
    //  * @param {number} expYear
    //  * @param {(isValid: boolean) => void} [success]
    //  * @param {Function} [error]
    //  */
    // static validateExpiryDate(expMonth: number, expYear: number, success: (isValid: boolean) => void = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'validateExpiryDate', [expMonth, expYear]);
    // }
    //
    // /**
    //  * Validates a CVC of a card
    //  * @param {string} cvc
    //  * @param {(isValid: boolean) => void} [success]
    //  * @param {Function} [error]
    //  */
    // static validateCVC(cvc: string, success: (isValid: boolean) => void = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'validateCVC', [cvc]);
    // }
    //
    // /**
    //  * Gets a card type from a card number
    //  * @param {string | number} cardNumber
    //  * @param {(type: string) => void} [success]
    //  * @param {Function} [error]
    //  */
    // static getCardType(cardNumber: string | number, success: (type: string) => void = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'getCardType', [String(cardNumber)]);
    // }
    //
    // /**
    //  * Pay with ApplePay
    //  * @param {CordovaStripe.ApplePayOptions} options
    //  * @param {(token: string, callback: (paymentProcessed: boolean) => void) => void} success
    //  * @param {Function} error
    //  */
    // static payWithApplePay(options: ApplePayOptions, success: (token: TokenResponse, callback: (paymentProcessed: boolean) => void) => void, error: ErrorCallback = NOOP) {
    //   if (!options || !options.merchantId || !options.country || !options.currency || !options.items || !options.items.length) {
    //     error({
    //       message: 'Missing one or more payment options.',
    //     });
    //     return;
    //   }
    //
    //   options.items = options.items.map(item => {
    //     item.amount = String(item.amount);
    //     return item;
    //   });
    //
    //   exec((token: TokenResponse) => {
    //     success(token, (paymentProcessed: boolean) => {
    //       exec(NOOP, NOOP, 'CordovaStripe', 'finalizeApplePayTransaction', [Boolean(paymentProcessed)]);
    //     });
    //   }, error, 'CordovaStripe', 'initializeApplePayTransaction', [
    //     options.merchantId,
    //     options.country,
    //     options.currency,
    //     options.items,
    //   ]);
    // }
    //
    // static initGooglePay(success = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'initGooglePay');
    // }
    //
    // static payWithGooglePay(options: GooglePayOptions, success: (token: TokenResponse) => void, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'payWithGooglePay', [options.amount, options.currencyCode]);
    // }
    //
    // static createSource(type: SourceType, params: SourceParams, success: (token: TokenResponse) => void = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'createSource', [SourceTypeArray.indexOf(type.toLowerCase() as SourceType), params]);
    // }
    //
    // static createPiiToken(personalId: string, success = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'createPiiToken', [personalId]);
    // }
    //
    // static createAccountToken(accountParams: AccountParams, success = NOOP, error: ErrorCallback = NOOP) {
    //   exec(success, error, 'CordovaStripe', 'createAccountToken', [accountParams]);
    // }
  }
}

