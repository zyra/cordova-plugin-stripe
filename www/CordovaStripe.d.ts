export interface Window {
    cordova: Cordova;
}
export interface Cordova {
    plugins: CordovaPlugins;
}
export interface CordovaPlugins {
    stripe: typeof CordovaStripe.Plugin;
}
export declare namespace CordovaStripe {
    interface CommonIntentOptions {
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
    type ConfirmSetupIntentOptions = CommonIntentOptions;
    interface ConfirmPaymentIntentOptions extends CommonIntentOptions {
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
    type SetPublishableKeyOptions = {
        key: string;
    };
    type ValidateCardNumberOptions = {
        number: string;
    };
    type ValidateExpiryDateOptions = {
        exp_month: number;
        exp_year: number;
    };
    type ValidateCVCOptions = {
        cvc: string;
    };
    type IdentifyCardBrandOptions = {
        number: string;
    };
    type CreatePiiTokenOptions = {
        pii: string;
    };
    type CreateSourceTokenOptions = {
        type: SourceType;
        params: SourceParams;
    };
    type FinalizeApplePayTransactionOptions = {
        success: boolean;
    };
    type ValidityResponse = {
        valid: boolean;
    };
    type AvailabilityResponse = {
        available: boolean;
    };
    type CardBrandResponse = {
        brand: CardBrand;
    };
    interface PaymentMethod {
        created?: number;
        customerId?: string;
        id?: string;
        livemode: boolean;
        type?: string;
        card?: Card;
    }
    enum UIButtonType {
        SUBMIT = "submit",
        CONTINUE = "continue",
        NEXT = "next",
        CANCEL = "cancel",
        RESEND = "resend",
        SELECT = "select",
    }
    interface UIButtonCustomizationOptions {
        type: UIButtonType;
        backgroundColor?: string;
        textColor?: string;
        fontName?: string;
        cornerRadius?: number;
        fontSize?: number;
    }
    interface UICustomizationOptions {
        accentColor?: string;
        buttonCustomizations?: UIButtonCustomizationOptions[];
    }
    interface BankAccount {
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
    interface Card {
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
    interface BankAccountTokenRequest {
        country: string;
        currency: string;
        account_holder_name: string;
        account_holder_type: string;
        routing_number: string;
        account_number: string;
    }
    interface BankAccountTokenResponse extends TokenResponse {
        bank_account: BankAccount;
    }
    interface CardTokenRequest {
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
    interface CardTokenResponse extends TokenResponse {
        card: Card;
    }
    interface TokenResponse {
        id: string;
        type: string;
        created: Date;
    }
    interface ApplePayItem {
        label: string;
        amount: number | string;
    }
    interface ApplePayOptions {
        merchantId: string;
        country: string;
        currency: string;
        items: ApplePayItem[];
    }
    interface GooglePayOptions {
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
            format?: 'MIN';
            phoneNumberRequired?: boolean;
        };
        shippingAddressRequired?: boolean;
        shippingAddressParameters?: {};
    }
    interface ThreeDeeSecureParams {
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
    interface GiroPayParams {
        amount: number;
        name: string;
        returnURL: string;
        statementDescriptor: string;
    }
    interface iDEALParams {
        amount: number;
        name: string;
        returnURL: string;
        statementDescriptor: string;
        bank: string;
    }
    interface SEPADebitParams {
        name: string;
        iban: string;
        addressLine1: string;
        city: string;
        postalCode: string;
        country: string;
    }
    interface SofortParams {
        amount: number;
        returnURL: string;
        country: string;
        statementDescriptor: string;
    }
    interface AlipayParams {
        amount: number;
        currency: string;
        returnURL: string;
    }
    interface AlipayReusableParams {
        currency: string;
        returnURL: string;
    }
    interface P24Params {
        amount: number;
        currency: string;
        email: string;
        name: string;
        returnURL: string;
    }
    interface VisaCheckoutParams {
        callId: string;
    }
    type SourceParams = ThreeDeeSecureParams | GiroPayParams | iDEALParams | SEPADebitParams | SofortParams | AlipayParams | AlipayReusableParams | P24Params | VisaCheckoutParams;
    enum SourceType {
        ThreeDeeSecure = "3ds",
        GiroPay = "giropay",
        iDEAL = "ideal",
        SEPADebit = "sepadebit",
        Sofort = "sofort",
        AliPay = "alipay",
        AliPayReusable = "alipayreusable",
        P24 = "p24",
        VisaCheckout = "visacheckout",
    }
    enum CardBrand {
        AMERICAN_EXPRESS = "AMERICAN_EXPRESS",
        DISCOVER = "DISCOVER",
        JCB = "JCB",
        DINERS_CLUB = "DINERS_CLUB",
        VISA = "VISA",
        MASTERCARD = "MASTERCARD",
        UNIONPAY = "UNIONPAY",
        UNKNOWN = "UNKNOWN",
    }
    interface Address {
        line1: string;
        line2: string;
        city: string;
        postal_code: string;
        state: string;
        country: string;
    }
    interface LegalEntity {
        address?: Address;
        dob?: {
            day: number;
            month: number;
            year: number;
        };
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
        };
        personal_id_number_provided?: boolean;
        phone_number?: string;
        ssn_last_4_provided?: boolean;
        tax_id_registrar?: string;
        type?: 'individual' | 'company';
        verification?: any;
    }
    interface AccountParams {
        tosShownAndAccepted: boolean;
        legalEntity: LegalEntity;
    }
    interface Error {
        message: string;
    }
    class Plugin {
        static addCustomerSource(opts: {
            sourceId: string;
            type?: string;
        }): Promise<void>;
        static cancelApplePay(): Promise<void>;
        static confirmPaymentIntent(opts: CordovaStripe.ConfirmPaymentIntentOptions): Promise<void>;
        static confirmSetupIntent(opts: CordovaStripe.CommonIntentOptions): Promise<void>;
        static createAccountToken(account: CordovaStripe.AccountParams): Promise<CordovaStripe.TokenResponse>;
        static createBankAccountToken(bankAccount: CordovaStripe.BankAccountTokenRequest): Promise<CordovaStripe.BankAccountTokenResponse>;
        static validateCard(card: CordovaStripe.CardTokenRequest): Promise<string>;
        static createCardToken(card: CordovaStripe.CardTokenRequest): Promise<CordovaStripe.CardTokenResponse>;
        static createPiiToken(opts: CordovaStripe.CreatePiiTokenOptions): Promise<CordovaStripe.TokenResponse>;
        static createSourceToken(opts: CordovaStripe.CreateSourceTokenOptions): Promise<CordovaStripe.TokenResponse>;
        static customerPaymentMethods(): Promise<{
            paymentMethods: CordovaStripe.PaymentMethod[];
        }>;
        static customizePaymentAuthUI(opts: any): Promise<void>;
        static deleteCustomerSource(opts: {
            sourceId: string;
        }): Promise<void>;
        static echo(options: {
            value: string;
        }): Promise<{
            value: string;
        }>;
        static finalizeApplePayTransaction(opts: CordovaStripe.FinalizeApplePayTransactionOptions): Promise<void>;
        static identifyCardBrand(opts: CordovaStripe.IdentifyCardBrandOptions): Promise<CordovaStripe.CardBrandResponse>;
        static initCustomerSession(opts: {
            id: string;
            object: 'ephemeral_key';
            associated_objects: Array<{
                type: 'customer';
                id: string;
            }>;
            created: number;
            expires: number;
            livemode: boolean;
            secret: string;
            apiVersion?: string;
        }): Promise<void>;
        static isApplePayAvailable(): Promise<CordovaStripe.AvailabilityResponse>;
        static isGooglePayAvailable(): Promise<CordovaStripe.AvailabilityResponse>;
        static payWithApplePay(options: CordovaStripe.ApplePayOptions): Promise<CordovaStripe.TokenResponse>;
        static setCustomerDefaultSource(opts: {
            sourceId: string;
            type?: string;
        }): Promise<void>;
        static setPublishableKey(opts: CordovaStripe.SetPublishableKeyOptions): Promise<void>;
        static startGooglePayTransaction(): Promise<void>;
        static validateCVC(opts: CordovaStripe.ValidateCVCOptions): Promise<CordovaStripe.ValidityResponse>;
        static validateCardNumber(opts: CordovaStripe.ValidateCardNumberOptions): Promise<CordovaStripe.ValidityResponse>;
        static validateExpiryDate(opts: CordovaStripe.ValidateExpiryDateOptions): Promise<CordovaStripe.ValidityResponse>;
    }
}
