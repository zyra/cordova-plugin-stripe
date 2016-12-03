package com.zyramedia.cordova.stripe;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class CordovaStripe extends CordovaPlugin {

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
                return true;
                break;

            default:
                return false;

        }

    }

    private void setPublishableKey(String key, CallbackContext callbackContext) throws JSONException {



    }

}