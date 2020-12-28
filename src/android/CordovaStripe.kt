package com.zyramedia.cordova.stripe

import android.app.Activity
import android.content.Intent
import android.util.Log
import ca.zyra.capacitor.stripe.GetGooglePayEnv
import ca.zyra.capacitor.stripe.GooglePayDataReq
import ca.zyra.capacitor.stripe.GooglePayPaymentsClient
import com.google.android.gms.wallet.*
import com.stripe.android.*
import com.stripe.android.model.*
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PluginCall(val data: JSONObject, private val ctx: CallbackContext) {
    fun getString(key: String, defaultValue: String = ""): String {
        return data.optString(key, defaultValue)
    }

    fun getNullableString(key: String, defaultValue: String? = ""): String?{
        return data.optString(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return data.optInt(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return data.optLong(key, defaultValue.toLong()).toFloat()
    }

    fun getObject(key: String): JSONObject {
        return data.getJSONObject(key)
    }

    fun getObject(key: String, defaultValue: JSONObject): JSONObject {
        val obj = data.getJSONObject(key)

        if (obj != null) {
            return obj
        }

        return defaultValue
    }

    fun getArray(key: String): JSONArray {
        return data.getJSONArray(key)
    }

    fun getArray(key: String, defaultValue: JSONArray): JSONArray {
        val arr = data.optJSONArray(key)

        if (arr != null) {
            return arr
        }

        return defaultValue
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return data.optBoolean(key, defaultValue)
    }

    fun hasOption(key: String): Boolean {
        return data.has(key)
    }

    fun error(msg: String, err: java.lang.Exception? = null) {
        ctx.error(msg)
    }

    fun success() {
        ctx.success()
    }

    fun resolve() {
        ctx.success()
    }

    fun success(msg: String) {
        ctx.success(msg)
    }

    fun success(msg: JSONObject) {
        ctx.success(msg)
    }
}

class CordovaStripe : CordovaPlugin() {
    private lateinit var stripeInstance: Stripe
    private lateinit var publishableKey: String
    private var isTest = true
    private var customerSession: CustomerSession? = null
    private var googlePayCallback: GooglePayCallback? = null
    private val context get() = cordova.context
    private val activity get() = cordova.activity

    private var savedCall: PluginCall? = null

    fun saveCall(c: PluginCall) {
        savedCall = c
    }

    fun freeSavedCall() {
        savedCall = null
    }

    override fun execute(action: String?, args: JSONArray?, callbackContext: CallbackContext?): Boolean {
        if (callbackContext == null || action == null) {
            return false
        }

        var opts = args!!.getJSONObject(0)

        if (opts == null) {
            opts = JSONObject()
        }

        val call = PluginCall(opts, callbackContext)
        javaClass.getMethod(action, call.javaClass).invoke(this, call)

        return true
    }


    fun setPublishableKey(call: PluginCall) {
        try {
            val key = call.getString("key")

            if (key == null || key == "") {
                call.error("you must provide a valid key")
                return
            }

            stripeInstance = Stripe(context, key)
            publishableKey = key
            isTest = key.contains("test")
            PaymentConfiguration.init(context, key)
            call.success()
        } catch (e: Exception) {
            call.error("unable to set publishable key: " + e.localizedMessage, e)
        }

    }


    fun identifyCardBrand(call: PluginCall) {
        val res = JSONObject()
        res.putOpt("brand", buildCard(call.data).build().brand)
        call.success(res)
    }

    fun validateCardNumber(call: PluginCall) {
        val res = JSONObject()
        res.putOpt("valid", buildCard(call.data).build().validateNumber())
        call.success(res)
    }

    fun validateExpiryDate(call: PluginCall) {
        val res = JSONObject()
        res.putOpt("valid", buildCard(call.data).build().validateExpiryDate())
        call.success(res)
    }

    fun validateCVC(call: PluginCall) {
        val res = JSONObject()
        res.putOpt("valid", buildCard(call.data).build().validateCVC())
        call.success(res)
    }

    fun validateCard(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val card = buildCard(call.data).build()

        if (!card.validateNumber()) {
            call.error("card's number is invalid")
            return
        }
        else if (!card.validateExpMonth()) {
            call.error("expiration month is invalid")
            return
        }
        else if (!card.validateExpiryDate()) {
            call.error("expiration year is invalid")
            return
        }
        else if (!card.cvc.isNullOrBlank() && !card.validateCVC()) {
            call.error("security code is invalid")
            return
        }
        call.success("success")
    }

    fun createCardToken(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val card = buildCard(call.data).build()

        if (!card.validateCard()) {
            call.error("invalid card information")
            return
        }

        val idempotencyKey = call.getString("idempotencyKey")
        val stripeAccountId = call.getString("stripeAccountId")

        val callback = object : ApiResultCallback<Token> {
            override fun onSuccess(result: Token) {
                val tokenJs = JSONObject()
                val cardJs = cardToJSON(result.card!!)

                tokenJs.putOpt("card", cardJs)
                tokenJs.putOpt("id", result.id)
                tokenJs.putOpt("created", result.created)
                tokenJs.putOpt("type", result.type)

                call.success(tokenJs)
            }

            override fun onError(e: Exception) {
                call.error("unable to create token: " + e.localizedMessage, e)
            }
        }

        stripeInstance.createCardToken(card, idempotencyKey, stripeAccountId, callback)
    }


    fun createBankAccountToken(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val accountNumber = call.getString("account_number")
        val accountHolderName = call.getString("account_holder_name")
        val accountHolderType = call.getString("account_holder_type")
        val country = call.getString("country")
        val currency = call.getString("currency")
        val routingNumber = call.getString("routing_number")

        var stripeAccHolder = BankAccountTokenParams.Type.Individual

        if (accountHolderType == "company") {
            stripeAccHolder = BankAccountTokenParams.Type.Company
        }

        val bankAccount = BankAccountTokenParams(country, currency, accountNumber, stripeAccHolder, accountHolderName, routingNumber)

        val idempotencyKey = call.getString("idempotencyKey")
        val stripeAccountId = call.getString("stripeAccountId")

        val callback = object : ApiResultCallback<Token> {
            override fun onSuccess(result: Token) {
                val js = JSONObject()

                if (result.bankAccount != null) {
                    val jsObj = bankAccountToJSON(result.bankAccount!!)
                    js.putOpt("bankAccount", jsObj)
                }

                js.put("id", result.id)
                js.put("created", result.created.getTime())
                js.put("type", result.type)
                js.put("object", "token")
                js.put("livemode", result.livemode)
                js.put("used", result.used)

                call.success(js)
            }

            override fun onError(e: Exception) {
                call.error("unable to create bank account token: " + e.localizedMessage, e)
            }
        }

        stripeInstance.createBankAccountToken(bankAccount, idempotencyKey, stripeAccountId, callback)
    }


    fun createSourceToken(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val sourceParams: SourceParams

        val sourceType = call.getInt("sourceType")
        val amount = call.getFloat("amount")!!.toLong()
        val currency = call.getString("currency")
        val returnURL = call.getString("returnURL")
        val card = call.getString("card")
        val name = call.getString("name")
        val statementDescriptor = call.getString("statementDescriptor")
        val bank = call.getString("bank")
        val iban = call.getString("iban")
        val addressLine1 = call.getString("address_line1")
        val city = call.getString("city")
        val zip = call.getString("address_zip")
        val country = call.getString("country")
        val email = call.getString("email")
        val callId = call.getString("callId")

        val idempotencyKey = call.getString("idempotencyKey")
        val stripeAccountId = call.getString("stripeAccountId")

        when (sourceType) {
            0 -> sourceParams = SourceParams.createThreeDSecureParams(amount, currency, returnURL, card)

            1 -> sourceParams = SourceParams.createGiropayParams(amount, name, returnURL, statementDescriptor)

            2 -> sourceParams = SourceParams.createIdealParams(amount, name, returnURL, statementDescriptor, bank)

            3 -> sourceParams = SourceParams.createSepaDebitParams(name, iban, addressLine1, city, zip, country)

            4 -> sourceParams = SourceParams.createSofortParams(amount, returnURL, country, statementDescriptor)

            5 -> sourceParams = SourceParams.createAlipaySingleUseParams(amount, currency, name, email, returnURL)

            6 -> sourceParams = SourceParams.createAlipayReusableParams(currency, name, email, returnURL)

            7 -> sourceParams = SourceParams.createP24Params(amount, currency, name, email, returnURL)

            8 -> sourceParams = SourceParams.createVisaCheckoutParams(callId)

            else -> return
        }

        val callback = object : ApiResultCallback<Source> {
            override fun onSuccess(result: Source) {
                val tokenJs = JSONObject()
                tokenJs.putOpt("id", result.id)
                tokenJs.putOpt("created", result.created)
                tokenJs.putOpt("type", result.type)
                call.success(tokenJs)
            }

            override fun onError(e: Exception) {
                call.error("unable to create source token: " + e.localizedMessage, e)
            }
        }

        stripeInstance.createSource(sourceParams, idempotencyKey, stripeAccountId, callback)
    }


    fun createAccountToken(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val legalEntity = call.getObject("legalEntity")
        val tosShownAndAccepted = call.getBoolean("tosShownAndAccepted")

        val idempotencyKey = call.getString("idempotencyKey")
        val stripeAccountId = call.getString("stripeAccountId")

        var address: Address? = null

        if (legalEntity.has("address")) {
            try {
                val addressJson = legalEntity.getJSONObject("address")
                address = Address.fromJson(addressJson)
            } catch (err: JSONException) {
                Log.w(TAG, "failed to parse address from legal entity object")
                Log.w(TAG, err)
                Log.w(TAG, "submitting request without an address")
            }
        }

        var verifyFront: String? = null;
        var verifyBack: String? = null;

        if (legalEntity.has("verification")) {
            val verifyObj = legalEntity.getJSONObject("verification")
            verifyFront = verifyObj.getString("front")
            verifyBack = verifyObj.getString("back")
        }

        val params: AccountParams
        val hasVerify = verifyFront != null || verifyBack != null

        when (legalEntity.getString("businessType")) {
            "company" -> {
                val builder = AccountParams.BusinessTypeParams.Company.Builder()
                    .setAddress(address)
                    .setName(legalEntity.getString("name"))
                    .setPhone(legalEntity.getString("phone"))

                if (hasVerify) {
                    val verifyDoc = AccountParams.BusinessTypeParams.Company.Document(verifyFront, verifyBack)
                    val verify = AccountParams.BusinessTypeParams.Company.Verification(verifyDoc)
                    builder.setVerification(verify)
                }

                params = AccountParams.create(tosShownAndAccepted, builder.build())
                Log.d(TAG, "preparing account params for company")
            }
            "individual" -> {
                val builder = AccountParams.BusinessTypeParams.Individual.Builder()
                    .setFirstName(legalEntity.getString("first_name"))
                    .setLastName(legalEntity.getString("last_name"))
                    .setEmail(legalEntity.getString("email"))
                    .setGender(legalEntity.getString("gender"))
                    .setIdNumber(legalEntity.getString("id_number"))
                    .setPhone(legalEntity.getString("phone"))
                    .setSsnLast4(legalEntity.getString("ssn_last4"))
                    .setAddress(address)

                if (hasVerify) {
                    val verifyDoc = AccountParams.BusinessTypeParams.Individual.Document(verifyFront, verifyBack)
                    val verify = AccountParams.BusinessTypeParams.Individual.Verification(verifyDoc)
                    builder.setVerification(verify)
                }

                params = AccountParams.create(tosShownAndAccepted, builder.build())
                Log.d(TAG, "preparing account params for individual")
            }
            else -> {
                params = AccountParams.create(tosShownAndAccepted)
                Log.d(TAG, "preparing account param with no no details other than acceptance")
            }
        }

        val callback = object : ApiResultCallback<Token> {
            override fun onSuccess(result: Token) {
                Log.d(TAG, "account token was successfully created")
                val res = JSONObject()
                res.putOpt("token", result.id)
                call.success(res)
            }

            override fun onError(e: Exception) {
                Log.d(TAG, "failed to create account token")
                call.error("unable to create account token: " + e.localizedMessage, e)
            }
        }

        stripeInstance.createAccountToken(params, idempotencyKey, stripeAccountId, callback)
    }

    fun createPiiToken(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val pii = call.getString("pii")
        val idempotencyKey = call.getString("idempotencyKey")
        val stripeAccountId = call.getString("stripeAccountId")
        val callback = object : ApiResultCallback<Token> {
            override fun onSuccess(result: Token) {
                val res = JSONObject()
                res.putOpt("id", result.id)
                call.success(res)
            }

            override fun onError(e: Exception) {
                call.error("unable to create pii token: " + e.localizedMessage, e)
            }
        }

        stripeInstance.createPiiToken(pii, idempotencyKey, stripeAccountId, callback)
    }

    fun confirmPaymentIntent(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val clientSecret = call.getString("clientSecret")
        val saveMethod = call.getBoolean("saveMethod", false)
        val redirectUrl = call.getString("redirectUrl")
        val stripeAccountId = call.getNullableString("stripeAccountId")
        val session = if(call.getString("setupFutureUsage") == "on_session")  ConfirmPaymentIntentParams.SetupFutureUsage.OnSession  else ConfirmPaymentIntentParams.SetupFutureUsage.OffSession
        var setupFutureUsage = if(saveMethod!!) session else null
        val params: ConfirmPaymentIntentParams

        this.cordova.setActivityResultCallback(this);

        when {
            call.hasOption("card") -> {
                var card = call.getObject("card")
                var address = Address.Builder()
                    .setLine1(card.optString("address_line1"))
                    .setLine2(card.optString("address_line2"))
                    .setCity(card.optString("address_city"))
                    .setState(card.optString("address_state"))
                    .setCountry(card.optString("address_country"))
                    .setPostalCode(card.optString("address_zip"))
                    .build()
                var billing_details = PaymentMethod.BillingDetails().toBuilder()
                    .setEmail(card.optString("email"))
                    .setName(card.optString("name"))
                    .setPhone(card.optString("phone"))
                    .setAddress(address)
                    .build()
                val cardParams = buildCard(card)
                    .build()
                    .toPaymentMethodParamsCard()
                val pmCreateParams = PaymentMethodCreateParams.create(cardParams, billing_details)
                params = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(pmCreateParams, clientSecret, redirectUrl, saveMethod!!, setupFutureUsage=setupFutureUsage)
            }

            call.hasOption("paymentMethodId") -> {
                params = ConfirmPaymentIntentParams.createWithPaymentMethodId(call.getString("paymentMethodId"), clientSecret, redirectUrl, saveMethod!!)
            }

            call.hasOption("sourceId") -> {
                params = ConfirmPaymentIntentParams.createWithSourceId(call.getString("sourceId"), clientSecret, redirectUrl, saveMethod!!)
            }

            call.hasOption("googlePayOptions") -> {
                val opts = call.getObject("googlePayOptions")
                val cb = object : GooglePayCallback() {
                    override fun onSuccess(res: PaymentData) {
                        try {
                            val pmParams = PaymentMethodCreateParams.createFromGooglePay(JSONObject(res.toJson()))
                            val confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(pmParams, clientSecret, redirectUrl, saveMethod)
                            stripeInstance.confirmPayment(activity, confirmParams, stripeAccountId)
                        } catch (e: JSONException) {
                            savedCall?.error("unable to parse json: " + e.localizedMessage, e)
                            freeSavedCall()
                        }
                    }

                    override fun onError(err: Exception) {
                        savedCall?.error(err.localizedMessage, err)
                        freeSavedCall()
                    }

                }
                saveCall(call)
                val optsJobject = JSObject.fromJSONObject(opts);
                processGooglePayTx(optsJobject, cb)
                return
            }

            call.hasOption("applePayOptions") -> {
                call.error("ApplePay is not supported on Android")
                return
            }

            else -> {
                params = ConfirmPaymentIntentParams.create(clientSecret, redirectUrl)
            }
        }

        stripeInstance.confirmPayment(activity, params, stripeAccountId)
        saveCall(call)
    }


    fun confirmSetupIntent(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val clientSecret = call.getString("clientSecret")
        val redirectUrl = call.getString("redirectUrl")

        val params: ConfirmSetupIntentParams

        when {
            call.hasOption("card") -> {
                val cb = buildCard(call.getObject("card"))
                val cardParams = cb.build().toPaymentMethodParamsCard()
                val pmCreateParams = PaymentMethodCreateParams.create(cardParams)
                params = ConfirmSetupIntentParams.create(pmCreateParams, clientSecret, redirectUrl)
            }

            call.hasOption("paymentMethodId") -> {
                params = ConfirmSetupIntentParams.create(call.getString("paymentMethodId"), clientSecret, redirectUrl)
            }

            else -> {
                params = ConfirmSetupIntentParams.createWithoutPaymentMethod(clientSecret, redirectUrl)
            }
        }

        this.cordova.setActivityResultCallback(this);
        stripeInstance.confirmSetupIntent(activity, params)
        saveCall(call)
    }


    fun customizePaymentAuthUI(call: PluginCall) {
        call.resolve()
    }


    fun isGooglePayAvailable(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }


        val paymentsClient = Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(if (isTest) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION)
                .build()
        )

        val allowedAuthMethods = JSONArray()
        allowedAuthMethods.put("PAN_ONLY")
        allowedAuthMethods.put("CRYPTOGRAM_3DS")

        val allowedCardNetworks = JSONArray()
        allowedCardNetworks.put("AMEX")
        allowedCardNetworks.put("DISCOVER")
        allowedCardNetworks.put("JCB")
        allowedCardNetworks.put("MASTERCARD")
        allowedCardNetworks.put("VISA")

        val isReadyToPayRequestJson = JSONObject()
        isReadyToPayRequestJson.putOpt("allowedAuthMethods", allowedAuthMethods)
        isReadyToPayRequestJson.putOpt("allowedCardNetworks", allowedCardNetworks)

        val req = IsReadyToPayRequest.fromJson(isReadyToPayRequestJson.toString())
        paymentsClient.isReadyToPay(req)
            .addOnCompleteListener { task ->
                val obj = JSONObject()
                obj.putOpt("available", task.isSuccessful)
                call.success(obj)
            }
    }


    fun startGooglePayTransaction(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }


        val isTest = this.isTest
        val env = if (isTest) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION

        Log.d(TAG, "startGooglePayTransaction | isTest: " + (if (isTest) "TRUE" else "FALSE") + " | env: " + if (env == WalletConstants.ENVIRONMENT_TEST) "TEST" else "PROD")

        val paymentsClient = Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(env)
                .build()
        )

        try {
            // PAN_ONLY, CRYPTOGRAM_3DS
            val defaultAuthMethods = JSONArray()
            defaultAuthMethods.put("PAN_ONLY")
            defaultAuthMethods.put("CRYPTOGRAM_3DS")

            val defaultCardNetworks = JSONArray()
            defaultCardNetworks.put("AMEX")
            defaultCardNetworks.put("DISCOVER")
            defaultCardNetworks.put("JCB")
            defaultCardNetworks.put("MASTERCARD")
            defaultCardNetworks.put("VISA")

            val totalPrice = call.getString("totalPrice")
            val totalPriceStatus = call.getString("totalPriceStatus")
            val currencyCode = call.getString("currencyCode")
            val merchantName = call.getString("merchantName")

            val emailRequired = call.getBoolean("emailRequired", false)
            val billingAddressRequired = call.getBoolean("billingAddressRequired", false)
            val allowPrepaidCards = call.getBoolean("allowPrepaidCards", true)
            val shippingAddressRequired = call.getBoolean("shippingAddressRequired", false)

            val authMethods = call.getArray("allowedAuthMethods", defaultAuthMethods)
            val cardNetworks = call.getArray("allowedCardNetworks", defaultCardNetworks)

            val billingAddressParams = call.getObject("billingAddressParams", JSONObject())

            if (!billingAddressParams.has("format")) {
                billingAddressParams.putOpt("format", "MIN")
            }

            if (!billingAddressParams.has("phoneNumberRequired")) {
                billingAddressParams.putOpt("phoneNumberRequired", false)
            }

            val shippingAddressParams = call.getObject("shippingAddressParameters", JSONObject())

            val params = JSONObject()
                .putOpt("allowedAuthMethods", authMethods)
                .putOpt("allowedCardNetworks", cardNetworks)
                .putOpt("billingAddressRequired", billingAddressRequired)
                .putOpt("allowPrepaidCards", allowPrepaidCards)
                .putOpt("billingAddressParameters", billingAddressParams)

            val tokenizationSpec = GooglePayConfig(publishableKey).tokenizationSpecification

            val cardPaymentMethod = JSONObject()
                .putOpt("type", "CARD")
                .putOpt("parameters", params)
                .putOpt("tokenizationSpecification", tokenizationSpec)

            val txInfo = JSONObject()
            txInfo.putOpt("totalPrice", totalPrice)
            txInfo.putOpt("totalPriceStatus", totalPriceStatus)
            txInfo.putOpt("currencyCode", currencyCode)

            val paymentDataReq = JSONObject()
                .putOpt("apiVersion", 2)
                .putOpt("apiVersionMinor", 0)
                .putOpt("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
                .putOpt("transactionInfo", txInfo)
                .putOpt("emailRequired", emailRequired)

            if (merchantName != null) {
                paymentDataReq.putOpt("merchantInfo", JSONObject().putOpt("merchantName", merchantName))
            }

            if (shippingAddressRequired!!) {
                paymentDataReq.putOpt("shippingAddressRequired", true)
                paymentDataReq.putOpt("shippingAddressParameters", shippingAddressParams)
            }

            val paymentDataReqStr = paymentDataReq.toString()

            Log.d(TAG, "payment data is: $paymentDataReqStr")

            val req = PaymentDataRequest.fromJson(paymentDataReqStr)

            AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(req),
                activity,
                LOAD_PAYMENT_DATA_REQUEST_CODE
            )

            saveCall(call)
        } catch (e: JSONException) {
            call.error("json parsing error: " + e.localizedMessage, e)
        }

    }


    fun initCustomerSession(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        try {
            CustomerSession.initCustomerSession(context, EphKeyProvider(call.data.toString()))
            customerSession = CustomerSession.getInstance()

            call.resolve()
        } catch (e: java.lang.Exception) {
            call.error("unable to init customer session: " + e.localizedMessage, e)
        }
    }


    fun customerPaymentMethods(call: PluginCall) {
        if (!ensurePluginInitialized(call)) {
            return
        }

        val cs = customerSession

        if (cs == null) {
            call.error("you must call initCustomerSession first")
            return
        }

        val l = StripePaymentMethodsListener(callback = object : PaymentMethodsCallback() {
            override fun onSuccess(paymentMethods: List<PaymentMethod>) {
                val arr = JSONArray()

                for (pm in paymentMethods) {
                    val obj = JSONObject()
                    obj.putOpt("created", pm.created)
                    obj.putOpt("customerId", pm.customerId)
                    obj.putOpt("id", pm.id)
                    obj.putOpt("livemode", pm.liveMode)
                    obj.putOpt("type", pm.type)

                    if (pm.card != null) {
                        val co = JSONObject()
                        val c: PaymentMethod.Card = pm.card!!
                        co.putOpt("brand", c.brand)

                        if (c.checks != null) {
                            co.putOpt("checks", JSONObject()
                                .putOpt("address_line1_check", c.checks!!.addressLine1Check)
                                .putOpt("address_postal_code_check", c.checks!!.addressPostalCodeCheck)
                                .putOpt("cvc_check", c.checks!!.cvcCheck)
                            )
                        }

                        co.putOpt("country", c.country)
                        co.putOpt("exp_month", c.expiryMonth)
                        co.putOpt("exp_year", c.expiryYear)
                        co.putOpt("funding", c.funding)
                        co.putOpt("last4", c.last4)

                        if (c.threeDSecureUsage != null) {
                            co.put("three_d_secure_usage", JSONObject().putOpt("supported", c.threeDSecureUsage!!.isSupported))
                        }

                        obj.put("card", co)
                    }

                    arr.put(obj)
                }

                val res = JSONObject()
                res.put("paymentMethods", arr)
                call.success(res)
            }

            override fun onError(err: Exception) {
                call.error(err.localizedMessage, err)
            }
        })

        cs.getPaymentMethods(PaymentMethod.Type.Card, l)
    }


    fun setCustomerDefaultSource(call: PluginCall) {
        if (customerSession == null) {
            call.error("you must call initCustomerSession first")
            return
        }

        val sourceId = call.getString("sourceId")
        val type = call.getString("type", "card")

        if (sourceId == null) {
            call.error("you must provide a sourceId")
            return
        }

        customerSession!!.setCustomerDefaultSource(sourceId, type, object : CustomerSession.CustomerRetrievalListener {
            override fun onCustomerRetrieved(customer: Customer) {
                call.success()
            }

            override fun onError(errorCode: Int, errorMessage: String, stripeError: StripeError?) {
                call.error(errorMessage, java.lang.Exception(errorMessage))
            }
        })
    }


    fun addCustomerSource(call: PluginCall) {
        if (customerSession == null) {
            call.error("you must call initCustomerSession first")
            return
        }

        val sourceId = call.getString("sourceId")
        val type = call.getString("type", "card")

        if (sourceId == null) {
            call.error("you must provide a sourceId")
            return
        }

        customerSession!!.addCustomerSource(sourceId, type, object : CustomerSession.SourceRetrievalListener {
            override fun onSourceRetrieved(source: Source) {
                call.success()
            }

            override fun onError(errorCode: Int, errorMessage: String, stripeError: StripeError?) {
                call.error(errorMessage, java.lang.Exception(errorMessage))
            }
        })
    }


    fun deleteCustomerSource(call: PluginCall) {
        if (customerSession == null) {
            call.error("you must call initCustomerSession first")
            return
        }

        val sourceId = call.getString("sourceId")

        if (sourceId == null) {
            call.error("you must provide a sourceId")
            return
        }

        customerSession!!.deleteCustomerSource(sourceId, object : CustomerSession.SourceRetrievalListener {
            override fun onSourceRetrieved(source: Source) {
                call.success()
            }

            override fun onError(errorCode: Int, errorMessage: String, stripeError: StripeError?) {
                call.error(errorMessage, java.lang.Exception(errorMessage))
            }
        })
    }

    /**
     * Ensures that setPublishableKey was called and stripeInstance exists.
     * Rejects the call with an error and returns false if the plugin is not ready.
     *
     * @param call {PluginCall} current method call
     * @return {boolean} returns true if the plugin is ready
     */
    private fun ensurePluginInitialized(call: PluginCall): Boolean {
        if (!::stripeInstance.isInitialized) {
            call.error("you must call setPublishableKey to initialize the plugin before calling this method")
            return false
        }

        return true
    }

    private fun handleGooglePayActivityResult(resultCode: Int, data: Intent?) {
        Log.v(TAG, "handleGooglePayActivityResult called with resultCode: $resultCode")

        if (googlePayCallback == null) {
            Log.e(TAG, "GooglePay :: got a result but there is no callback saved")
            return
        }

        val cb = googlePayCallback!!
        googlePayCallback = null

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (data == null) {
                    Log.e(TAG, "GooglePay :: result was ok but data was null")
                    cb.onError(Exception("unexpected error occurred"))
                    return
                }

                val paymentData = PaymentData.getFromIntent(data)

                if (paymentData == null) {
                    Log.e(TAG, "GooglePay :: result was ok but PaymentData was null")
                    cb.onError(Exception("unexpected error occurred"))
                    return
                }

                cb.onSuccess(paymentData)
            }

            Activity.RESULT_CANCELED, AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(data)

                if (status != null) {
                    cb.onError(Exception(status.statusMessage))
                } else {
                    cb.onError(Exception("transaction was cancelled"))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "handleOnActivityResult called with request code: $requestCode and resultCode: $resultCode")

        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            Log.d(TAG, "requestCode matches GooglePay, forwarding data to handleGooglePayActivityResult")
            handleGooglePayActivityResult(resultCode, data)
            return
        }

        val call = savedCall

        if (call == null) {
            Log.d(TAG, "could not find a saved PluginCall, discarding activity result")
            return
        }

        Log.d(TAG, "passing activity result to stripe")

        stripeInstance.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                Log.d(TAG, "onPaymentResult.onSuccess called")
                val pi = result.intent
                val res = paymentIntentToJSON(pi)
                call.success(res)
            }

            override fun onError(e: Exception) {
                Log.d(TAG, "onPaymentResult.onError called")
                call.error("unable to complete transaction: " + e.localizedMessage, e)
            }
        })

        stripeInstance.onSetupResult(requestCode, data, object : ApiResultCallback<SetupIntentResult> {
            override fun onSuccess(result: SetupIntentResult) {
                Log.d(TAG, "onSetupResult.onSuccess called")
                val si = result.intent
                val res = setupIntentToJSON(si)
                call.success(res)
            }

            override fun onError(e: Exception) {
                Log.d(TAG, "onSetupResult.onError called")
                call.error("unable to complete transaction: " + e.localizedMessage, e)
            }
        })
    }

    private fun processGooglePayTx(opts: JSObject, callback: GooglePayCallback) {
        val env = GetGooglePayEnv(isTest)

        Log.d(TAG, "initGooglePay :: [Testing = $isTest] :: [ENV = $env]")

        val paymentsClient = GooglePayPaymentsClient(context, env)

        try {
            val paymentDataReq = GooglePayDataReq(publishableKey, opts)

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "initGooglePay :: [payment data = $paymentDataReq]")
            }

            val req = PaymentDataRequest.fromJson(paymentDataReq)

            googlePayCallback = callback

            AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(req),
                activity,
                LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse json object", e)
            callback.onError(e)
        }
    }
}