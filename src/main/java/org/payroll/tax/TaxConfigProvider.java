package org.payroll.tax;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;
import static org.payroll.model.TaxConstants.*;

public final class TaxConfigProvider {
    private static final NavigableMap<LocalDate, TaxConfig> CONFIGS = new TreeMap<>();

    static {
        CONFIGS.put(LocalDate.of(2025, 1, 1), new TaxConfig(
                BigDecimal.valueOf(0.15), FEDERAL_T1, FEDERAL_T2, FEDERAL_T3, FEDERAL_T4,
                FEDERAL_RATE_2, FEDERAL_RATE_3, FEDERAL_RATE_4, FEDERAL_RATE_5,
                FEDERAL_BPA, BigDecimal.valueOf(1471),
                BC_BPA, BC_T1, BC_T2, BC_T3, BC_T4, BC_T5, BC_T6,
                BC_RATE_1, BC_RATE_2, BC_RATE_3, BC_RATE_4, BC_RATE_5, BC_RATE_6, BC_RATE_7,
                CPP_BASE_RATE, CPP_RATE, CPP_MAX_CONTRIBUTION,
                EI_RATE, EI_MAX_CONTRIBUTION
        ));
        CONFIGS.put(LocalDate.of(2025, 7, 1), new TaxConfig(
                BigDecimal.valueOf(0.14), /* same rest as above */
                FEDERAL_T1, FEDERAL_T2, FEDERAL_T3, FEDERAL_T4,
                FEDERAL_RATE_2, FEDERAL_RATE_3, FEDERAL_RATE_4, FEDERAL_RATE_5,
                FEDERAL_BPA, BigDecimal.valueOf(1471),
                BC_BPA, BC_T1, BC_T2, BC_T3, BC_T4, BC_T5, BC_T6,
                BC_RATE_1, BC_RATE_2, BC_RATE_3, BC_RATE_4, BC_RATE_5, BC_RATE_6, BC_RATE_7,
                CPP_BASE_RATE, CPP_RATE, CPP_MAX_CONTRIBUTION,
                EI_RATE, EI_MAX_CONTRIBUTION
        ));
        // Add future entries here (e.g., 2026-01-01)
    }

    public static TaxConfig forPayDate(LocalDate payDate) {
        var entry = CONFIGS.floorEntry(payDate);
        if (entry == null) throw new IllegalStateException("No tax config for " + payDate);
        return entry.getValue();
    }
}