# Cordova Stripe Plugin
A Cordova plugin that lets you use Stripe Native SDKs for Android, iOS and Browser.

[![npm](https://img.shields.io/npm/l/express.svg)](https://www.npmjs.com/package/cordova-plugin-stripe)

[![NPM](https://nodei.co/npm/cordova-plugin-stripe.png?stars&downloads)](https://nodei.co/npm/cordova-plugin-stripe/)
[![NPM](https://nodei.co/npm-dl/cordova-plugin-stripe.png?months=6&height=2)](https://nodei.co/npm/cordova-plugin-stripe/)


---

# API Reference <a name="reference"></a>


* [stripe](#module_stripe)
    * [.setPublishableKey(key, success, error)](#module_stripe.setPublishableKey)
    * [.createCardToken(creditCard, success, error)](#module_stripe.createCardToken)
    * [.createBankAccountToken(bankAccount, success, error)](#module_stripe.createBankAccountToken)
    * [.validateCardNumber(cardNumber, success, error)](#module_stripe.validateCardNumber)


---

<a name="module_stripe"></a>

## stripe
<a name="module_stripe.setPublishableKey"></a>

### stripe.setPublishableKey(key, success, error)
Set publishable key

**Kind**: static method of <code>[stripe](#module_stripe)</code>  

| Param | Type | Description |
| --- | --- | --- |
| key | <code>string</code> | Publishable key |
| success | <code>function</code> | Success callback |
| error | <code>function</code> | Error callback |

<a name="module_stripe.createCardToken"></a>

### stripe.createCardToken(creditCard, success, error)
Create a credit card token

**Kind**: static method of <code>[stripe](#module_stripe)</code>  

| Param | Type | Description |
| --- | --- | --- |
| creditCard | <code>Object</code> | Credit card information |
| success | <code>function</code> | Success callback |
| error | <code>function</code> | Error callback |

<a name="module_stripe.createBankAccountToken"></a>

### stripe.createBankAccountToken(bankAccount, success, error)
Create a bank account token

**Kind**: static method of <code>[stripe](#module_stripe)</code>  

| Param | Type | Description |
| --- | --- | --- |
| bankAccount | <code>Object</code> | Bank account information |
| success | <code>function</code> | Success callback |
| error | <code>function</code> | Error callback |

<a name="module_stripe.validateCardNumber"></a>

### stripe.validateCardNumber(cardNumber, success, error)
Validates card number

**Kind**: static method of <code>[stripe](#module_stripe)</code>  

| Param | Type | Description |
| --- | --- | --- |
| cardNumber | <code>String</code> | Credit card number |
| success | <code>function</code> | Success callback that will be called if card number is valid |
| error | <code>function</code> | Error callback that will be called if card number is invalid |

---

