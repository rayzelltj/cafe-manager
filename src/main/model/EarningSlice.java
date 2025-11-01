package main.model;

import java.math.BigDecimal;

public final class EarningSlice {
    public final EarningType type;
    public final BigDecimal hours;
    public final BigDecimal rate;
    public final BigDecimal subtotalHourlyPay;

    public EarningSlice(EarningType type, BigDecimal hours, BigDecimal rate) {
        this.type = type;
        this.hours = hours;
        this.rate = rate;
        this.subtotalHourlyPay = this.hours.multiply(this.rate);
    }

    public BigDecimal getSubtotalHourlyPay() {
        return subtotalHourlyPay;
    }
}
