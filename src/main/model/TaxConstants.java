package main.model;
import java.math.BigDecimal;
/**
 * Tax constants for 2025 Canadian federal and BC provincial tax calculations
 * Based on CRA T4008 Payroll Deductions Supplementary Tables
 */
public final class TaxConstants {
    private TaxConstants() {}


    public static final int PAY_PERIODS_BIWEEKLY = 26;

    // Federal (2025 PDOC values for Option 1)
    public static final BigDecimal FEDERAL_BPA = BigDecimal.valueOf(16129.00);
    public static final BigDecimal FEDERAL_T1 = BigDecimal.valueOf(57375.00);
    public static final BigDecimal FEDERAL_T2 = BigDecimal.valueOf(114750.00);
    public static final BigDecimal FEDERAL_T3 = BigDecimal.valueOf(177882.00);
    public static final BigDecimal FEDERAL_T4 = BigDecimal.valueOf(253414.00);
    public static final BigDecimal FEDERAL_RATE_1 = BigDecimal.valueOf(0.15);
    public static final BigDecimal FEDERAL_RATE_2 = BigDecimal.valueOf(0.205);
    public static final BigDecimal FEDERAL_RATE_3 = BigDecimal.valueOf(0.26);
    public static final BigDecimal FEDERAL_RATE_4 = BigDecimal.valueOf(0.29);
    public static final BigDecimal FEDERAL_RATE_5 = BigDecimal.valueOf(0.33);

    // BC (2025 PDOC values for Option 1)
    public static final BigDecimal BC_BPA = BigDecimal.valueOf(12932.00);
    public static final BigDecimal BC_T1 = BigDecimal.valueOf(49279.00);
    public static final BigDecimal BC_T2 = BigDecimal.valueOf(98560.00);
    public static final BigDecimal BC_T3 = BigDecimal.valueOf(113158.00);
    public static final BigDecimal BC_T4 = BigDecimal.valueOf(137407.00);
    public static final BigDecimal BC_T5 = BigDecimal.valueOf(186306.00);
    public static final BigDecimal BC_T6 = BigDecimal.valueOf(259829.00);
    public static final BigDecimal BC_RATE_1 = BigDecimal.valueOf(0.0506);
    public static final BigDecimal BC_RATE_2 = BigDecimal.valueOf(0.0770);
    public static final BigDecimal BC_RATE_3 = BigDecimal.valueOf(0.1050);
    public static final BigDecimal BC_RATE_4 = BigDecimal.valueOf(0.1229);
    public static final BigDecimal BC_RATE_5 = BigDecimal.valueOf(0.1470);
    public static final BigDecimal BC_RATE_6 = BigDecimal.valueOf(0.1680);
    public static final BigDecimal BC_RATE_7 = BigDecimal.valueOf(0.2050);

    // CPP
    public static final BigDecimal CPP_ANNUAL_BASIC_EXEMPTION = BigDecimal.valueOf(3500.00);
    public static final BigDecimal CPP_RATE = BigDecimal.valueOf(0.0595);
    public static final BigDecimal CPP_BASE_RATE = BigDecimal.valueOf(0.0495);
    public static final BigDecimal CPP_ADDITIONAL_RATE = BigDecimal.valueOf(0.01);
    public static final BigDecimal CPP_MAX_CONTRIBUTION = BigDecimal.valueOf(3356.10);
    public static final BigDecimal CPP_MAX_ADDITIONAL_CONTRIBUTION = BigDecimal.valueOf(396.00);
    public static final BigDecimal CPP_MAX_PENSIONABLE_EARNINGS = BigDecimal.valueOf(71300.00);

    // EI
    public static final BigDecimal EI_RATE = BigDecimal.valueOf(0.0164);
    public static final BigDecimal EI_MAX_INSURABLE_EARNINGS = BigDecimal.valueOf(65700.00);
    public static final BigDecimal EI_MAX_CONTRIBUTION = BigDecimal.valueOf(1077.48);

    // BC tax reduction
    public static final BigDecimal BC_TAX_REDUCTION_THRESHOLD_1 = BigDecimal.valueOf(25020.00);
    public static final BigDecimal BC_TAX_REDUCTION_THRESHOLD_2 = BigDecimal.valueOf(40807.00);
    public static final BigDecimal BC_TAX_REDUCTION_AMOUNT = BigDecimal.valueOf(562.00);
    public static final BigDecimal BC_TAX_REDUCTION_RATE = BigDecimal.valueOf(0.0356);

    // Vacation
    public static final BigDecimal VACATION_PAY_RATE = BigDecimal.valueOf(0.04);
}