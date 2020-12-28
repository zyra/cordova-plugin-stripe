import Foundation
import Stripe
import PassKit

@objc(CordovaStripe)
public class CordovaStripe: CDVPlugin {
    internal var applePayCtx: ApplePayContext?
    internal var ephemeralKey: NSDictionary?
    internal var customerCtx: STPCustomerContext?
    internal var paymentCtx: STPPaymentContext?
    internal var pCfg: STPPaymentConfiguration?
    
    internal var ERR_NO_ACTIVE_CUSTOMER_CTX = "No active customer session was found. You must crete one by calling initCustomerSession"
    
    @objc func setPublishableKey(_ command: CDVInvokedUrlCommand) {
        let call = parseCommand(command);
        let value = call.getString("key") ?? ""
        var pluginResult:CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "you must provide a valid key")
        
        if value != "" {
            StripeAPI.defaultPublishableKey = value;
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        }
        
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func validateCardNumber(_ command: CDVInvokedUrlCommand) {
        let number: String? = parseCommand(command, String.self);
        let state = STPCardValidator.validationState(
                forNumber: number,
                validatingCardBrand: false
        )

        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
            "valid": state == STPCardValidationState.valid
        ])
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func validateExpiryDate(_ command: CDVInvokedUrlCommand) {
        let call = parseCommand(command);
        let state = STPCardValidator.validationState(
                forExpirationYear: call.getString("exp_year") ?? "",
                inMonth: call.getString("exp_month") ?? ""
        )

        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
                "valid": state == STPCardValidationState.valid
        ])
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func validateCVC(_ command: CDVInvokedUrlCommand) {
        let call = parseCommand(command);
        let state = STPCardValidator.validationState(
                forCVC: (call.getString("cvc")) ?? "",
                cardBrand: strToBrand(call.getString("brand"))
        )

        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
                "valid": state == STPCardValidationState.valid
        ])
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func identifyCardBrand(_ command: CDVInvokedUrlCommand) {
        let call = parseCommand(command);
        let val = STPCardValidator.brand(forNumber: call.getString("number") ?? "")

        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
            "brand": brandToStr(val)
        ])
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func validateCard(_ command: CDVInvokedUrlCommand) {
        let call = parseCommand(command);
       
        let stateNumber = STPCardValidator.validationState(
                forNumber: call.getString("number"),
                validatingCardBrand: false
        )
        let stateExpDate = STPCardValidator.validationState(
            forExpirationYear: (call.getInt("exp_year") != nil) ? String(call.getInt("exp_year")!) : "",
            inMonth: (call.getInt("exp_month") != nil) ? String(call.getInt("exp_month")!) : ""
        )
        let stateCvc = STPCardValidator.validationState(
                forCVC: call.getString("cvc") ?? "",
                cardBrand: STPCardValidator.brand(forNumber: call.getString("number") ?? "")
        )
        
        var pluginResult: CDVPluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK, 
                messageAs: "success"
        )
        
        if (stateNumber != STPCardValidationState.valid) {
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR, 
                messageAs: "card's number is invalid"
            )
        }
        else if (stateExpDate != STPCardValidationState.valid) {
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR, 
                messageAs: "expiration date is invalid"
            )
        }
        else if ((call.getString("cvc") != nil) && stateCvc != STPCardValidationState.valid) {
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR, 
                messageAs: "security code is invalid"
            )
            return
        }
        
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }


    @objc func createCardToken(_ command: CDVInvokedUrlCommand) {
        let call = parseCommand(command);
        if !ensurePluginInitialized(self.commandDelegate, command) {
            return
        }

        let params = cardParams(fromCall: call)

        STPAPIClient.shared.createToken(withCard: params) { (token, error) in
            var pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
            guard let token = token else {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "unable to create token: " + error!.localizedDescription)
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                return
            }
            
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: token.allResponseFields)
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc func createBankAccountToken(_ command: CDVInvokedUrlCommand) {
        let call = parseCommand(command);
        if !ensurePluginInitialized(self.commandDelegate, command) {
            return
        }

        let params = makeBankAccountParams(call: call)

        STPAPIClient.shared.createToken(withBankAccount: params) { (token, error) in
            var pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
            guard let token = token else {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "unable to create bank account token: " + error!.localizedDescription)
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                return
            }

            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: token.allResponseFields)
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc func payWithApplePay(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;

//        let call = parseCommand(command);
//        let paymentRequest: PKPaymentRequest!
//        var pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
//
//        do {
//            paymentRequest = try applePayOpts(call: call)
//        } catch let err {
//            call.error("unable to parse apple pay options: " + err.localizedDescription, err)
//            return
//        }
//
//        if let authCtrl = PKPaymentAuthorizationViewController(paymentRequest: paymentRequest) {
//            authCtrl.delegate = self
//            call.save()
//
//            self.applePayCtx = ApplePayContext(callbackId: call.callbackId, mode: .Token, completion: nil, clientSecret: nil)
//
//            DispatchQueue.main.async {
//                self.bridge.viewController.present(authCtrl, animated: true, completion: nil)
//            }
//            return
//        }
//
//        call.error("invalid payment request")
    }

    @objc func cancelApplePay(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        guard let ctx = self.applePayCtx else {
//            call.error("there is no existing Apple Pay transaction to cancel")
//            return
//        }
//
//        if let c = ctx.completion {
//            c(PKPaymentAuthorizationResult(status: .failure, errors: nil))
//        }
//
//        if let oldCallback = self.bridge.getSavedCall(ctx.callbackId) {
//            self.bridge.releaseCall(oldCallback)
//        }
//
//        self.applePayCtx = nil
//        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
    }

    @objc func finalizeApplePayTransaction(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        guard let ctx = self.applePayCtx else {
//            call.error("there is no existing Apple Pay transaction to finalize")
//            return
//        }
//
//        let success = call.getBool("success") ?? false
//
//        if let c = ctx.completion {
//            let s: PKPaymentAuthorizationStatus = success ? .success : .failure
//            c(PKPaymentAuthorizationResult(status: s, errors: nil))
//            let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
//        } else {
//            call.error("unable to complete the payment")
//        }
//
//        self.clearApplePay()
    }

    @objc func createSourceToken(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        if !ensurePluginInitialized(self.commandDelegate, command) {
//            return
//        }
//
//        call.error("not implemented")
//        // TODO implement
//        /*
//        let type = call.getInt("sourceType")
//
//        if type == nil {
//            call.error("you must provide a source type")
//            return
//        }
//
//        let sourceType = STPSourceType.init(rawValue: type!)
//
//        if sourceType == nil {
//            call.error("invalid source type")
//            return
//        }
//
//        let params: STPSourceParams
//
//        switch sourceType!
//        {
//        case .threeDSecure:
//            UInt(bitPattern: <#T##Int#>)
//            let amount = UInt.init(: call.getInt("amount", 0)) ?? 0
//            params = STPSourceParams.threeDSecureParams(
//                withAmount: amount,
//                currency: call.getString("currency"),
//                returnURL: call.getString("returnURL"),
//                card: call.getString("card"))
//        case .bancontact:
//            <#code#>
//        case .card:
//            <#code#>
//        case .giropay:
//            <#code#>
//        case .IDEAL:
//            <#code#>
//        case .sepaDebit:
//            <#code#>
//        case .sofort:
//            <#code#>
//        case .alipay:
//            <#code#>
//        case .P24:
//            <#code#>
//        case .EPS:
//            <#code#>
//        case .multibanco:
//            <#code#>
//        case .weChatPay:
//
//        case .unknown:
//
//        }
//       */
    }

    @objc func createPiiToken(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        if !ensurePluginInitialized(self.commandDelegate, command) {
//            return
//        }
//
//        let pii = call.getString("pii") ?? ""
//
//        STPAPIClient.shared.createToken(withPersonalIDNumber: pii) { (token, error) in
//            guard let token = token else {
//                call.error("unable to create token: " + error!.localizedDescription, error)
//                return
//            }
//
//            call.resolve([
//                "token": token.tokenId
//            ])
//        }
    }

    @objc func createAccountToken(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        if !ensurePluginInitialized(self.commandDelegate, command) {
//            return
//        }
//
//        call.error("not implemented")
//
//        // TODO implement
    }

    @objc func confirmPaymentIntent(_ command: CDVInvokedUrlCommand) {
        let call = parseCommand(command);
        if !ensurePluginInitialized(self.commandDelegate, command) {
            return
        }
        
        let clientSecret = call.getString("clientSecret")
        
        var pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
        if clientSecret == nil || clientSecret == "" {
             pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "you must provide a client secret")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }


        if call.hasOption("applePayOptions") {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Apple Pay is not implemented yet")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
            
//            let paymentRequest: PKPaymentRequest!
//
//            do {
//                paymentRequest = try applePayOpts(call: call)
//            } catch let err {
//                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "unable to parse apple pay options: " + err.localizedDescription)
//                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
//                return
//            }
//
//            if let authCtrl = PKPaymentAuthorizationViewController(paymentRequest: paymentRequest) {
//                authCtrl.delegate = self
//                call.save()
//                self.applePayCtx = ApplePayContext(callbackId: command.callbackId,
//                                                   mode: .PaymentIntent,
//                                                   completion: nil,
//                                                   clientSecret: clientSecret)
//
//                DispatchQueue.main.async {
//                    self.bridge.viewController.present(authCtrl,
//                                                       animated: true,
//                                                       completion: nil)
//                }
//                return
//            }
//
//            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "invalid payment request")
//            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
//            return
            
        } else if call.hasOption("googlePayOptions") {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "GooglePay is not supported on iOS")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        let pip: STPPaymentIntentParams = STPPaymentIntentParams.init(clientSecret: clientSecret!)

        if let sm = call.getBool("saveMethod"), sm == true {
            pip.savePaymentMethod = true
        }

//        if call.hasOption("redirectUrl") {
//            pip.returnURL = call.getString("redirectUrl")
//        }

        if call.hasOption("card") {
            let bd = STPPaymentMethodBillingDetails()
            bd.address = address(addressDict(fromCall: call))

            let cObj = call.getObject("card") ?? [:]
            let cpp = cardParams(fromObj: cObj)
            cpp.address = STPAddress.init(paymentMethodBillingDetails: bd)
            let pmp = STPPaymentMethodParams.init(card: STPPaymentMethodCardParams.init(cardSourceParams: cpp), billingDetails: bd, metadata: nil)
            pip.paymentMethodParams = pmp

        } else if call.hasOption("paymentMethodId") {
            pip.paymentMethodId = call.getString("paymentMethodId")
        } else if call.hasOption("sourceId") {
            pip.sourceId = call.getString("sourceId")
        }

        let pm = STPPaymentHandler.shared()
        STPAPIClient.shared.publishableKey = StripeAPI.defaultPublishableKey
        pm.confirmPayment(pip, with: self) { (status, pi, err) in
            switch status {
            case .failed:
                if err != nil {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "payment failed: " + err!.localizedDescription)
                } else {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "user cancelled the transaction")
                }
                
            case .canceled:
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "user cancelled the transaction")

            case .succeeded:
                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: pi!.allResponseFields)
            }
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc func confirmSetupIntent(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
//
//        let call = parseCommand(command);
//        if !ensurePluginInitialized(self.commandDelegate, command) {
//            return
//        }
//
//        let clientSecret = call.getString("clientSecret")
//
//        if clientSecret == nil || clientSecret == "" {
//            call.error("you must provide a client secret")
//            return
//        }
//
//        let pip: STPSetupIntentConfirmParams = STPSetupIntentConfirmParams.init(clientSecret: clientSecret!)
//
//        if call.hasOption("redirectUrl") {
//           pip.returnURL = call.getString("redirectUrl")
//        }
//
//        if call.hasOption("card") {
//            let bd = STPPaymentMethodBillingDetails()
//            bd.address = address(addressDict(fromCall: call))
//
//            let cObj = call.getObject("card") ?? [:]
//            let cpp = cardParams(fromObj: cObj)
//            cpp.address = STPAddress.init(paymentMethodBillingDetails: bd)
//            let pmp = STPPaymentMethodParams.init(card: STPPaymentMethodCardParams.init(cardSourceParams: cpp), billingDetails: bd, metadata: nil)
//            pip.paymentMethodParams = pmp
//
//        } else if call.hasOption("paymentMethodId") {
//            pip.paymentMethodID = call.getString("paymentMethodId")
//        }
//
//        let pm = STPPaymentHandler.shared()
//
//        pm.confirmSetupIntent(pip, with: self) { (status, si, err) in
//            switch status {
//            case .failed:
//                if err != nil {
//                    call.error("payment failed: " + err!.localizedDescription, err)
//                } else {
//                    call.error("payment failed")
//                }
//
//            case .canceled:
//                call.error("user cancelled the transaction")
//
//            case .succeeded:
//                let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
//            }
//        }
    }

    @objc func initCustomerSession(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
//        // TODO: Fix CustomerKeyProvider extension
//        let call = parseCommand(command);
//        guard
//                let id = call.getString("id"),
//                let object = call.getString("object"),
//                let associatedObjects = call.getArray("associated_objects", [String: String].self),
//                let created = call.getInt("created"),
//                let expires = call.getInt("expires"),
//                let livemode = call.getBool("livemode"),
//                let secret = call.getString("secret")
//        else {
//            let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "invalid ephemeral options")
//            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
//            return;
//        }
//
//        self.ephemeralKey = [
//            "id": id,
//            "object": object,
//            "associated_objects": associatedObjects,
//            "created": created,
//            "expires": expires,
//            "livemode": livemode,
//            "secret": secret
//        ]
//
//        let ctx = STPCustomerContext(keyProvider: self)
//        let pCfg = STPPaymentConfiguration.shared
//
//        if let po = call.getObject("paymentOptions") as? [String: Bool] {
//            if po["applePay"] ?? false {
//                pCfg.applePayEnabled = true
//            }
//            if po["fpx"] ?? false {
//                pCfg.fpxEnabled = true
//            }
//            if po["default"] ?? false {
//                // TODO The additionalPaymentOptions is no longer an object, and has been deprecated
//                // There are clear alternatives for the other usages, but this does not seem to have a clear alternative.
//                // I believe that the alternative is to simply not do anything, but I cannot find any explanations of what this did before the switch to stripe 21
//             //   pCfg.additionalPaymentOptions.insert(.default)
//            }
//        }
//
//        let rbaf = call.getString("requiredBillingAddressFields")
//
//        switch rbaf {
//        case "full":
//            pCfg.requiredBillingAddressFields = .full
//        case "zip":
//            pCfg.requiredBillingAddressFields = .postalCode
//        case "name":
//            pCfg.requiredBillingAddressFields = .name
//        default:
//            pCfg.requiredBillingAddressFields = .none
//        }
//
//        if call.getString("shippingType") ?? "" == "delivery" {
//            pCfg.shippingType = .delivery
//        }
//
//        if let ac = call.getArray("availableCountries", String.self) {
//            pCfg.availableCountries = Set(ac)
//        }
//
//        if let cn = call.getString("companyName") {
//            pCfg.companyName = cn
//        }
//
//        if let amid = call.getString("appleMerchantIdentifier") {
//            pCfg.appleMerchantIdentifier = amid
//        }
//
//        self.customerCtx = ctx
//        self.pCfg = pCfg
//
//        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
//        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func presentPaymentOptions(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        guard let pCfg = self.pCfg, let ctx = self.customerCtx else {
//            call.error(ERR_NO_ACTIVE_CUSTOMER_CTX)
//            return
//        }
//
//        let pCtx = STPPaymentContext(customerContext:  ctx,
//                                     configuration: pCfg,
//                                     theme: STPTheme.default())
//
//        DispatchQueue.main.async {
//            pCtx.delegate = self
//            pCtx.hostViewController = self.bridge.viewController
//            pCtx.presentPaymentOptionsViewController()
//        }
//
//        call.save()
    }

    @objc func presentShippingOptions(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
//
//        let call = parseCommand(command);
//        guard let pCfg = self.pCfg, let ctx = self.customerCtx else {
//            call.error(ERR_NO_ACTIVE_CUSTOMER_CTX)
//            return
//        }
//
//        let pCtx = STPPaymentContext(customerContext: ctx,
//                                     configuration: pCfg,
//                                     theme: STPTheme.default())
//
//        DispatchQueue.main.async {
//            pCtx.delegate = self
//            pCtx.hostViewController = self.bridge.viewController
//            pCtx.presentShippingViewController()
//        }
    }

    @objc func presentPaymentRequest(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        guard let pCfg = self.pCfg, let ctx = self.customerCtx else {
//            call.error(ERR_NO_ACTIVE_CUSTOMER_CTX)
//            return
//        }
//
//        let pCtx = STPPaymentContext(customerContext: ctx,
//                                     configuration: pCfg,
//                                     theme: STPTheme.default())
//
//        DispatchQueue.main.async {
//            pCtx.delegate = self
//            pCtx.hostViewController = self.bridge.viewController
//            pCtx.paymentAmount = 5151
//            pCtx.requestPayment()
//        }
    }

    @objc func customizePaymentAuthUI(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
    }

    @objc func initPaymentSession(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;

//        // TODO: Fix CustomerKeyProvider extension
//        let call = parseCommand(command);
//        var pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
//        guard let ctx = self.customerCtx else {
//            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: ERR_NO_ACTIVE_CUSTOMER_CTX)
//            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
//            return
//        }
//
//        self.paymentCtx = STPPaymentContext(customerContext: ctx)
//        self.paymentCtx!.delegate = self
//        self.paymentCtx!.hostViewController = self.viewController
//
//        if let amount = call.getInt("paymentAmount") {
//            self.paymentCtx!.paymentAmount = amount
//        }
//
//        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
//        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func customerPaymentMethods(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        guard let ctx = self.customerCtx else {
//            call.error(ERR_NO_ACTIVE_CUSTOMER_CTX)
//            return
//        }
//
//        ctx.listPaymentMethodsForCustomer { methods, error in
//            guard let methods = methods else {
//                call.error(error?.localizedDescription ?? "unknown error")
//                return
//            }
//
//            var vals: [[String: Any]] = []
//
//            for m in methods {
//                let val = pmToJSON(m: m)
//                vals.append(val)
//            }
//
//            let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
//                "paymentMethods": vals,
//            ])
//        }
    }

    @objc func setCustomerDefaultSource(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not supported on iOS")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
    }

    @objc func addCustomerSource(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        guard let ctx = self.customerCtx else {
//            call.error(ERR_NO_ACTIVE_CUSTOMER_CTX)
//            return
//        }
//
//        guard let pm = STPPaymentMethod.decodedObject(fromAPIResponse: [
//            "type": call.getString("type") as Any,
//            "id": call.getString("sourceId") as Any,
//        ]) else {
//            call.error("failed to decode object as a PaymentMethod")
//            return
//        }
//
//        ctx.attachPaymentMethod(toCustomer: pm, completion: { (err) in
//            if (err != nil) {
//                call.error(err!.localizedDescription)
//                return
//            }
//
//            self.customerPaymentMethods(call)
//        })
    }

    @objc func deleteCustomerSource(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        guard let ctx = self.customerCtx else {
//            call.error(ERR_NO_ACTIVE_CUSTOMER_CTX)
//            return
//        }
//
//        guard let pm = STPPaymentMethod.decodedObject(fromAPIResponse: [
//                   "id": call.getString("sourceId") as Any,
//               ]) else {
//                   call.error("failed to decode object as a PaymentMethod")
//                   return
//               }
//
//        ctx.detachPaymentMethod(fromCustomer: pm) { (err) in
//            if (err != nil) {
//                call.error(err!.localizedDescription)
//                return
//            }
//
//            self.customerPaymentMethods(call)
//        }
    }

    @objc func isApplePayAvailable(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
//            "available": Stripe.deviceSupportsApplePay()
//        ])
//        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func isGooglePayAvailable(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Not implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
        
//        let call = parseCommand(command);
//        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: [
//            "available": false
//        ])
//        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc func payWithGooglePay(_ command: CDVInvokedUrlCommand) {
        let pluginResult: CDVPluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Google Pay is not available on iOS")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        return;
    }

    @objc internal func clearApplePay() {
        return
//        guard let ctx = self.applePayCtx else {
//            return
//        }
//
//        if let c = self.bridge.getSavedCall(ctx.callbackId) {
//            self.bridge.releaseCall(c)
//        }
//
//        self.applePayCtx = nil
    }
}
