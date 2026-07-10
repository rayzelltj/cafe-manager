# cafe-manager — Canadian Payroll Engine

A payroll calculation engine in Java that computes CRA-compliant net pay for a small business, built to replace a paid third-party payroll service for a 7-employee Vancouver café.

Given an employee's hours, pay rate, and pay period, it computes gross pay, statutory deductions (CPP, EI, federal and BC provincial income tax), and net pay — following the CRA's official withholding formulas.

## Why

Commercial payroll software costs roughly $200/month for a business this size. This engine implements the same statutory calculations directly, with every figure verifiable against the CRA's reference calculator.

## Correctness

This is the part that matters for payroll: the numbers have to be exactly right.

- Implements the **CRA T4008 "Option 1"** payroll deductions formula (2025 tax year) — federal and BC provincial.
- All monetary math uses `BigDecimal` with explicit `HALF_UP` rounding — **no floating-point money bugs**.
- Validated against the [CRA Payroll Deductions Online Calculator (PDOC)](https://www.canada.ca/en/revenue-agency/services/e-services/digital-services-businesses/payroll-deductions-online-calculator.html): **8 real pay periods checked to the cent across 7 outputs** (gross, CPP, EI, federal tax, BC tax, total tax, net) — see `src/test/java/model/PayStubTest.java`.

## Features

- **Federal income tax** — 5-bracket progressive calculation with K1 (basic personal amount), K2 (CPP/EI), and K4 (Canada Employment Amount) credits.
- **BC provincial income tax** — 7-bracket progressive calculation with BPA and CPP-base credits.
- **CPP** — base + additional (CPP2) rate split, annual basic exemption, and contribution maximum.
- **EI** — insurable-earnings rate, annual maximum, and EI-exempt employee handling.
- **Vacation pay** accrual (4%).
- **Effective-dated tax configuration** — rates/thresholds are config-driven (`TaxConfig`), so mid-year changes (e.g. the July 1 2025 federal rate change) are supported without touching calculation logic.
- **SQLite persistence** for employees and generated pay stubs.

## Tech stack

Java · Gradle (Kotlin DSL) · JUnit 5 · SQLite (JDBC) · PDFBox · OpenCSV

## Getting started

```bash
# Build
./gradlew build

# Run the test suite (the CRA-validated pay-stub checks)
./gradlew test

# Run the app
./gradlew run
```

## Project structure

```
src/main/java/org/payroll/
  model/        # Employee, PayStub, EarningSlice, TaxCalculator, TaxConstants
  tax/          # TaxConfig, TaxConfigProvider (effective-dated rate config)
  persistence/  # SQLite Db + schema bootstrap
  Main.java
src/main/resources/db/schema_payroll.sql
src/test/java/model/PayStubTest.java   # CRA PDOC-validated cases
```

## Roadmap

Core payroll engine is complete and tested. Planned next:

- Desktop UI for entering hours and generating pay stubs.
- PDF pay-stub export.
- Automated bi-weekly pay-run scheduling.

## Disclaimer

This is a personal project for a specific small business, not certified tax software. Always verify against the CRA PDOC before relying on any figure for official remittance.

## License

MIT
