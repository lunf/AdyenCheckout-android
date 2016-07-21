package adyen.com.adyenpaysdk.services;

import adyen.com.adyenpaysdk.pojo.CardPaymentData;

/**
 * Created by andrei on 11/10/15.
 */
public interface PaymentService {

    void fetchPublicKey(final boolean isTestMode, final String cseToken, final PaymentServiceImpl.VolleyCallback callback);

    void encryptCardData(final String publicKey, final CardPaymentData cardPaymentData, final PaymentServiceImpl.VolleyCallback callback);

    boolean luhnCheck(final String cardNumber);
}
