[![npm](https://img.shields.io/npm/l/express.svg)](https://www.npmjs.com/package/cordova-plugin-stripe)

[![NPM](https://nodei.co/npm/cordova-plugin-stripe.png?stars&downloads)](https://nodei.co/npm/cordova-plugin-stripe/)


# Cordova Stripe Plugin
A Cordova plugin that lets you use Stripe Native SDKs for Android and iOS. This plugin also provides Browser platform support, see [this section](#browsersupport) for more details.

## Installation
```shell
cordova plugin add cordova-plugin-stripe
```

## Usage

First we need to set our publishable key. This can be your test or live key.
```javascript
cordova.plugins.stripe.setPublishableKey('pk_test_MyPublishableKey');
```

Now we can create a credit card token to send to our backend later on.

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
```

Once you have the token, you can now send it to your backend so you can charge the customer later on.


## API

#### setPublishableKey(key, success, error)
Set the publishable key.
* **key**: Publishable key (string)
* **success**: Success callback (Function)
* **error**: Error callback (Function)

#### createCardToken(creditCard, success, error)
Create a credit card token
* **creditCard**: Credit card information. See example above for available properties. (Object)
* **success**: Success callback (Function)
* **error**: Error callback (Function)


## Browser support
This plugin provides browser platform support. Method names and signatures match the [API above](#api). The plugin will automatically inject Stripe.js script into the web page when initialized.
 
*Thanks to [klirix](https://github.com/klirix) for submitting a [PR](https://github.com/zyramedia/cordova-plugin-stripe/pull/5) for the browser platform.*