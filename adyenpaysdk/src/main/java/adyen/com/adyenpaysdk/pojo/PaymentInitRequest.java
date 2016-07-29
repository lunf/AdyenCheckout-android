package adyen.com.adyenpaysdk.pojo;

import java.math.BigDecimal;

import adyen.com.adyenpaysdk.util.Currency;

/**
 * Created by andrei on 12/9/15.
 */
public class PaymentInitRequest {

    private BigDecimal checkoutAmount;
    private Currency currency;
    private int brandColor;
    private int brandLogo;
    private String token;
    private boolean testBackend;


    public BigDecimal getCheckoutAmount() {
        return checkoutAmount;
    }

    public void setCheckoutAmount(BigDecimal checkoutAmount) {
        this.checkoutAmount = checkoutAmount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public int getBrandColor() {
        return brandColor;
    }

    public void setBrandColor(int brandColor) {
        this.brandColor = brandColor;
    }

    public int getBrandLogo() {
        return brandLogo;
    }

    public void setBrandLogo(int brandLogo) {
        this.brandLogo = brandLogo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isTestBackend() {
        return testBackend;
    }

    public void setTestBackend(boolean testBackend) {
        this.testBackend = testBackend;
    }
}
