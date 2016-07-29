package adyen.com.adyenuisdk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Date;

import adyen.com.adyenpaysdk.AdyenSdk;
import adyen.com.adyenpaysdk.exceptions.CheckoutRequestException;
import adyen.com.adyenpaysdk.pojo.CardPaymentData;
import adyen.com.adyenpaysdk.pojo.PaymentInitRequest;
import adyen.com.adyenpaysdk.pojo.CheckoutMerchantRequest;
import adyen.com.adyenpaysdk.util.Currency;
import adyen.com.adyenuisdk.customcomponents.AdyenEditText;
import adyen.com.adyenuisdk.listener.AdyenCheckoutListener;
import adyen.com.adyenuisdk.listener.EditTextImeBackListener;
import adyen.com.adyenuisdk.util.ColorUtil;

/**
 * Created by andrei on 11/5/15.
 */
public class PaymentActivity extends Activity {

    private static final String tag = PaymentActivity.class.getSimpleName();

    private TextView mPaymentAmount;
    private RelativeLayout mPayButton;
    private LinearLayout mPaymentForm;
    private LinearLayout mMerchantLogo;
    private LinearLayout mMainLayout;
    private ImageView mMerchantLogoImage;

    private AdyenEditText mCreditCardNo;
    private AdyenEditText mCreditCardExpDate;
    private AdyenEditText mCreditCardCvc;

    ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    InputMethodManager inputMethodManager;

    private static AdyenCheckoutListener adyenCheckoutListener;

    private Bundle extras;
    private AdyenSdk mAdyenSdk;

    private ProgressDialog mProgressDialog;
    private String mPublicKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.payment_form);

        extras = getIntent().getExtras();

        mPaymentAmount = (TextView)findViewById(R.id.credit_card_pay);
        mPayButton = (RelativeLayout)findViewById(R.id.pay_button);
        mPaymentForm = (LinearLayout)findViewById(R.id.payment_form_layout);
        mMerchantLogo = (LinearLayout)findViewById(R.id.merchant_logo_layout);

        mMainLayout = (LinearLayout)findViewById(R.id.main_layout);

        mCreditCardNo = (AdyenEditText)findViewById(R.id.credit_card_no);
        mCreditCardExpDate = (AdyenEditText)findViewById(R.id.credit_card_exp_date);
        mCreditCardCvc = (AdyenEditText)findViewById(R.id.credit_card_cvc);

        mMerchantLogoImage = (ImageView)findViewById(R.id.merchantLogoImage);
        mMerchantLogoImage.setImageResource(extras.getInt("logo"));

        showInputKeyboard();
        initPaymentButtonText();
        initPaymentButton();
        initAdyenEditTextListeners();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().
                setStatusBarColor(Color.parseColor(
                        ColorUtil.changeColorHSB(getResources().getString(extras.getInt("backgroundColor")))));
        }


        mAdyenSdk = new AdyenSdk(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading. Please wait.....");
        mProgressDialog.show();

        String cseToken = extras.getString("token");
        boolean isTestMode = extras.getBoolean("useTestBackend");
        getPublicKey(isTestMode, cseToken);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideInputKeyboard();
    }

    private void hideInputKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void showInputKeyboard() {
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        calculateKeyboardHeight();
    }

    public void calculateKeyboardHeight() {
        mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect layoutRectangle = new Rect();
                mMainLayout.getWindowVisibleDisplayFrame(layoutRectangle);

                int screenHeight = mMainLayout.getRootView().getHeight();
                int heightDifference = screenHeight - (layoutRectangle.bottom - layoutRectangle.top);

                if(heightDifference > 500) {
                    Log.i(tag, "Logo height: " + (layoutRectangle.bottom - mPaymentForm.getHeight()));
                    setLogoLayoutHeight(layoutRectangle.bottom - mPaymentForm.getHeight());
                }

            }
        };

        mPaymentForm.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    private void setLogoLayoutHeight(int logoHeight) {
        ViewGroup.LayoutParams layoutParams = mMerchantLogo.getLayoutParams();
        layoutParams.height = logoHeight;
        mMerchantLogo.setLayoutParams(layoutParams);
        mPaymentForm.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    public static class PaymentActivityBuilder {
        Bundle arguments;
        PaymentInitRequest paymentInitRequest;

        public PaymentActivityBuilder(PaymentInitRequest request) throws CheckoutRequestException {
            arguments = new Bundle();
            paymentInitRequest = request;
            initPaymentFragment();
        }

        private void initPaymentFragment() throws CheckoutRequestException {
            if(paymentInitRequest.getBrandColor() != 0) {
                arguments.putInt("backgroundColor", paymentInitRequest.getBrandColor());
            } else {
                throw new CheckoutRequestException("Brand color is not set! Please set the brand color.");
            }

            if(paymentInitRequest.getBrandLogo() != 0) {
                arguments.putInt("logo", paymentInitRequest.getBrandLogo());
            } else {
                throw new CheckoutRequestException("Brand logo is not set! Please set the brand logo.");
            }

            if(paymentInitRequest.getCheckoutAmount().intValue() > 0) {
                arguments.putString("amount", paymentInitRequest.getCheckoutAmount().toString());
            } else {
                throw new CheckoutRequestException("Amount is not set! Please set the amount.");
            }

            if(paymentInitRequest.getCurrency() != null && !TextUtils.isEmpty(paymentInitRequest.getCurrency().toString())) {
                arguments.putString("currency", paymentInitRequest.getCurrency().toString());
            } else {
                throw new CheckoutRequestException("Currency is not set! Please set the currency.");
            }

            if(!TextUtils.isEmpty(paymentInitRequest.getToken())) {
                arguments.putString("token", paymentInitRequest.getToken());
            } else {
                throw new CheckoutRequestException("Token is not set! Please set the token.");
            }

            arguments.putBoolean("useTestBackend", paymentInitRequest.isTestBackend());
        }

        public Intent build(AdyenCheckoutListener listener, Context context) {
            adyenCheckoutListener = listener;
            Intent intent = new Intent(context, PaymentActivity.class);
            intent.putExtras(arguments);
            return intent;
        }
    }

    public void initPaymentButtonText() {
        String currencyCode = extras.getString("currency");
        String currencySign = getCurrencySign(currencyCode);
        if(currencyCode.equals(Currency.USD.toString())) {
            mPaymentAmount.setText(mPaymentAmount.getText()
                    + " " + currencySign + " " + String.valueOf(String.format("%.02f", extras.getFloat("amount"))));
        } else {
            mPaymentAmount.setText(mPaymentAmount.getText()
                    + " " + String.valueOf(String.format("%.02f", extras.getFloat("amount"))) + " " + currencySign);
        }
    }

    private String getCurrencySign(String currencyCode) {
        return Currency.valueOf(currencyCode).getCurrencySign();
    }

    public void initPaymentButton() {
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CreditCardForm.isValid() && !mPublicKey.isEmpty()) {
                    hideInputKeyboard();
                    mProgressDialog.show();
                    encryptPaymentData(mPublicKey);

                }
            }
        });
    }

    private void getPublicKey(boolean isTestMode, String cseToken) {


        mAdyenSdk.fetchPublicKey(isTestMode, cseToken, new AdyenSdk.CompletionCallback() {
            @Override
            public void onSuccess(String result) {
                mPublicKey = result;
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(String error) {
                mProgressDialog.dismiss();
                adyenCheckoutListener.checkoutFailedWithError(error);
            }
        });
    }

    private void encryptPaymentData(String publicKey) {

        final CardPaymentData cardPaymentData = buildCardData();

        mAdyenSdk.encryptCard(publicKey, cardPaymentData, new AdyenSdk.CompletionCallback() {
            @Override
            public void onSuccess(String result) {
                mProgressDialog.dismiss();
                CheckoutMerchantRequest checkoutMerchantRequest = new CheckoutMerchantRequest();
                checkoutMerchantRequest.setPaymentData(result);
                BigDecimal amount = new BigDecimal(extras.getString("amount"));
                checkoutMerchantRequest.setAmount(amount);
                checkoutMerchantRequest.setCurrency(Currency.valueOf(extras.getString("currency")));
                adyenCheckoutListener.checkoutAuthorizedPayment(checkoutMerchantRequest);

            }

            @Override
            public void onError(String error) {
                mProgressDialog.dismiss();
                adyenCheckoutListener.checkoutFailedWithError(error);
            }
        });

    }
    private CardPaymentData buildCardData() {
        CardPaymentData cardPaymentData = new CardPaymentData();

        cardPaymentData.setCardHolderName("test");
        cardPaymentData.setCvc(mCreditCardCvc.getText().toString());
        cardPaymentData.setExpiryMonth(mCreditCardExpDate.getText().toString().split("/")[0]);
        cardPaymentData.setExpiryYear("20" + mCreditCardExpDate.getText().toString().split("/")[1]);
        cardPaymentData.setGenerationTime(new Date());
        cardPaymentData.setNumber(mCreditCardNo.getText().toString());

        return cardPaymentData;
    }

    private void initAdyenEditTextListeners() {
        mCreditCardNo.setOnEditTextImeBackListener(new EditTextImeBackListener() {
            @Override
            public void onImeBack(AdyenEditText ctrl, String text) {
                finish();
            }
        });

        mCreditCardExpDate.setOnEditTextImeBackListener(new EditTextImeBackListener() {
            @Override
            public void onImeBack(AdyenEditText ctrl, String text) {
                finish();
            }
        });

        mCreditCardCvc.setOnEditTextImeBackListener(new EditTextImeBackListener() {
            @Override
            public void onImeBack(AdyenEditText ctrl, String text) {
                finish();
            }
        });
    }

    /*
    * Used for unit testing
    */
    public LinearLayout getmPaymentForm() {
        return mPaymentForm;
    }
}
