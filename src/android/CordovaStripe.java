package com.zyramedia.cordova.stripe;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.stripe.android.TokenCallback;
import com.stripe.android.Stripe;
import com.stripe.android.model.BankAccount;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.util.CardUtils;

public class CordovaStripe extends CordovaPlugin {

    private Stripe stripeObject;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        stripeObject = new Stripe(webView.getContext());
    }

    @Override
    public boolean execute(final String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("setPublishableKey")) {
            setPublishableKey(data.getString(0), callbackContext);
        } else if (action.equals("createCardToken")) {
            createCardToken(data.getJSONObject(0), callbackContext);
        } else if (action.equals("createBankAccountToken")) {
            createBankAccountToken(data.getJSONObject(0), callbackContext);
        } else if (action.equals("validateCardNumber")) {
            validateCardNumber(data.getString(0), callbackContext);
        } else if (action.equals("validateExpiryDate")) {
            validateExpiryDate(data.getInt(0), data.getInt(1), callbackContext);
        } else if (action.equals("validateCVC")) {
            validateCVC(data.getString(0), callbackContext);
        } else if (action.equals("getCardType")) {
            getCardType(data.getString(0), callbackContext);
        } else {
            return false;
        }

        return true;

    }

    private void setPublishableKey(final String key, final CallbackContext callbackContext) {

        try {
            stripeObject.setDefaultPublishableKey(key);
            callbackContext.success();
        } catch (AuthenticationException e) {
            callbackContext.error(e.getMessage());
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

            stripeObject.createToken(
                    cardObject,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            callbackContext.success(token.getId());
                        }
                        public void onError(Exception error) {
                            callbackContext.error(error.getMessage());
                        }
                    }
            );

        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        }

    }
    
    private void createBankAccountToken(final JSONObject bankAccount, final CallbackContext callbackContext) {
        
        try {

            BankAccount bankAccountObject = new BankAccount(
                    bankAccount.getString("accountNumber"),
                    bankAccount.getString("countryCode"),
                    bankAccount.getString("currency"),
                    bankAccount.getString("routingNumber")
            );

            stripeObject.createBankAccountToken(
                    bankAccountObject,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            callbackContext.success(token.getId());
                        }
                        public void onError(Exception error) {
                            callbackContext.error(error.getMessage());
                        }
                    }
            );
            
        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
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

}
