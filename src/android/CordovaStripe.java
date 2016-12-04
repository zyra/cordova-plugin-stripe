package com.zyramedia.cordova.stripe;

import com.stripe.android.*;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class CordovaStripe extends CordovaPlugin {

    private Stripe stripeObject;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        stripeObject = new Stripe();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("greet")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

            return true;

        } else {

            return false;

        }

        switch(action) {

            case "setPublishableKey":
                setPublishableKey(data.getString(0), callbackContext);
                break;

            case "createCardToken":
                createCardToken(data.getJSONObject(0), callbackContext);
                break;

            default:
                return false;

        }

        return true;

    }

    private void setPublishableKey(String key, final CallbackContext callbackContext) {

        stripeObject.setDefaultPublishableKey(key);
        callbackContext.success();

    }

    private void createCardToken(JSONObject creditCard, final CallbackContext callbackContext) {

        stripeObject.createToken(
            new Card(creditCard.number, creditCard.exp_month, creditCard.exp_year, creditCard.cvc),
            new TokenCallback() {
                public void onSuccess(Token token) {
                    callbackContext.success(token);
                }
                public void onError(Exception error) {
                    callbackContext.error(error);
                }
            }
        );

    }

}