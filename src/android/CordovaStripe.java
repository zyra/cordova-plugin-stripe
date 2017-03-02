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

public class CordovaStripe extends CordovaPlugin {

    private Stripe stripeObject;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        stripeObject = new Stripe(webView.getContext());
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("setPublishableKey")) {
            setPublishableKey(data.getString(0), callbackContext);
        } else if (action.equals("createCardToken")) {
            createCardToken(data.getJSONObject(0), callbackContext);
        } else if (action.equals("createBankAccountToken")) {
            createBankAccountToken(data.getJSONObject(0), callbackContext);
        } else {
            return false;
        }

        return true;

    }

    private void setPublishableKey(String key, final CallbackContext callbackContext) {

        try {
            stripeObject.setDefaultPublishableKey(key);
            callbackContext.success();
        } catch (AuthenticationException e) {
            callbackContext.error(e.getMessage());
        }

    }

    private void createCardToken(JSONObject creditCard, final CallbackContext callbackContext) {

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
    
    private void createBankAccountToken(JSONObject bankAccount, final CallbackContext callbackContext) {
        
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

}
