package main.model;
import main.tax.TaxConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PayStub {
    private final Employee employee;
    private final BigDecimal hoursWorked;
    private final BigDecimal hourlyRate;
    private final BigDecimal vacationPayRate;
    private final LocalDate payPeriodStart;
    private final LocalDate payPeriodEnd;

    private BigDecimal hourlyPay = BigDecimal.ZERO;
    private BigDecimal vacationPay = BigDecimal.ZERO;
    private BigDecimal grossPay = BigDecimal.ZERO;

    private BigDecimal cppDeduction = BigDecimal.ZERO;
    private BigDecimal eiDeduction = BigDecimal.ZERO;
    private BigDecimal federalTaxDeduction = BigDecimal.ZERO;
    private BigDecimal provincialTaxDeduction = BigDecimal.ZERO;
    private BigDecimal totalIncomeTaxDeduction = BigDecimal.ZERO;
    private BigDecimal netPay = BigDecimal.ZERO;

    // YTD before this pay
    private BigDecimal ytdGrossBefore = BigDecimal.ZERO;
    private BigDecimal ytdCppBefore = BigDecimal.ZERO;
    private BigDecimal ytdEiBefore = BigDecimal.ZERO;
    private BigDecimal ytdIncomeTaxBefore = BigDecimal.ZERO;

    private static final RoundingMode RM = RoundingMode.HALF_UP;


    public PayStub(Employee employee, double hoursWorked, double hourlyRate, double vacationPayRate,
                   LocalDate payPeriodStart, LocalDate payPeriodEnd) {
        this.employee = employee;
        this.hoursWorked = BigDecimal.valueOf(hoursWorked);
        this.hourlyRate = BigDecimal.valueOf(hourlyRate);
        this.vacationPayRate = BigDecimal.valueOf(vacationPayRate);
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
    }

    public void setYtdBefore(double ytdGross, double ytdCpp, double ytdEi, double ytdIncomeTax) {
        this.ytdGrossBefore = BigDecimal.valueOf(ytdGross);
        this.ytdCppBefore = BigDecimal.valueOf(ytdCpp);
        this.ytdEiBefore = BigDecimal.valueOf(ytdEi);
        this.ytdIncomeTaxBefore = BigDecimal.valueOf(ytdIncomeTax);
    }

    public void calculateAll() {
        calculateHourlyPay();
        calculateVacationPay();
        calculateGrossPay();
        calculateCPP();
        calculateEI();
        calculateIncomeTaxes();
        calculateNetPay();
    }

    private void calculateHourlyPay() {
        this.hourlyPay = this.hoursWorked.multiply(this.hourlyRate);
    }

    private void calculateVacationPay() {
        this.vacationPay = this.hourlyPay.multiply(this.vacationPayRate);
    }

    private void calculateGrossPay() {
        this.grossPay = this.hourlyPay.add(this.vacationPay).setScale(2, RM);
    }

    private void calculateCPP() {
        BigDecimal basicExemptionPerPeriod = TaxConstants.CPP_ANNUAL_BASIC_EXEMPTION
                .divide(BigDecimal.valueOf(TaxConstants.PAY_PERIODS_BIWEEKLY), 10, RM);

        BigDecimal pensionable = this.grossPay.subtract(basicExemptionPerPeriod);
        if (pensionable.compareTo(BigDecimal.ZERO) <= 0) {
            this.cppDeduction = BigDecimal.ZERO.setScale(2, RM);
            return;
        }

        BigDecimal raw = pensionable.multiply(TaxConstants.CPP_RATE);

        // Cap using remaining annual CPP contribution room (best-effort using YTD)
        BigDecimal annualMax = TaxConstants.CPP_MAX_CONTRIBUTION.add(TaxConstants.CPP_MAX_ADDITIONAL_CONTRIBUTION);
        BigDecimal remainingAnnual = annualMax.subtract(this.ytdCppBefore);
        if (remainingAnnual.compareTo(BigDecimal.ZERO) <= 0) {
            this.cppDeduction = BigDecimal.ZERO.setScale(2, RM);
            return;
        }

        BigDecimal capped = raw.min(remainingAnnual);
        this.cppDeduction = capped.setScale(2, RM);
    }

    private void calculateEI() {
        if (employee != null && employee.isEiExempt()) {
            this.eiDeduction = BigDecimal.ZERO.setScale(2, RM);
            return;
        }

        BigDecimal insurableThisPeriod = this.grossPay; // includes vacation, stat, etc., if insurable
        BigDecimal periodEI = insurableThisPeriod.multiply(TaxConstants.EI_RATE);

        BigDecimal remaining = TaxConstants.EI_MAX_CONTRIBUTION.subtract(this.ytdEiBefore);
        if (remaining.signum() <= 0) {
            this.eiDeduction = BigDecimal.ZERO.setScale(2, RM);
            return;
        }

        this.eiDeduction = periodEI.min(remaining).setScale(2, RM);
    }

    private void calculateIncomeTaxes() {
        final TaxConfig cfg = main.tax.TaxConfigProvider.forPayDate(this.payPeriodEnd);

        // ---- Annualization helpers ----
        final BigDecimal P = BigDecimal.valueOf(TaxConstants.PAY_PERIODS_BIWEEKLY);
        final boolean eiExempt = (employee != null && employee.isEiExempt());

        // Per-period statutory deductions you already computed
        BigDecimal perPeriodCpp = this.cppDeduction;   // NOTE: this is TOTAL CPP (base + additional) in your implementation
        BigDecimal perPeriodEi  = this.eiDeduction;    // 0 if EI-exempt per your earlier logic

        // Annual gross employment income (for A and later for CEA/K4)
        BigDecimal annualGross = this.grossPay.multiply(P);

        // F5A (additional CPP) estimated from total per-period CPP
        BigDecimal f5aAnnual = TaxCalculator.f5aFromPerPeriodCpp(perPeriodCpp);

        // A: annual taxable employment income (simplified for your scenario)
        BigDecimal annualTaxableIncome = annualGross.subtract(f5aAnnual);
        if (annualTaxableIncome.signum() < 0) annualTaxableIncome = BigDecimal.ZERO;

        // Per-period taxable income (for provincial step you’re using)
        BigDecimal perPeriodTaxable = annualTaxableIncome.divide(P, 10, RM);

        // ---- PDOC-style projections for K2 inputs ----

        // Project TOTAL CPP from per-period then convert to BASE CPP for K2
        BigDecimal projectedAnnualCppTotal = perPeriodCpp.multiply(P);
        BigDecimal projectedAnnualCppBase =
                projectedAnnualCppTotal
                        .multiply(TaxConstants.CPP_BASE_RATE.divide(TaxConstants.CPP_RATE, 12, RM));
        BigDecimal cappedAnnualCppBaseForK2 =
                projectedAnnualCppBase.min(TaxConstants.CPP_MAX_CONTRIBUTION);

        // Project EI (0 if EI-exempt), then cap
        BigDecimal projectedAnnualEi = eiExempt ? BigDecimal.ZERO : perPeriodEi.multiply(P);
        BigDecimal cappedAnnualEiForK2 =
                eiExempt ? BigDecimal.ZERO : projectedAnnualEi.min(TaxConstants.EI_MAX_CONTRIBUTION);

        // ---- Federal tax (per period) ----
        this.federalTaxDeduction = TaxCalculator.federalPerPeriodFromAnnual(
                annualTaxableIncome,
                cappedAnnualCppBaseForK2,
                cappedAnnualEiForK2,
                annualGross,
                eiExempt,
                cfg
        );

        // ---- Provincial tax (per period) ----
        // Your provincial method expects a per-period "taxable" base;
        // you derived perPeriodTaxable above from annual A; that’s consistent with your approach.
        BigDecimal bcBeforeCredits = TaxCalculator.provincialPerPeriod(
                perPeriodTaxable,
                this.cppDeduction,
                this.eiDeduction,
                eiExempt,
                cfg
        );

        // BC tax reduction S computed on ANNUAL taxable income and applied per-period
        BigDecimal s = calculateBCReduction(annualTaxableIncome, bcBeforeCredits);

        this.provincialTaxDeduction = bcBeforeCredits.subtract(s).max(BigDecimal.ZERO).setScale(2, RM);

        // ---- Totals ----
        this.totalIncomeTaxDeduction = this.federalTaxDeduction.add(this.provincialTaxDeduction).setScale(2, RM);
        BigDecimal totalDeductions = this.cppDeduction.add(this.eiDeduction).add(this.totalIncomeTaxDeduction);
        this.netPay = this.grossPay.subtract(totalDeductions).setScale(2, RM);
    }

    private BigDecimal calculateBCReduction(BigDecimal annualTaxableIncome, BigDecimal t4PerPeriod) {
        // Convert PDOC S calculation (annual) to per-period by computing S_annual then dividing by periods
        BigDecimal sAnnual;
        if (annualTaxableIncome.compareTo(TaxConstants.BC_TAX_REDUCTION_THRESHOLD_1) <= 0) {
            sAnnual = TaxConstants.BC_TAX_REDUCTION_AMOUNT.min(t4PerPeriod.multiply(BigDecimal.valueOf(TaxConstants.PAY_PERIODS_BIWEEKLY)));
        } else if (annualTaxableIncome.compareTo(TaxConstants.BC_TAX_REDUCTION_THRESHOLD_2) <= 0) {
            BigDecimal reduction = TaxConstants.BC_TAX_REDUCTION_AMOUNT.subtract(
                    annualTaxableIncome.subtract(TaxConstants.BC_TAX_REDUCTION_THRESHOLD_1).multiply(TaxConstants.BC_TAX_REDUCTION_RATE)
            );
            if (reduction.compareTo(BigDecimal.ZERO) < 0) reduction = BigDecimal.ZERO;
            sAnnual = reduction.min(t4PerPeriod.multiply(BigDecimal.valueOf(TaxConstants.PAY_PERIODS_BIWEEKLY)));
        } else {
            sAnnual = BigDecimal.ZERO;
        }

        return sAnnual.divide(BigDecimal.valueOf(TaxConstants.PAY_PERIODS_BIWEEKLY), 2, RM);
    }

    private void calculateNetPay() {
        BigDecimal totalDeductions = this.cppDeduction.add(this.eiDeduction).add(this.totalIncomeTaxDeduction);
        this.netPay = this.grossPay.subtract(totalDeductions).setScale(2, RM);
    }

    // Getters (double) for compatibility with existing tests
    public double getGrossPay() { return this.grossPay.doubleValue(); }
    public double getCPPDeduction() { return this.cppDeduction.doubleValue(); }
    public double getEIDeduction() { return this.eiDeduction.doubleValue(); }
    public double getFederalTaxDeduction() { return this.federalTaxDeduction.doubleValue(); }
    public double getProvincialTaxDeduction() { return this.provincialTaxDeduction.doubleValue(); }
    public double getTotalIncomeTaxDeduction() { return this.totalIncomeTaxDeduction.doubleValue(); }
    public double getNetPay() { return this.netPay.doubleValue(); }
}