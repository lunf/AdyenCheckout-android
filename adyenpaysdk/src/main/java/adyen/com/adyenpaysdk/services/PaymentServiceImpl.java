package adyen.com.adyenpaysdk.services;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import adyen.com.adyenpaysdk.R;
import adyen.com.adyenpaysdk.controllers.NetworkController;
import adyen.com.adyenpaysdk.exceptions.EncrypterException;
import adyen.com.adyenpaysdk.exceptions.NoPublicKeyExeption;
import adyen.com.adyenpaysdk.pojo.CardPaymentData;
import adyen.com.adyenpaysdk.util.ClientSideEncrypter;
import adyen.com.adyenpaysdk.util.Luhn;


/**
 * Created by andrei on 11/10/15.
 */
public class PaymentServiceImpl implements PaymentService {

    private static final String tag = PaymentServiceImpl.class.getSimpleName();

    private static final String SUCCESS = "ok";
    private Context mContext;

    public PaymentServiceImpl(Context context) {
        mContext = context;
    }

    private void fetchPublicKey(String hppUrl, final VolleyCallback callback) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, hppUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(tag, response.toString());

                        try {
                            String status = response.getString("status");
                            if(SUCCESS.equals(status)) {
                                // get public key
                                callback.onSuccess(response.getString("publicKey"));
                            }
                        } catch (JSONException e) {
                            callback.onError(mContext.getString(R.string.error_json_data));
                            Log.e(tag, e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(mContext.getString(R.string.error_network));
                        VolleyLog.d(tag, "Error: " + error.getMessage());
                    }
                });

        NetworkController networkController = new NetworkController(mContext);
        networkController.addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public void fetchPublicKey(boolean isTestMode, String cseToken, VolleyCallback callback) {
        String host = (isTestMode) ? "test" : "live";
        String url = String.format("https://%s.adyen.com/hpp/cse/%s/json.shtml", host, cseToken);
        fetchPublicKey(url, callback);
    }

    @Override
    public void encryptCardData(String publicKey, CardPaymentData cardPaymentData, VolleyCallback callback) {
        JSONObject cardJson = new JSONObject();
        String encryptedData = null;
        String jsonData = null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            cardJson.put("generationtime", simpleDateFormat.format(cardPaymentData.getGenerationTime()));
            cardJson.put("number", cardPaymentData.getNumber());
            cardJson.put("holderName", cardPaymentData.getCardHolderName());
            cardJson.put("cvc", cardPaymentData.getCvc());
            cardJson.put("expiryMonth", cardPaymentData.getExpiryMonth());
            cardJson.put("expiryYear", cardPaymentData.getExpiryYear());
            jsonData = cardJson.toString();
        } catch (JSONException e) {
            Log.e(tag, e.getMessage(), e);
        }

        if (TextUtils.isEmpty(jsonData)) {
            callback.onError("Invalid card data");
        }

        if(TextUtils.isEmpty(publicKey)) {
            callback.onError("No public key was found!");
        }

        // Do encryption
        try {
            ClientSideEncrypter encrypter = new ClientSideEncrypter(publicKey);
            encryptedData = encrypter.encrypt(jsonData);
        } catch (EncrypterException e) {
            callback.onError(e.getMessage());
        }

        if(TextUtils.isEmpty(encryptedData)) {
            callback.onError("Invalid encrypted data");
        }

        callback.onSuccess(encryptedData);

    }

    @Override
    public boolean luhnCheck(String cardNumber) {
        return Luhn.check(cardNumber);
    }

    public interface VolleyCallback {
        void onSuccess(String result);

        void onError(String message);
    }

}