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

---

# API Reference

{{#orphans~}}
{{>member-index}}
{{/orphans}}

---

{{#modules~}}
{{>header~}}
{{>body~}}
{{>members~}}

---

{{/modules}}