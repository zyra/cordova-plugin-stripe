package com.zyramedia.cordova.stripe;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.stripe.android.CardUtils;
import com.stripe.android.TokenCallback;
import com.stripe.android.Stripe;
import com.stripe.android.model.BankAccount;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import android.util.Log;


public class CordovaStripe extends CordovaPlugin {

  private Stripe stripeInstance;

  private PaymentSheet paymentSheet;
    PaymentSheet.CustomerConfiguration customerConfig;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    stripeInstance = new Stripe(webView.getContext());
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
    } else if (action.equals("presentPaymentSheet")) {
      String publishableKey = data.getString(0);
      String setupIntentClientSecret = data.getString(1);
      presentPaymentSheet(setupIntentClientSecret, publishableKey, callbackContext);
    } else {
      return false;
    }

    return true;

  }

  private void setPublishableKey(final String key, final CallbackContext callbackContext) {

    try {
      stripeInstance.setDefaultPublishableKey(key);
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
    } 
    catch (JSONException e) {
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

    } 
    catch (JSONException e) {
      return null;
    }
  }

  private void presentPaymentSheet(String setupIntentClientSecret, String publishableKey, CallbackContext callbackContext) {

    paymentSheet = new PaymentSheet(this.cordova.getActivity(), this::onPaymentSheetResult);

    final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Example, Inc.")
        .customer(customerConfig)
        .build();
        paymentSheet.presentWithPaymentIntent(
        setupIntentClientSecret,
        configuration
    );
//        if (message != null && message.length() > 0) {
//            callbackContext.success(message);
//        } else {
//            callbackContext.error("Expected one non-empty string argument.");
//        }
}

private void onPaymentSheetResult(
    final PaymentSheetResult paymentSheetResult
  ) {
    if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
      Log.d("App", "Canceled");
//          callbackContext.success("Canceled!");
    } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
      Log.e("App", "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
//          callbackContext.success(((PaymentSheetResult.Failed) paymentSheetResult).getError());
    } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
      // Display for example, an order confirmation screen
      Log.d("App", "Completed");
//          callbackContext.success("Success!");
    }
  }

}
