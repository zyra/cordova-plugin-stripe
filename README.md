# Cordova Stripe Plugin
A Cordova plugin that lets you use Stripe Native SDKs for Android, iOS and Browser.

[![npm](https://img.shields.io/npm/l/express.svg)](https://www.npmjs.com/package/cordova-plugin-stripe)

[![NPM](https://nodei.co/npm/cordova-plugin-stripe.png?stars&downloads)](https://nodei.co/npm/cordova-plugin-stripe/)
[![NPM](https://nodei.co/npm-dl/cordova-plugin-stripe.png?months=6&height=2)](https://nodei.co/npm/cordova-plugin-stripe/)


## Installation
```shell
cordova plugin add cordova-plugin-stripe
```

<br>
<br>

## Usage

First we need to set our publishable key. This can be your test or live key.
```javascript
cordova.plugins.stripe.setPublishableKey('pk_test_MyPublishableKey');
```

Now we can create a credit card token to send to our backend.

```javascript
var card = {
  number: '4242424242424242', // 16-digit credit card number
  expMonth: 12, // expiry month
  expYear: 2020, // expiry year
  cvc: '220', // CVC / CCV
  name: 'John Smith', // card holder name (optional)
  address_line1: '123 Some Street', // address line 1 (optional)
  address_line2: 'Suite #220', // address line 2 (optional)
  address_city: 'Toronto', // city (optional)
  address_state: 'Ontario', // state/province (optional)
  address_country: 'Canada', // country (optional)
  postal_code: 'L5L5L5', // Postal Code / Zip Code (optional)
  currency: 'CAD' // Three-letter ISO currency code (optional)
};

function onSuccess(tokenId) {
    console.log('Got card token!', tokenId);
}

function onError(errorMessage) {
    console.log('Error getting card token', errorMessage);
}

cordova.plugins.stripe.createCardToken(card, onSuccess, onError);


// bank account example
var bankAccount = {
  routing_number: '11000000',
  account_number: '000123456789',
  account_holder_name: 'John Smith', // optional
  account_holder_type: 'individual', // optional
  currency: 'CAD',
  country: 'CA'
};

cordova.plugins.stripe.createBankAccountToken(bankAccount, onSuccess, onError);
```

Once you have the token, you can now send it to your backend so you can charge the customer later on.

<br>
<br>


## API Reference


* [stripe](#module_stripe)
    * [.setPublishableKey(key, [success], [error])](#module_stripe.setPublishableKey)
    * [.createCardToken(creditCard, success, error)](#module_stripe.createCardToken)
    * [.createBankAccountToken(bankAccount, success, error)](#module_stripe.createBankAccountToken)
    * [.validateCardNumber(cardNumber, success, error)](#module_stripe.validateCardNumber)
    * [.validateExpiryDate(expMonth, expYear, success, error)](#module_stripe.validateExpiryDate)
    * [.validateCVC(cvc, success, error)](#module_stripe.validateCVC)
    * [.getCardType(cardNumber, success, error)](#module_stripe.getCardType)
    * [.CreditCardTokenParams](#module_stripe.CreditCardTokenParams) : <code>Object</code>
    * [.BankAccountTokenParams](#module_stripe.BankAccountTokenParams) : <code>object</code>


<br>
<br>

<a name="module_stripe"></a>

## stripe
<a name="module_stripe.setPublishableKey"></a>

### stripe.setPublishableKey(key, [success], [error])
Set publishable key

**Kind**: static method of [<code>stripe</code>](#module_stripe)  

| Param | Type | Description |
| --- | --- | --- |
| key | <code>string</code> | Publishable key |
| [success] | <code>function</code> | Success callback |
| [error] | <code>function</code> | Error callback |

<a name="module_stripe.createCardToken"></a>

### stripe.createCardToken(creditCard, success, error)
Create a credit card token

**Kind**: static method of [<code>stripe</code>](#module_stripe)  

| Param | Type | Description |
| --- | --- | --- |
| creditCard | [<code>CreditCardTokenParams</code>](#module_stripe.CreditCardTokenParams) | Credit card information |
| success | <code>function</code> | Success callback |
| error | <code>function</code> | Error callback |

<a name="module_stripe.createBankAccountToken"></a>

### stripe.createBankAccountToken(bankAccount, success, error)
Create a bank account token

**Kind**: static method of [<code>stripe</code>](#module_stripe)  

| Param | Type | Description |
| --- | --- | --- |
| bankAccount | [<code>BankAccountTokenParams</code>](#module_stripe.BankAccountTokenParams) | Bank account information |
| success | <code>function</code> | Success callback |
| error | <code>function</code> | Error callback |

<a name="module_stripe.validateCardNumber"></a>

### stripe.validateCardNumber(cardNumber, success, error)
Validates card number

**Kind**: static method of [<code>stripe</code>](#module_stripe)  

| Param | Type | Description |
| --- | --- | --- |
| cardNumber | <code>String</code> | Credit card number |
| success | <code>function</code> | Success callback that will be called if card number is valid |
| error | <code>function</code> | Error callback that will be called if card number is invalid |

<a name="module_stripe.validateExpiryDate"></a>

### stripe.validateExpiryDate(expMonth, expYear, success, error)
Validates the expiry date of a card

**Kind**: static method of [<code>stripe</code>](#module_stripe)  

| Param | Type | Description |
| --- | --- | --- |
| expMonth | <code>number</code> | Expiry month |
| expYear | <code>number</code> | Expiry year |
| success | <code>function</code> |  |
| error | <code>function</code> |  |

<a name="module_stripe.validateCVC"></a>

### stripe.validateCVC(cvc, success, error)
Validates a CVC of a card

**Kind**: static method of [<code>stripe</code>](#module_stripe)  

| Param | Type | Description |
| --- | --- | --- |
| cvc | <code>string</code> | CVC/CVV |
| success | <code>function</code> |  |
| error | <code>function</code> |  |

**Example**  
```js
function onSuccess() {
  console.log('isValid');
}

function onError() {
  console.log('invalid');
}

cordova.plugin.stripe.validateCVC('424', onSuccess, onError);
```
<a name="module_stripe.getCardType"></a>

### stripe.getCardType(cardNumber, success, error)
Gets a card type from a card number

**Kind**: static method of [<code>stripe</code>](#module_stripe)  

| Param | Type | Description |
| --- | --- | --- |
| cardNumber | <code>string</code> | Credit card number |
| success | <code>function</code> |  |
| error | <code>function</code> |  |

**Example**  
```js
cordova.plugins.stripe.getCardType('4242424242424242', function(cardType) {
  console.log(cardType); // visa
});
```
<a name="module_stripe.CreditCardTokenParams"></a>

### stripe.CreditCardTokenParams : <code>Object</code>
Parameters to create a credit card token

**Kind**: static typedef of [<code>stripe</code>](#module_stripe)  
**Properties**

| Name | Type | Description |
| --- | --- | --- |
| number | <code>string</code> | Card number |
| expMonth | <code>number</code> | Expiry month |
| expYear | <code>number</code> | Expiry year |
| cvc | <code>string</code> | CVC/CVV |
| name | <code>string</code> | Cardholder name |
| address_line1 | <code>string</code> | Address line 1 |
| address_line2 | <code>string</code> | Address line 2 |
| address_city | <code>string</code> | Address line 2 |
| address_state | <code>string</code> | State/Province |
| address_country | <code>string</code> | Country |
| postal_code | <code>string</code> | Postal/Zip code |
| currency | <code>string</code> | 3-letter code for currency |

<a name="module_stripe.BankAccountTokenParams"></a>

### stripe.BankAccountTokenParams : <code>object</code>
Parameters to create a bank account token

**Kind**: static typedef of [<code>stripe</code>](#module_stripe)  
**Properties**

| Name | Type | Description |
| --- | --- | --- |
| routing_number | <code>string</code> | Routing number |
| account_number | <code>string</code> | Account number |
| currency | <code>string</code> | Currency code. Example: `CAD`. |
| country | <code>string</code> | Country code. Example: `CA`. |
| account_holder_name | <code>string</code> | Account holder name |
| account_holder_type | <code>string</code> | Account holder type. This can be `individual` or `company`. |

<br>
<br>


<br>
<br>

## Tests
To test this plugin with `cordova-plugin-test-framework`, run the following command to install the tests:
```shell
cordova plugin add https://github.com/zyramedia/cordova-plugin-stripe#:/tests
```

<br>
<br>

## Browser support
This plugin provides browser platform support. Method names and signatures match the [API above](#api). The plugin will automatically inject Stripe.js script into the web page when initialized.

*Thanks to [klirix](https://github.com/klirix) for submitting a [PR](https://github.com/zyramedia/cordova-plugin-stripe/pull/5) for the browser platform.*


<br><br>
## Contribution
- **Having an issue**? or looking for support? [Open an issue](https://github.com/zyra/cordova-plugin-stripe/issues/new) and we will get you the help you need.
- Got a **new feature or a bug fix**? Fork the repo, make your changes, and submit a pull request.

## Support this project
If you find this project useful, please star the repo to let people know that it's reliable. Also, share it with friends and colleagues that might find this useful as well. Thank you :smile:
