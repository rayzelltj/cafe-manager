package main.model;

import java.time.LocalDate;

public class PayStub {
    private static final double VACATION_RATE = 0.04;

    private static final double CPP_BASIC_EXEMPTION = 3500.0;
    private static final double CPP_MAX = 71300.0;
    private static final int CPP_MONTHS_PER_YEAR = 12;

    private static final double F5A_FIRST_TERM_CAP = 678.00;
    private static final double F5A_SECOND_TERM_CAP = 396.00;

    private static final int PAY_PERIODS_PER_YEAR = 26;

    private static final double FEDERAL_BASIC_PERSONAL_AMOUNT = 16129.00;
    private static final double CPP_CREDIT_MAX = 3356.10;
    private static final double EI_CREDIT_MAX = 1077.48;
    private package main.model;

import java.time.LocalDate;

    public class PayStub {
        private static final double VACATION_RATE = 0.04;

        private static final double CPP_BASIC_EXEMPTION = 3500.0;
        private static final double CPP_MAX = 71300.0;
        private static final int CPP_MONTHS_PER_YEAR = 12;

        private static final double F5A_FIRST_TERM_CAP = 678.00;
        private static final double F5A_SECOND_TERM_CAP = 396.00;

        private static final int PAY_PERIODS_PER_YEAR = 26;

        private static final double FEDERAL_BASIC_PERSONAL_AMOUNT = 16129.00;
        private static final double CPP_CREDIT_MAX = 3356.10;
        private static final double EI_CREDIT_MAX = 1077.48;
        private static final double CPP_RATE_BASE = 0.0495;
        private static final double CPP_RATE_TOTAL = 0.0595;
        private static final double EI_RATE = 0.0166; // 2025 employee EI rate
        private static final double CANADA_EMPLOYMENT_AMOUNT = 1432.00;

        // ===== Instance Variables =====
        private Employee employee;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private LocalDate chequeDate;

        private double hoursWorked;
        private double hourlyRate;

        private double grossHourlyPay;      // Current gross pay
        private double vacPay;              // Vacation pay paid out
        private double cpp;                 // CPP deduction
        private double federalIncomeTax;    // Deduction
        private double provincialIncomeTax; // Deduction
        private double netPay;              // Gross - deductions

        // Year-To-Date Tracking
        private double grossPayYTD;
        private double vacPayYTD;
        private double cppYTD;
        private double incomeTaxYTD;
        private double netPayYTD;

        public PayStub(Employee employee, LocalDate periodStart, LocalDate periodEnd, LocalDate chequeDate, double hoursWorked, double hourlyRate){
            this.employee = employee;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.chequeDate = chequeDate;
            this.hoursWorked = hoursWorked;
            this.hourlyRate = hourlyRate;

        }

        public void calculateAll() {
            this.grossHourlyPay = calculateGrossHourlyPay();
            this.vacPay = calculateVacPay();
            this.cpp = calculateCPP();
            this.federalIncomeTax = calculateFederalTaxPerPeriod();
            this.provincialIncomeTax = calculateProvincialTaxBC();
        }

        public double calculateGrossHourlyPay() {
            return hoursWorked * hourlyRate;
        }

        public double calculateVacPay() {
            return grossHourlyPay * VACATION_RATE;
        }

        public double calculateCPP() {
            final double CPP_RATE = 0.0595;
            final double CPP_EXEMPTION_PER_PERIOD = CPP_BASIC_EXEMPTION / PAY_PERIODS_PER_YEAR;

            return -((this.grossHourlyPay - CPP_EXEMPTION_PER_PERIOD) * CPP_RATE);
        }
        // Step 1:
        // EFFECTS: calculates annual taxable income (A)
        // A = [P × (I – F – F2 – F5A – U1 )] – HD – F1
        // P = # of pay periods in a year = 26
        // I = grossHourlyPay + vacPay
        // F = Payroll deductions for the pay period for employee contributions to a
        //     registered pension plan (RPP) for current and past services, a registered
        //     retirement savings plan (RRSP), to a pooled registered pension plan (PRPP),
        //     or a retirement compensation arrangement (RCA). For tax deduction purposes,
        //     employers can deduct amounts contributed to an RPP, RRSP, PRPP, or RCA by or
        //     on behalf of an employee to determine the employee's taxable income = 0
        // F2 = Alimony or maintenance payments required by a legal document dated before
        //      May 1, 1997, to be payroll-deducted authorized by a tax services office or tax centre
        //    = 0
        // F5A = F5* × ((PI – B)/PI)
        // U1 = 0
        // HD = Annual deduction for living in a prescribed zone, as shown on Form TD1
        //    = 0
        // F1 = Annual deductions such as child care expenses and support payments requested by an
        //    = employee or pensioner and authorized by a tax services office or tax centre
        //    = 0

        public double calculateAnnualTaxableIncome() {
            double P = PAY_PERIODS_PER_YEAR;
            double I = this.grossHourlyPay + this.vacPay;
            double F = 0;
            double F2 = 0;
            double F5A = calculateF5A();
            double U1 = 0;
            double HD = 0;
            double F1 = 0;

            return (P * (I - F - F2 - F5A - U1)) - HD - F1; // A
        }

        private double calculateF5A() {
            // Annualized total remuneration
            double I1 = (this.grossHourlyPay + this.vacPay) * PAY_PERIODS_PER_YEAR; // annual pay
            double PI = this.grossHourlyPay + this.vacPay; // pensionable income this pay period
            double B = 0;  // Non-periodic bonuses — assumed 0 unless otherwise known
            double PM = CPP_MONTHS_PER_YEAR; // 12 months/year

            // First term: 0.0100 × (I1 – 3,500), max $678 × (PM/12)
            double firstTermBase = I1 - CPP_BASIC_EXEMPTION;
            double firstTerm = 0.0100 * Math.max(firstTermBase, 0);
            double firstTermCap = 678.00 * (PM / (double) CPP_MONTHS_PER_YEAR);
            firstTerm = Math.min(firstTerm, firstTermCap);

            // Second term: 0.04 × (I1 – 71,300 × (PM/12)), max $396 × (PM/12)
            double secondTermBase = I1 - (CPP_MAX * (PM / (double) CPP_MONTHS_PER_YEAR));
            double secondTerm = 0.04 * Math.max(secondTermBase, 0);
            double secondTermCap = 396.00 * (PM / (double) CPP_MONTHS_PER_YEAR);
            secondTerm = Math.min(secondTerm, secondTermCap);

            // F5* = firstTerm + secondTerm
            double F5star = firstTerm + secondTerm;

            // F5A = F5* × ((PI – B) / PI)
            return (PI == 0) ? 0 : F5star * ((PI - B) / PI);
        }

        // Updated federal tax calculation to match PDOC and return per period tax rounded to cent
        public double calculateFederalTaxPerPeriod() {
            double A = calculateAnnualTaxableIncome();

            double R = getFederalTaxRate(A);

            // Use TD1 federal basic personal amount from your example
            double K1 = roundToTwoDecimals(0.15 * FEDERAL_BASIC_PERSONAL_AMOUNT);

            // Annualized CPP contributions at base rate for the credit, capped at max
            double annualPensionableEarnings = (grossHourlyPay + vacPay) * PAY_PERIODS_PER_YEAR;
            double cppCreditBase = (annualPensionableEarnings - CPP_BASIC_EXEMPTION) * CPP_RATE_BASE;
            cppCreditBase = Math.max(0, cppCreditBase);
            cppCreditBase = Math.min(cppCreditBase, CPP_CREDIT_MAX);
            cppCreditBase = roundToTwoDecimals(cppCreditBase);

            // Annualized EI contributions capped at max
            double annualEIContribution = annualPensionableEarnings * EI_RATE;
            annualEIContribution = Math.min(annualEIContribution, EI_CREDIT_MAX);
            annualEIContribution = roundToTwoDecimals(annualEIContribution);

            double K2 = 0.15 * (cppCreditBase + annualEIContribution);
            K2 = roundToTwoDecimals(K2);

            double K3 = 0.0;

            double K4 = Math.min(0.15 * A, 0.15 * CANADA_EMPLOYMENT_AMOUNT);
            K4 = roundToTwoDecimals(K4);

            double K = 0.0;

            double T3 = (R * A) - K - K1 - K2 - K3 - K4;
            if (T3 < 0) T3 = 0;

            // Federal tax per pay period
            double federalTaxPerPeriod = T3 / PAY_PERIODS_PER_YEAR;

            return roundToTwoDecimals(federalTaxPerPeriod);
        }

        private double getFederalTaxRate(double income) {
            // 2025 Federal Tax Brackets (approx.)
            if (income <= 55867) return 0.15;
            if (income <= 111733) return 0.205;
            if (income <= 173205) return 0.26;
            if (income <= 246752) return 0.29;
            return 0.33;
        }

        public double calculateProvincialTaxBC() {
            final double BC_TAX_RATE = 0.0506;
            final double BC_BASIC_PERSONAL_AMOUNT = 12932.00;
            final double BC_CANADA_EMPLOYMENT_AMOUNT = 1432.00;

            double A = calculateAnnualTaxableIncome();

            // KP = 0 for BC
            double KP = 0.0;

            // K1P = BC lowest tax rate * BC basic personal amount
            double K1P = BC_TAX_RATE * BC_BASIC_PERSONAL_AMOUNT;

            // Calculate annual pensionable earnings (same as federal)
            double annualPensionableEarnings = (grossHourlyPay + vacPay) * PAY_PERIODS_PER_YEAR;

            // Calculate CPP credit base for BC
            double cppCreditBase = (annualPensionableEarnings - CPP_BASIC_EXEMPTION) * CPP_RATE_BASE;
            cppCreditBase = Math.max(0, cppCreditBase);
            cppCreditBase = Math.min(cppCreditBase, CPP_CREDIT_MAX);

            // Calculate EI credit for BC
            double annualEIContribution = annualPensionableEarnings * EI_RATE;
            annualEIContribution = Math.min(annualEIContribution, EI_CREDIT_MAX);

            // K2P = BC lowest tax rate * (CPP credit + EI credit)
            double K2P = BC_TAX_RATE * (cppCreditBase + annualEIContribution);

            // K3P = 0 for your case
            double K3P = 0.0;

            // K4P = BC lowest tax rate * Canada Employment Amount
            double K4P = BC_TAX_RATE * BC_CANADA_EMPLOYMENT_AMOUNT;

            // Calculate T4
            double T4 = (BC_TAX_RATE * A) - KP - K1P - K2P - K3P - K4P;
            if (T4 < 0) T4 = 0;

            // Calculate surtax S based on A
            double S;
            if (A <= 25020) {
                S = Math.min(T4, 562);
            } else if (A <= 40807) {
                S = Math.min(T4, 562 - ((A - 25020) * 0.0356));
            } else {
                S = 0;
            }

            // V1, V2, LCP assumed zero for BC
            double V1 = 0.0;
            double V2 = 0.0;
            double LCP = 0.0;

            // Calculate T2
            double T2 = T4 + V1 + V2 - S - (PAY_PERIODS_PER_YEAR * LCP);
            if (T2 < 0) T2 = 0;

            // Provincial tax per pay period, rounded to two decimals
            return roundToTwoDecimals(T2 / PAY_PERIODS_PER_YEAR);
        }

        // Helper to round to 2 decimal places (cents)
        private double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }


    }
    static final double CPP_RATE_BASE = 0.0495;
    private static final double CPP_RATE_TOTAL = 0.0595;
    private static final double EI_RATE = 0.0166; // 2025 employee EI rate
    private static final double CANADA_EMPLOYMENT_AMOUNT = 1432.00;

    // ===== Instance Variables =====
    private Employee employee;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate chequeDate;

    private double hoursWorked;
    private double hourlyRate;

    private double grossHourlyPay;      // Current gross pay
    private double vacPay;              // Vacation pay paid out
    private double cpp;                 // CPP deduction
    private double federalIncomeTax;    // Deduction
    private double provincialIncomeTax; // Deduction
    private double netPay;              // Gross - deductions

    // Year-To-Date Tracking
    private double grossPayYTD;
    private double vacPayYTD;
    private double cppYTD;
    private double incomeTaxYTD;
    private double netPayYTD;

    public PayStub(Employee employee, LocalDate periodStart, LocalDate periodEnd, LocalDate chequeDate, double hoursWorked, double hourlyRate){
        this.employee = employee;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.chequeDate = chequeDate;
        this.hoursWorked = hoursWorked;
        this.hourlyRate = hourlyRate;
        
    }

    public void calculateAll() {
        this.grossHourlyPay = calculateGrossHourlyPay();
        this.vacPay = calculateVacPay();
        this.cpp = calculateCPP();
        this.federalIncomeTax = calculateFederalTaxPerPeriod();
        this.provincialIncomeTax = calculateProvincialTaxBC();
    }

    public double calculateGrossHourlyPay() {
        return hoursWorked * hourlyRate;
    }

    public double calculateVacPay() {
        return grossHourlyPay * VACATION_RATE;
    }

    public double calculateCPP() {
        final double CPP_RATE = 0.0595;
        final double CPP_EXEMPTION_PER_PERIOD = CPP_BASIC_EXEMPTION / PAY_PERIODS_PER_YEAR;

        return -((this.grossHourlyPay - CPP_EXEMPTION_PER_PERIOD) * CPP_RATE);
    }
    // Step 1:
    // EFFECTS: calculates annual taxable income (A)
    // A = [P × (I – F – F2 – F5A – U1 )] – HD – F1
    // P = # of pay periods in a year = 26
    // I = grossHourlyPay + vacPay
    // F = Payroll deductions for the pay period for employee contributions to a
    //     registered pension plan (RPP) for current and past services, a registered
    //     retirement savings plan (RRSP), to a pooled registered pension plan (PRPP),
    //     or a retirement compensation arrangement (RCA). For tax deduction purposes,
    //     employers can deduct amounts contributed to an RPP, RRSP, PRPP, or RCA by or
    //     on behalf of an employee to determine the employee's taxable income = 0
    // F2 = Alimony or maintenance payments required by a legal document dated before
    //      May 1, 1997, to be payroll-deducted authorized by a tax services office or tax centre
    //    = 0
    // F5A = F5* × ((PI – B)/PI)
    // U1 = 0
    // HD = Annual deduction for living in a prescribed zone, as shown on Form TD1
    //    = 0
    // F1 = Annual deductions such as child care expenses and support payments requested by an
    //    = employee or pensioner and authorized by a tax services office or tax centre
    //    = 0

    public double calculateAnnualTaxableIncome() {
        double P = PAY_PERIODS_PER_YEAR;
        double I = this.grossHourlyPay + this.vacPay;
        double F = 0;
        double F2 = 0;
        double F5A = calculateF5A();
        double U1 = 0;
        double HD = 0;
        double F1 = 0;

        return (P * (I - F - F2 - F5A - U1)) - HD - F1; // A
    }

    private double calculateF5A() {
        // Annualized total remuneration
        double I1 = (this.grossHourlyPay + this.vacPay) * PAY_PERIODS_PER_YEAR; // annual pay
        double PI = this.grossHourlyPay + this.vacPay; // pensionable income this pay period
        double B = 0;  // Non-periodic bonuses — assumed 0 unless otherwise known
        double PM = CPP_MONTHS_PER_YEAR; // 12 months/year

        // First term: 0.0100 × (I1 – 3,500), max $678 × (PM/12)
        double firstTermBase = I1 - CPP_BASIC_EXEMPTION;
        double firstTerm = 0.0100 * Math.max(firstTermBase, 0);
        double firstTermCap = 678.00 * (PM / (double) CPP_MONTHS_PER_YEAR);
        firstTerm = Math.min(firstTerm, firstTermCap);

        // Second term: 0.04 × (I1 – 71,300 × (PM/12)), max $396 × (PM/12)
        double secondTermBase = I1 - (CPP_MAX * (PM / (double) CPP_MONTHS_PER_YEAR));
        double secondTerm = 0.04 * Math.max(secondTermBase, 0);
        double secondTermCap = 396.00 * (PM / (double) CPP_MONTHS_PER_YEAR);
        secondTerm = Math.min(secondTerm, secondTermCap);

        // F5* = firstTerm + secondTerm
        double F5star = firstTerm + secondTerm;

        // F5A = F5* × ((PI – B) / PI)
        return (PI == 0) ? 0 : F5star * ((PI - B) / PI);
    }

    // Updated federal tax calculation to match PDOC and return per period tax rounded to cent
    public double calculateFederalTaxPerPeriod() {
        double A = calculateAnnualTaxableIncome();

        double R = getFederalTaxRate(A);

        // Use TD1 federal basic personal amount from your example
        double K1 = roundToTwoDecimals(0.15 * FEDERAL_BASIC_PERSONAL_AMOUNT);

        // Annualized CPP contributions at base rate for the credit, capped at max
        double annualPensionableEarnings = (grossHourlyPay + vacPay) * PAY_PERIODS_PER_YEAR;
        double cppCreditBase = (annualPensionableEarnings - CPP_BASIC_EXEMPTION) * CPP_RATE_BASE;
        cppCreditBase = Math.max(0, cppCreditBase);
        cppCreditBase = Math.min(cppCreditBase, CPP_CREDIT_MAX);
        cppCreditBase = roundToTwoDecimals(cppCreditBase);

        // Annualized EI contributions capped at max
        double annualEIContribution = annualPensionableEarnings * EI_RATE;
        annualEIContribution = Math.min(annualEIContribution, EI_CREDIT_MAX);
        annualEIContribution = roundToTwoDecimals(annualEIContribution);

        double K2 = 0.15 * (cppCreditBase + annualEIContribution);
        K2 = roundToTwoDecimals(K2);

        double K3 = 0.0;

        double K4 = Math.min(0.15 * A, 0.15 * CANADA_EMPLOYMENT_AMOUNT);
        K4 = roundToTwoDecimals(K4);

        double K = 0.0;

        double T3 = (R * A) - K - K1 - K2 - K3 - K4;
        if (T3 < 0) T3 = 0;

        // Federal tax per pay period
        double federalTaxPerPeriod = T3 / PAY_PERIODS_PER_YEAR;

        return roundToTwoDecimals(federalTaxPerPeriod);
    }

    private double getFederalTaxRate(double income) {
        // 2025 Federal Tax Brackets (approx.)
        if (income <= 55867) return 0.15;
        if (income <= 111733) return 0.205;
        if (income <= 173205) return 0.26;
        if (income <= 246752) return 0.29;
        return 0.33;
    }

    public double calculateProvincialTaxBC() {
        final double BC_TAX_RATE = 0.0506;
        final double BC_BASIC_PERSONAL_AMOUNT = 12932.00;
        final double BC_CANADA_EMPLOYMENT_AMOUNT = 1432.00;

        double A = calculateAnnualTaxableIncome();

        // KP = 0 for BC
        double KP = 0.0;

        // K1P = BC lowest tax rate * BC basic personal amount
        double K1P = BC_TAX_RATE * BC_BASIC_PERSONAL_AMOUNT;

        // Calculate annual pensionable earnings (same as federal)
        double annualPensionableEarnings = (grossHourlyPay + vacPay) * PAY_PERIODS_PER_YEAR;

        // Calculate CPP credit base for BC
        double cppCreditBase = (annualPensionableEarnings - CPP_BASIC_EXEMPTION) * CPP_RATE_BASE;
        cppCreditBase = Math.max(0, cppCreditBase);
        cppCreditBase = Math.min(cppCreditBase, CPP_CREDIT_MAX);

        // Calculate EI credit for BC
        double annualEIContribution = annualPensionableEarnings * EI_RATE;
        annualEIContribution = Math.min(annualEIContribution, EI_CREDIT_MAX);

        // K2P = BC lowest tax rate * (CPP credit + EI credit)
        double K2P = BC_TAX_RATE * (cppCreditBase + annualEIContribution);

        // K3P = 0 for your case
        double K3P = 0.0;

        // K4P = BC lowest tax rate * Canada Employment Amount
        double K4P = BC_TAX_RATE * BC_CANADA_EMPLOYMENT_AMOUNT;

        // Calculate T4
        double T4 = (BC_TAX_RATE * A) - KP - K1P - K2P - K3P - K4P;
        if (T4 < 0) T4 = 0;

        // Calculate surtax S based on A
        double S;
        if (A <= 25020) {
            S = Math.min(T4, 562);
        } else if (A <= 40807) {
            S = Math.min(T4, 562 - ((A - 25020) * 0.0356));
        } else {
            S = 0;
        }

        // V1, V2, LCP assumed zero for BC
        double V1 = 0.0;
        double V2 = 0.0;
        double LCP = 0.0;

        // Calculate T2
        double T2 = T4 + V1 + V2 - S - (PAY_PERIODS_PER_YEAR * LCP);
        if (T2 < 0) T2 = 0;

        // Provincial tax per pay period, rounded to two decimals
        return roundToTwoDecimals(T2 / PAY_PERIODS_PER_YEAR);
    }

    // Helper to round to 2 decimal places (cents)
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


}
