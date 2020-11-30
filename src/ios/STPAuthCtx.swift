import Foundation
import Stripe

extension CordovaStripe : STPAuthenticationContext {
    public func authenticationPresentingViewController() -> UIViewController {
        return self.bridge?.bridgeDelegate as! UIViewController
    }
}
