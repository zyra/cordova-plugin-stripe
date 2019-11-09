package com.zyramedia.cordova.stripe

import android.util.Log
import com.stripe.android.PaymentSession
import com.stripe.android.PaymentSessionData

internal class StripePaymentSessionListener : PaymentSession.PaymentSessionListener {
    override fun onError(errorCode: Int, errorMessage: String) {
        Log.v("PaymentSessionListener", "onError")
    }

    override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
        Log.v("PaymentSessionListener", "onPaymentSessionDataChanged")
    }

    override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
        Log.v("PaymentSessionListener", "onCommunicatingStateChanged")
    }
}

