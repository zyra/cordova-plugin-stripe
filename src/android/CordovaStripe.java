package com.zyramedia.cordova.stripe;

import android.app.Activity;
import android.content.Intent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.stripe.android.CardUtils;
import com.stripe.android.SourceCallback;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.AccountParams;
import com.stripe.android.model.BankAccount;
import com.stripe.android.model.Card;
import com.stripe.android.model.Source;
import com.stripe.android.model.SourceParams;
import com.stripe.android.model.Token;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class CordovaStripe extends CordovaPlugin {

    private Stripe stripeInstance;
    private String publishableKey;
    private PaymentsClient paymentsClient;
    private boolean googlePayReady;
    private PaymentMethodTokenizationParameters googlePayParams;
    private final int LOAD_PAYMENT_DATA_REQUEST_CODE = 9972;
    private CallbackContext googlePayCallbackContext;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        stripeInstance = new Stripe(webView.getContext());
    }

    @Override
    public boolean execute(final String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        switch (action) {
            case "setPublishableKey":
                setPublishableKey(data.getString(0), callbackContext);
                break;

            case "createCardToken":
                createCardToken(data.getJSONObject(0), callbackContext);
                break;

            case "createBankAccountToken":
                createBankAccountToken(data.getJSONObject(0), callbackContext);
                break;

            case "validateCardNumber":
                validateCardNumber(data.getString(0), callbackContext);
                break;

            case "validateExpiryDate":
                validateExpiryDate(data.getInt(0), data.getInt(1), callbackContext);
                break;

            case "validateCVC":
                validateCVC(data.getString(0), callbackContext);
                break;

            case "getCardType":
                getCardType(data.getString(0), callbackContext);
                break;

            case "createSource":
                createSource(data.getInt(0), data.getJSONObject(1), callbackContext);
                break;

            case "initGooglePay":
                initGooglePay(callbackContext);
                break;

            case "createGooglePayToken":
                createGooglePayToken(data.getString(0), data.getString(2), callbackContext);
                break;

            case "createPiiToken":
                createPiiToken(data.getString(0), callbackContext);
                break;

            case "createAccountToken":
                createAccountToken(data.getJSONObject(0), callbackContext);
                break;

            default:
                return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(intent);
                    String rawToken = paymentData.getPaymentMethodToken().getToken();
                    Token stripeToken = Token.fromString(rawToken);
                    if (stripeToken != null) {
                        JSONObject tokenObject = getCardObjectFromToken(stripeToken);
                        googlePayCallbackContext.success(tokenObject);
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(intent);
                    googlePayCallbackContext.error("Error occurred while attempting to pay with GooglePay. Error #" + status.toString());
                    break;
            }
        }
    }

    private void setPublishableKey(final String key, final CallbackContext callbackContext) {

        try {
            stripeInstance.setDefaultPublishableKey(key);
            publishableKey = key;
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getLocalizedMessage());
        }

    }

    private void createCardToken(final JSONObject creditCard, final CallbackContext callbackContext) {

        try {
            Card cardObject = new Card(
                    creditCard.getString("number"),
                    creditCard.getInt("expMonth"),
                    creditCard.getInt("expYear"),
                    creditCard.getString("cvc"),
                    creditCard.has("name") ? creditCard.getString("name") : null,
                    creditCard.has("address_line1") ? creditCard.getString("address_line1") : null,
                    creditCard.has("address_line2") ? creditCard.getString("address_line2") : null,
                    creditCard.has("address_city") ? creditCard.getString("address_city") : null,
                    creditCard.has("address_state") ? creditCard.getString("address_state") : null,
                    creditCard.has("postalCode") ? creditCard.getString("postalCode") : null,
                    creditCard.has("address_country") ? creditCard.getString("address_country") : null,
                    creditCard.has("currency") ? creditCard.getString("currency") : null
            );

            stripeInstance.createToken(
                    cardObject,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            callbackContext.success(getCardObjectFromToken(token));
                        }

                        public void onError(Exception error) {
                            callbackContext.error(error.getLocalizedMessage());
                        }
                    }
            );

        } catch (JSONException e) {
            callbackContext.error(e.getLocalizedMessage());
        }

    }

    private void createBankAccountToken(final JSONObject bankAccount, final CallbackContext callbackContext) {

        try {

            BankAccount bankAccountObject = new BankAccount(
                    bankAccount.getString("account_number"),
                    bankAccount.getString("country"),
                    bankAccount.getString("currency"),
                    bankAccount.getString("routing_number")
            );

            if (bankAccount.getString("account_holder_name") != null) {
                bankAccountObject.setAccountHolderName(bankAccount.getString("account_holder_name"));
            }

            String accountHolderType = bankAccount.getString("account_holder_type");
            if (accountHolderType.equals("individual")) {
                bankAccountObject.setAccountHolderType(BankAccount.TYPE_INDIVIDUAL);
            } else if (accountHolderType.equals("company")) {
                bankAccountObject.setAccountHolderType(BankAccount.TYPE_COMPANY);
            }

            stripeInstance.createBankAccountToken(
                    bankAccountObject,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            callbackContext.success(getBankObjectFromToken(token));
                        }

                        public void onError(Exception error) {
                            callbackContext.error(error.getLocalizedMessage());
                        }
                    }
            );

        } catch (JSONException e) {
            callbackContext.error(e.getLocalizedMessage());
        }

    }

    private void validateCardNumber(final String cardNumber, final CallbackContext callbackContext) {
        if (CardUtils.isValidCardNumber(cardNumber)) {
            callbackContext.success();
        } else {
            callbackContext.error("Invalid card number");
        }
    }

    private void validateExpiryDate(final Integer expMonth, final Integer expYear, final CallbackContext callbackContext) {
        Card card = new Card(null, expMonth, expYear, null);
        if (card.validateExpiryDate()) {
            callbackContext.success();
        } else {
            callbackContext.error("Invalid expiry date");
        }
    }

    private void validateCVC(final String cvc, final CallbackContext callbackContext) {
        Card card = new Card(null, null, null, cvc);
        if (card.validateCVC()) {
            callbackContext.success();
        } else {
            callbackContext.error("Invalid CVC");
        }
    }

    private void getCardType(final String cardNumber, final CallbackContext callbackContext) {
        Card card = new Card(cardNumber, null, null, null);
        callbackContext.success(card.getBrand());
    }

    private void createSource(final int sourceType, final JSONObject params, final CallbackContext callbackContext) {
        SourceParams sourceParams;


        try {
            long amount = params.has("amount")? params.getLong("amount") : 0;
            String currency = params.has("currency")? params.getString("currency") : "";
            String returnURL = params.has("returnURL")? params.getString("returnURL") : "";
            String card = params.has("card")? params.getString("card") : "";
            String name = params.has("name")? params.getString("name") : "";
            String statementDescriptor = params.has("statementDescriptor")? params.getString("statementDescriptor") : "";
            String bank = params.has("bank")? params.getString("bank") : "";
            String iban = params.has("iban")? params.getString("iban") : "";
            String addressLine1 = params.has("addressLine1")? params.getString("addressLine1") : "";
            String city = params.has("city")? params.getString("city") : "";
            String postalCode = params.has("postalCode")? params.getString("postalCode") : "";
            String country = params.has("country")? params.getString("country") : "";
            String email = params.has("email")? params.getString("email") : "";
            String callId = params.has("callId")? params.getString("callId") : "";

            switch (sourceType) {
                case 0:
                    sourceParams = SourceParams.createThreeDSecureParams(amount, currency, returnURL, card);
                    break;

                case 1:
                    sourceParams = SourceParams.createGiropayParams(amount, name, returnURL, statementDescriptor);
                    break;

                case 2:
                    sourceParams = SourceParams.createIdealParams(amount, name, returnURL, statementDescriptor, bank);
                    break;

                case 3:
                    sourceParams = SourceParams.createSepaDebitParams(name, iban, addressLine1, city, postalCode, country);
                    break;

                case 4:
                    sourceParams = SourceParams.createSofortParams(amount, returnURL, country, statementDescriptor);
                    break;

                case 5:
                    sourceParams = SourceParams.createAlipaySingleUseParams(amount, currency, name, email, returnURL);
                    break;

                case 6:
                    sourceParams = SourceParams.createAlipayReusableParams(currency, name, email, returnURL);
                    break;

                case 7:
                    sourceParams = SourceParams.createP24Params(amount, currency, name, email, returnURL);
                    break;

                case 8:
                    sourceParams = SourceParams.createVisaCheckoutParams(callId);
                    break;

                default:
                    return;
            }
        } catch (JSONException err) {
            callbackContext.error(err.getLocalizedMessage());
            return;
        }

        stripeInstance.createSource(sourceParams, new SourceCallback() {
            @Override
            public void onError(Exception error) {
                callbackContext.error(error.getLocalizedMessage());
            }

            @Override
            public void onSuccess(Source source) {
                try {
                    callbackContext.success(source.toJson());
                } catch (Exception err) {
                    callbackContext.error(err.getLocalizedMessage());
                }
            }
        });
    }

    private void createPiiToken(final String personalId, final CallbackContext callbackContext) {
        stripeInstance.createPiiToken(personalId, new TokenCallback() {
            @Override
            public void onError(Exception error) {
                callbackContext.error(error.getLocalizedMessage());
            }

            @Override
            public void onSuccess(Token token) {
                callbackContext.success(token.getId());
            }
        });
    }

    private void createAccountToken(final JSONObject params, final CallbackContext callbackContext) {
        try {
            Map<String, Object> legalEntity = jsonObjectToHashMap(params.getJSONObject("legalEntity"));
            Token token = stripeInstance.createAccountTokenSynchronous(
                    AccountParams.createAccountParams(
                            params.getBoolean("tosShownAndAccepted"),
                            legalEntity
                    )
            );

            callbackContext.success(token.getId());
        } catch (Exception err) {
            callbackContext.error(err.getLocalizedMessage());
        }
    }

    private JSONObject getBankObjectFromToken(final Token token) {
        try {
            JSONObject tokenObject = new JSONObject();
            JSONObject bankObject = new JSONObject();

            BankAccount account = token.getBankAccount();

            bankObject.put("account_holder_name", account.getAccountHolderName());
            bankObject.put("account_holder_type", account.getAccountHolderType());
            bankObject.put("bank_name", account.getBankName());
            bankObject.put("country", account.getCountryCode());
            bankObject.put("currency", account.getCurrency());
            bankObject.put("last4", account.getLast4());
            bankObject.put("routing_number", account.getRoutingNumber());

            tokenObject.put("bank_account", bankObject);
            tokenObject.put("id", token.getId());
            tokenObject.put("created", token.getCreated());
            tokenObject.put("type", token.getType());

            return tokenObject;
        } catch (JSONException e) {
            return null;
        }
    }

    private JSONObject getCardObjectFromToken(final Token token) {
        try {
            JSONObject tokenObject = new JSONObject();
            JSONObject cardObject = new JSONObject();

            Card card = token.getCard();

            cardObject.put("address_city", card.getAddressCity());
            cardObject.put("address_country", card.getAddressCountry());
            cardObject.put("address_state", card.getAddressState());
            cardObject.put("address_line1", card.getAddressLine1());
            cardObject.put("address_line2", card.getAddressLine2());
            cardObject.put("address_zip", card.getAddressZip());
            cardObject.put("brand", card.getBrand());
            cardObject.put("country", card.getAddressCountry());
            cardObject.put("cvc", card.getCVC());
            cardObject.put("exp_month", card.getExpMonth());
            cardObject.put("exp_year", card.getExpYear());
            cardObject.put("funding", card.getFunding());
            cardObject.put("last4", card.getLast4());
            cardObject.put("name", card.getName());

            tokenObject.put("card", cardObject);
            tokenObject.put("id", token.getId());
            tokenObject.put("created", token.getCreated());
            tokenObject.put("type", token.getType());

            return tokenObject;

        } catch (JSONException e) {
            return null;
        }
    }

    private void initGooglePay(final CallbackContext callbackContext) {
        paymentsClient = Wallet.getPaymentsClient(
                cordova.getContext(),
                new Wallet.WalletOptions.Builder().setEnvironment(publishableKey == null || publishableKey.contains("test") ? WalletConstants.ENVIRONMENT_TEST : WalletConstants.ENVIRONMENT_PRODUCTION)
                        .build()
        );

        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build();
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                (Task<Boolean> task1) -> {
                    try {
                        googlePayReady =
                                task1.getResult(ApiException.class);
                        if (googlePayReady) {
                            //show Google as payment option

                            googlePayParams = PaymentMethodTokenizationParameters.newBuilder()
                                    .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                                    .addParameter("gateway", "stripe")
                                    .addParameter("stripe:publishableKey", publishableKey)
                                    .addParameter("stripe:version", "5.1.0")
                                    .build();

                            callbackContext.success();
                        } else {
                            //hide Google as payment option
                            callbackContext.error("GooglePay not supported.");
                        }
                    } catch (ApiException exception) {
                        callbackContext.error(exception.getLocalizedMessage());
                    }
                });
    }

    private void createGooglePayToken(String totalPrice, String currencyCode, final CallbackContext callbackContext) {
        PaymentDataRequest.Builder request = PaymentDataRequest.newBuilder()
                .setTransactionInfo(
                        TransactionInfo.newBuilder()
                                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                .setTotalPrice(totalPrice)
                                .setCurrencyCode(currencyCode)
                                .build()
                )
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .setCardRequirements(
                        CardRequirements.newBuilder()
                                .addAllowedCardNetworks(
                                        Arrays.asList(
                                                WalletConstants.CARD_NETWORK_AMEX,
                                                WalletConstants.CARD_NETWORK_DISCOVER,
                                                WalletConstants.CARD_NETWORK_VISA,
                                                WalletConstants.CARD_NETWORK_MASTERCARD
                                        )
                                )
                                .build()
                );

        request.setPaymentMethodTokenizationParameters(googlePayParams);
        final PaymentDataRequest finalRequest = request.build();

        if (finalRequest != null) {
            cordova.getActivity().runOnUiThread(() -> {
                AutoResolveHelper.resolveTask(
                        paymentsClient.loadPaymentData(finalRequest),
                        cordova.getActivity(),
                        LOAD_PAYMENT_DATA_REQUEST_CODE
                );
                googlePayCallbackContext = callbackContext;
            });
        } else {
            callbackContext.error("Unable to pay with GooglePay");
        }
    }


    private HashMap<String, Object> jsonObjectToHashMap(final JSONObject obj) {
        if (obj != null && obj.length() > 0) {
            final Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
            return new Gson().fromJson(obj.toString(), type);
        } else {
            return new HashMap<String, Object>();
        }
    }
}
