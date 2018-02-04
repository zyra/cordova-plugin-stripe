import { exec } from 'cordova';

const NOOP: any = () => {};

export interface Window {
  cordova: Cordova;
}

export interface Cordova {
  plugins: CordovaPlugins;
}

export interface CordovaPlugins {
  stripe: CordovaStripe.Plugin;
}

export namespace CordovaStripe {
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
    brand: string;
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
    expMonth: number;
    expYear: number;
    cvc: string;
    name?: string;
    address_line1?: string;
    address_line2?: string;
    address_city?: string;
    address_state?: string;
    address_country?: string;
    postal_code?: string;
    currency?: string
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
    amount: number;
  }

  export interface ApplePayOptions {
    merchantId: string;
    country: string;
    currency: string;
    items: ApplePayItem[];
  }

  export interface Error {
    message: string;
  }

  export type BlankCallback = () => void;
  export type ErrorCallback = (error: Error) => void;
  export type CardTokenCallback = (token: CardTokenResponse) => void;
  export type BankAccountTokenCallback = (token: BankAccountTokenRequest) => void;

  export class Plugin {
    /**
     * Set publishable key
     * @param {string} key
     * @param {Function} success
     * @param {Function} error
     */
    static setPublishableKey(key: string, success: BlankCallback = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'setPublishableKey', [key]);
    }

    /**
     * Create a credit card token
     * @param {CordovaStripe.CardTokenRequest} creditCard
     * @param {CordovaStripe.CardTokenCallback} success
     * @param {CordovaStripe.ErrorCallback} error
     */
    static createCardToken(creditCard: CardTokenRequest, success: CardTokenCallback = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'createCardToken', [creditCard]);
    }

    /**
     * Create a bank account token
     * @param {CordovaStripe.BankAccountTokenRequest} bankAccount
     * @param {Function} success
     * @param {Function} error
     */
    static createBankAccountToken(bankAccount: BankAccountTokenRequest, success: BankAccountTokenCallback = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'createBankAccountToken', [bankAccount]);
    }

    /**
     * Validates card number
     * @param cardNumber Card number
     * @param {(isValid: boolean) => void} [success]
     * @param {Function} [error]
     */
    static validateCardNumber(cardNumber, success: (isValid: boolean) => void = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'validateCardNumber', [cardNumber]);
    }

    /**
     * Validates the expiry date of a card
     * @param {number} expMonth
     * @param {number} expYear
     * @param {(isValid: boolean) => void} [success]
     * @param {Function} [error]
     */
    static validateExpiryDate(expMonth: number, expYear: number, success: (isValid: boolean) => void = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'validateExpiryDate', [expMonth, expYear]);
    }

    /**
     * Validates a CVC of a card
     * @param {string} cvc
     * @param {(isValid: boolean) => void} [success]
     * @param {Function} [error]
     */
    static validateCVC(cvc: string, success: (isValid: boolean) => void = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'validateCVC', [cvc]);
    }

    /**
     * Gets a card type from a card number
     * @param {string | number} cardNumber
     * @param {(type: string) => void} [success]
     * @param {Function} [error]
     */
    static getCardType(cardNumber: string | number, success: (type: string) => void = NOOP, error: ErrorCallback = NOOP) {
      exec(success, error, 'CordovaStripe', 'getCardType', [String(cardNumber)]);
    }

    /**
     * Pay with ApplePay
     * @param {CordovaStripe.ApplePayOptions} options
     * @param {(token: string, callback: (paymentProcessed: boolean) => void) => void} success
     * @param {Function} error
     */
    static payWithApplePay(options: ApplePayOptions, success: (token: string, callback: (paymentProcessed: boolean) => void) => void, error: ErrorCallback = NOOP) {
      if (!options || !options.merchantId || !options.country || !options.currency || !options.items || !options.items.length) {
        error({
          message: 'Missing one or more payment options.'
        });
        return;
      }

      exec((token: string) => {
        success(token, (paymentProcessed: boolean) => {
          exec(NOOP, NOOP, 'CordovaStripe', 'finalizeApplePayTransaction', [Boolean(paymentProcessed)]);
        });
      }, error, 'CordovaStripe', 'initializeApplePayTransaction', [
        options.merchantId,
        options.country,
        options.currency,
        options.items
      ])
    }
  }
}

