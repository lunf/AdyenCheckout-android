package adyen.com.adyenpaysdk.pojo;

import java.math.BigDecimal;

import adyen.com.adyenpaysdk.util.Currency;

/**
 * Created by andrei on 12/21/15.
 */
public class CheckoutMerchantRequest {

    private String paymentData;
    private BigDecimal amount;
    private Currency currency;


    public String getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(String paymentData) {
        this.paymentData = paymentData;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

}
