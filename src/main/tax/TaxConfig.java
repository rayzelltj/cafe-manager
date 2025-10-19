package main.tax;

import java.math.BigDecimal;

public final class TaxConfig {

    public final BigDecimal lowFedRate;        // drives first bracket & credits (K1/K2/K4)
    public final BigDecimal fedT1, fedT2, fedT3, fedT4;
    public final BigDecimal fedRate2, fedRate3, fedRate4, fedRate5;
    public final BigDecimal fedBPA;
    public final BigDecimal canadaEmploymentAmount;  // 2025: 1471

    // BC
    public final BigDecimal bcBPA;
    public final BigDecimal bcT1, bcT2, bcT3, bcT4, bcT5, bcT6;
    public final BigDecimal bcRate1, bcRate2, bcRate3, bcRate4, bcRate5, bcRate6, bcRate7;

    // CPP & EI (for caps, etc.)
    public final BigDecimal cppBaseRate, cppTotalRate, cppMaxContribution, eiRate, eiMaxContribution;

    public TaxConfig(
            BigDecimal lowFedRate,
            BigDecimal fedT1, BigDecimal fedT2, BigDecimal fedT3, BigDecimal fedT4,
            BigDecimal fedRate2, BigDecimal fedRate3, BigDecimal fedRate4, BigDecimal fedRate5,
            BigDecimal fedBPA, BigDecimal canadaEmploymentAmount,
            BigDecimal bcBPA, BigDecimal bcT1, BigDecimal bcT2, BigDecimal bcT3,
            BigDecimal bcT4, BigDecimal bcT5, BigDecimal bcT6,
            BigDecimal bcRate1, BigDecimal bcRate2, BigDecimal bcRate3,
            BigDecimal bcRate4, BigDecimal bcRate5, BigDecimal bcRate6, BigDecimal bcRate7,
            BigDecimal cppBaseRate, BigDecimal cppTotalRate, BigDecimal cppMaxContribution,
            BigDecimal eiRate, BigDecimal eiMaxContribution
    ) {
        this.lowFedRate = lowFedRate;
        this.fedT1 = fedT1; this.fedT2 = fedT2; this.fedT3 = fedT3; this.fedT4 = fedT4;
        this.fedRate2 = fedRate2; this.fedRate3 = fedRate3; this.fedRate4 = fedRate4; this.fedRate5 = fedRate5;
        this.fedBPA = fedBPA;
        this.canadaEmploymentAmount = canadaEmploymentAmount;

        this.bcBPA = bcBPA;
        this.bcT1 = bcT1; this.bcT2 = bcT2; this.bcT3 = bcT3;
        this.bcT4 = bcT4; this.bcT5 = bcT5; this.bcT6 = bcT6;
        this.bcRate1 = bcRate1; this.bcRate2 = bcRate2; this.bcRate3 = bcRate3;
        this.bcRate4 = bcRate4; this.bcRate5 = bcRate5; this.bcRate6 = bcRate6; this.bcRate7 = bcRate7;

        this.cppBaseRate = cppBaseRate; this.cppTotalRate = cppTotalRate; this.cppMaxContribution = cppMaxContribution;
        this.eiRate = eiRate; this.eiMaxContribution = eiMaxContribution;
    }
}
