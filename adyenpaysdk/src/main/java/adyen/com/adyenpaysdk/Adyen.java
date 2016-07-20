package adyen.com.adyenpaysdk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import adyen.com.adyenpaysdk.exceptions.EncrypterException;
import adyen.com.adyenpaysdk.exceptions.NoPublicKeyExeption;
import adyen.com.adyenpaysdk.pojo.CardPaymentData;
import adyen.com.adyenpaysdk.services.PaymentService;
import adyen.com.adyenpaysdk.services.PaymentServiceImpl;
import adyen.com.adyenpaysdk.util.ClientSideEncrypter;
import adyen.com.adyenpaysdk.util.Luhn;


/**
 * Created by andrei on 11/5/15.
 */
public class Adyen {

    private static Adyen mInstance = null;

    private static final String tag = Adyen.class.getSimpleName();

    private boolean useTestBackend = false;
    private String token;
    private String publicKey;
    private static Context mContext;

    public interface CompletionCallback {

        void onSuccess(String result);

        void onError(String error);

    }

    private Adyen(Context context) {
        mContext = context;
    }

    public static Adyen getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new Adyen(context);
        }
        return mInstance;
    }

    public void fetchPublicKey(final CompletionCallback completion) {
        PaymentService paymentService = new PaymentServiceImpl(mContext);
        String host = (useTestBackend) ? "test" : "live";
        String url = String.format("https://%s.adyen.com/hpp/cse/%s/json.shtml", host, token);
        paymentService.fetchPublicKey(url, new PaymentServiceImpl.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    publicKey = result.getString("publicKey");
                    completion.onSuccess(publicKey);
                } catch (JSONException e) {
                    Log.e(tag, e.getMessage(), e);
                    completion.onError(e.getMessage());
                }
            }

            @Override
            public void onError(String resultCode, String message) {
                completion.onError(message);
            }
        });
    }

    public String encryptData(String data) throws NoPublicKeyExeption, EncrypterException {
        String encryptedData = null;
        if(!TextUtils.isEmpty(publicKey)) {
            try {
                ClientSideEncrypter encrypter = new ClientSideEncrypter(publicKey);
                encryptedData = encrypter.encrypt(data);
            } catch (EncrypterException e) {
                throw e;
            }
        } else {
            throw new NoPublicKeyExeption("No public key was found!");
        }

        return encryptedData;
    }

    public boolean luhnCheck(String cardNumber) {
        return Luhn.check(cardNumber);
    }

    public boolean isUseTestBackend() {
        return useTestBackend;
    }

    public void setUseTestBackend(boolean useTestBackend) {
        this.useTestBackend = useTestBackend;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String serialize(CardPaymentData cardPaymentData) throws EncrypterException, NoPublicKeyExeption {
        JSONObject cardJson = new JSONObject();
        String encryptedData = null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            cardJson.put("generationtime", simpleDateFormat.format(cardPaymentData.getGenerationTime()));
            cardJson.put("number", cardPaymentData.getNumber());
            cardJson.put("holderName", cardPaymentData.getCardHolderName());
            cardJson.put("cvc", cardPaymentData.getCvc());
            cardJson.put("expiryMonth", cardPaymentData.getExpiryMonth());
            cardJson.put("expiryYear", cardPaymentData.getExpiryYear());
            encryptedData = encryptData(cardJson.toString());
        } catch (JSONException e) {
            Log.e(tag, e.getMessage(), e);
        }


        return encryptedData;
    }
}
