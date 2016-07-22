package adyen.com.adyenuisdk.listener;

import adyen.com.adyenpaysdk.pojo.CheckoutMerchantRequest;

/**
 * Created by andrei on 12/3/15.
 */
public interface AdyenCheckoutListener {

    void checkoutAuthorizedPayment(CheckoutMerchantRequest checkoutMerchantRequest);
    void checkoutFailedWithError(String errorMessage);

}
