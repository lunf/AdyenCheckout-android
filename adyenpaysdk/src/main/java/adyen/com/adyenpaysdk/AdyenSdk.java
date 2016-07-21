package adyen.com.adyenpaysdk;

import android.content.Context;

import adyen.com.adyenpaysdk.pojo.CardPaymentData;
import adyen.com.adyenpaysdk.services.PaymentService;
import adyen.com.adyenpaysdk.services.PaymentServiceImpl;
import adyen.com.adyenpaysdk.util.Luhn;

/**
 * This class allow another app use Adyen SCE technologies
 */

public class AdyenSdk {

    private  Context mContext;
    private PaymentService mPaymentService;

    public AdyenSdk(final Context context) {
        mContext = context;
        mPaymentService = new PaymentServiceImpl(mContext);
    }

    /**
     * This method will fetch your public key based on your CSE Token. Public Key can be found in Settings --> Users --> Webservice user
     *
     * @param isTestMode
     * @param cseToken
     * @param completion
     */
    public void fetchPublicKey(final boolean isTestMode, final String cseToken, final CompletionCallback completion) {

        mPaymentService.fetchPublicKey(isTestMode, cseToken, new PaymentServiceImpl.VolleyCallback() {
            @Override
            public void onSuccess(String publicKey) {
                    completion.onSuccess(publicKey);
            }

            @Override
            public void onError(String message) {
                completion.onError(message);
            }
        });
    }

    /**
     * This method will encrypt card data.
     *
     * @param publicKey
     * @param cardPaymentData
     * @param completion
     */
    public void encryptCard(final String publicKey, final CardPaymentData cardPaymentData, final CompletionCallback completion) {
        mPaymentService.encryptCardData(publicKey, cardPaymentData, new PaymentServiceImpl.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                completion.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                completion.onError(message);
            }
        });
    }

    /**
     * This method will fetch public key and encrypt card data based on result of fetch.
     *
     * @param isTestMode
     * @param cseToken
     * @param cardPaymentData
     * @param completion
     */
    public void fetchAndEncrypt(final boolean isTestMode, final String cseToken, final CardPaymentData cardPaymentData, final CompletionCallback completion) {

    }

    /**
     * Do luhn check on the card number
     *
     * @param cardNumber
     * @return
     */
    public boolean doLuhnCheck(String cardNumber) {
        return mPaymentService.luhnCheck(cardNumber);
    }

    public interface CompletionCallback {

        void onSuccess(String result);

        void onError(String error);

    }
}
