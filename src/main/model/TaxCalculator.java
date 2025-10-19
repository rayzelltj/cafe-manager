package main.model;

import main.tax.TaxConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tax calculator implementing CRA Option 1 formula for federal and BC provincial tax calculations
 * Based on CRA T4008 Payroll Deductions Supplementary Tables
 */

public class TaxCalculator {
    private static final RoundingMode RM = RoundingMode.HALF_UP;
    private static final BigDecimal PERIODS = BigDecimal.valueOf(TaxConstants.PAY_PERIODS_BIWEEKLY);

    public static BigDecimal federalPerPeriodFromAnnual(
            BigDecimal annualTaxableIncome,
            BigDecimal annualCppContributionProjected, // BASE CPP annual for K2 (already capped by caller)
            BigDecimal annualEiContributionProjected,  // EI annual for K2 (0 if EI-exempt; already capped by caller)
            BigDecimal annualGrossEmployment,          // for K4 min()
            boolean eiExempt,
            TaxConfig cfg) {

        // 1) Annual basic tax on A using cfg for the first bracket rate
        BigDecimal annualBasicTax = calculateProgressiveFederalAnnual(annualTaxableIncome, cfg);

        // 2) K1: lowest federal rate * BPA
        BigDecimal k1 = cfg.fedBPA.multiply(cfg.lowFedRate);

        // 3) K2: lowest federal rate * (BASE CPP annual + EI annual)
        BigDecimal baseCppAnnual = annualCppContributionProjected.min(cfg.cppMaxContribution);
        BigDecimal eiAnnual = eiExempt ? BigDecimal.ZERO : annualEiContributionProjected.min(cfg.eiMaxContribution);
        BigDecimal k2 = (baseCppAnnual.add(eiAnnual)).multiply(cfg.lowFedRate);

        // 4) K4: lowest federal rate * min(annual gross employment, CEA)
        BigDecimal cea = cfg.canadaEmploymentAmount; // 2025: 1471
        BigDecimal k4 = annualGrossEmployment.min(cea).multiply(cfg.lowFedRate);

        // 5) Net annual federal tax
        BigDecimal t3Annual = annualBasicTax.subtract(k1).subtract(k2).subtract(k4);
        if (t3Annual.signum() < 0) t3Annual = BigDecimal.ZERO;

        // 6) Per-period withholding
        return t3Annual.divide(BigDecimal.valueOf(TaxConstants.PAY_PERIODS_BIWEEKLY), 2, RoundingMode.HALF_UP);
    }

    /** Helper: progressive federal using annual brackets */
    private static BigDecimal calculateProgressiveFederalAnnual(BigDecimal income, TaxConfig cfg) {
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal remaining = income;

        // First bracket at cfg.lowFedRate (0.15 before Jul 1, 0.14 from Jul 1, 2025)
        BigDecimal b1 = cfg.fedT1.min(remaining);
        tax = tax.add(b1.multiply(cfg.lowFedRate));
        remaining = remaining.subtract(b1);
        if (remaining.signum() <= 0) return tax;

        BigDecimal b2 = cfg.fedT2.subtract(cfg.fedT1).min(remaining);
        tax = tax.add(b2.multiply(cfg.fedRate2));
        remaining = remaining.subtract(b2);
        if (remaining.signum() <= 0) return tax;

        BigDecimal b3 = cfg.fedT3.subtract(cfg.fedT2).min(remaining);
        tax = tax.add(b3.multiply(cfg.fedRate3));
        remaining = remaining.subtract(b3);
        if (remaining.signum() <= 0) return tax;

        BigDecimal b4 = cfg.fedT4.subtract(cfg.fedT3).min(remaining);
        tax = tax.add(b4.multiply(cfg.fedRate4));
        remaining = remaining.subtract(b4);
        if (remaining.signum() <= 0) return tax;

        tax = tax.add(remaining.multiply(cfg.fedRate5));
        return tax;
    }


    public static BigDecimal provincialPerPeriod(BigDecimal grossPerPeriodTaxable,
                                                 BigDecimal perPeriodCpp,
                                                 BigDecimal perPeriodEi,
                                                 boolean eiExempt,
                                                 TaxConfig cfg) {
        // Per-period thresholds from cfg (BC has 7 brackets -> 6 thresholds)
        BigDecimal t1 = cfg.bcT1.divide(PERIODS, 10, RM);
        BigDecimal t2 = cfg.bcT2.divide(PERIODS, 10, RM);
        BigDecimal t3 = cfg.bcT3.divide(PERIODS, 10, RM);
        BigDecimal t4 = cfg.bcT4.divide(PERIODS, 10, RM);
        BigDecimal t5 = cfg.bcT5.divide(PERIODS, 10, RM);
        BigDecimal t6 = cfg.bcT6.divide(PERIODS, 10, RM);

        // Progressive tax using cfg rates/thresholds
        BigDecimal tax = calculateProgressiveBCPerPeriod(
                grossPerPeriodTaxable, t1, t2, t3, t4, t5, t6, cfg
        );

        // Non-refundable credits at the lowest provincial rate
        BigDecimal bpaPerPeriod = cfg.bcBPA.divide(PERIODS, 10, RM);
        BigDecimal creditBpa     = bpaPerPeriod.multiply(cfg.bcRate1);

        // CPP credit uses BASE CPP portion only
        BigDecimal basePortion   = perPeriodCpp.multiply(cfg.cppBaseRate.divide(cfg.cppTotalRate, 10, RM));
        BigDecimal creditCpp     = cfg.bcRate1.multiply(basePortion);

        // (BC doesn’t give a separate EI credit—keep zero even if not exempt)
        BigDecimal creditEi      = eiExempt ? BigDecimal.ZERO : cfg.bcRate1.multiply(perPeriodEi);

        BigDecimal afterCredits  = tax.subtract(creditBpa).subtract(creditCpp).subtract(creditEi);
        if (afterCredits.signum() < 0) afterCredits = BigDecimal.ZERO;

        return afterCredits.setScale(2, RM);
    }

    /** 7-bracket progressive for BC, fully cfg-driven */
    private static BigDecimal calculateProgressiveBCPerPeriod(
            BigDecimal income,
            BigDecimal t1, BigDecimal t2, BigDecimal t3,
            BigDecimal t4, BigDecimal t5, BigDecimal t6,
            TaxConfig cfg
    ) {
        BigDecimal tax = BigDecimal.ZERO, rem = income;

        // Bracket 1
        BigDecimal b1 = t1.min(rem);
        tax = tax.add(b1.multiply(cfg.bcRate1));
        rem = rem.subtract(b1);
        if (rem.signum() <= 0) return tax;

        // Bracket 2
        BigDecimal b2 = t2.subtract(t1).min(rem);
        tax = tax.add(b2.multiply(cfg.bcRate2));
        rem = rem.subtract(b2);
        if (rem.signum() <= 0) return tax;

        // Bracket 3
        BigDecimal b3 = t3.subtract(t2).min(rem);
        tax = tax.add(b3.multiply(cfg.bcRate3));
        rem = rem.subtract(b3);
        if (rem.signum() <= 0) return tax;

        // Bracket 4
        BigDecimal b4 = t4.subtract(t3).min(rem);
        tax = tax.add(b4.multiply(cfg.bcRate4));
        rem = rem.subtract(b4);
        if (rem.signum() <= 0) return tax;

        // Bracket 5
        BigDecimal b5 = t5.subtract(t4).min(rem);
        tax = tax.add(b5.multiply(cfg.bcRate5));
        rem = rem.subtract(b5);
        if (rem.signum() <= 0) return tax;

        // Bracket 6
        BigDecimal b6 = t6.subtract(t5).min(rem);
        tax = tax.add(b6.multiply(cfg.bcRate6));
        rem = rem.subtract(b6);
        if (rem.signum() <= 0) return tax;

        // Bracket 7
        tax = tax.add(rem.multiply(cfg.bcRate7));
        return tax;
    }

    /**
     * Helper: compute F5A annual factor from per-period CPP (F5A = F5_per_period * periods where F5_per_period = C * (0.0100/0.0595))
     */
    public static BigDecimal f5aFromPerPeriodCpp(BigDecimal perPeriodCpp) {
        BigDecimal f5PerPeriod = perPeriodCpp.multiply(BigDecimal.valueOf(0.0100 / 0.0595));
        return f5PerPeriod.multiply(PERIODS).setScale(2, RM);
    }
}
