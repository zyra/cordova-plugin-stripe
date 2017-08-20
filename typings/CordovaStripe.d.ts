interface CordovaPlugins {
    stripe: ICordovaStripe;
}

interface ICordovaStripe {
    createCardToken: (card: IStripeCreateCardTokenRequest, token: (token: IStripeToken) => void, err: (err) => void) => void;
}

interface IStripeCreateCardTokenRequest {
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

interface Metadata {
}