package adyen.com.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import adyen.com.adyenpaysdk.exceptions.CheckoutRequestException;
import adyen.com.adyenpaysdk.pojo.PaymentInitRequest;
import adyen.com.adyenpaysdk.pojo.CheckoutMerchantRequest;
import adyen.com.adyenpaysdk.util.Currency;
import adyen.com.adyenuisdk.PaymentActivity;
import adyen.com.adyenuisdk.listener.AdyenCheckoutListener;

public class MainActivity extends FragmentActivity implements AdyenCheckoutListener {

    private InitPaymentFragment mInitPaymentFragment;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        mInitPaymentFragment = new InitPaymentFragment();

        initView();
    }

    private void initView() {
        getSupportFragmentManager().
                beginTransaction().
                add(R.id.fragment_container, mInitPaymentFragment).
                addToBackStack(null).
                commit();
    }

    public void initPayment(View view) {
        ConfigLoader configLoader = new ConfigLoader(this);
        JSONObject configuration = configLoader.loadJsonConfiguration();
        PaymentInitRequest paymentInitRequest = new PaymentInitRequest();
        try {
            paymentInitRequest.setBrandColor(R.color.nespresso_grey);
            paymentInitRequest.setBrandLogo(R.mipmap.nespresso_logo);
            paymentInitRequest.setCheckoutAmount(new BigDecimal(10));
            paymentInitRequest.setCurrency(Currency.EUR);
            paymentInitRequest.setToken(configuration.getString("userToken"));
            paymentInitRequest.setTestBackend(true);

            Intent intent = new PaymentActivity.PaymentActivityBuilder(paymentInitRequest).build(this, context);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (CheckoutRequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void checkoutAuthorizedPayment(CheckoutMerchantRequest checkoutMerchantRequest) {
        Log.i("Response: ", checkoutMerchantRequest.toString());
        Toast.makeText(context, "Card data encrypted and ready to submit to your server", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void checkoutFailedWithError(String errorMessage) {
        Log.i("Fail: ", errorMessage);
    }
}
