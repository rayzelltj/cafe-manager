import main.model.Employee;
import main.model.EarningSlice;
import main.model.EarningType;
import main.model.PayStub;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class App {
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    public static void main(String[] args) {

        try (Scanner in = new Scanner(System.in)) {
            System.out.println("=== BC Cafe Payroll Calculator (Biweekly) ===");

            // Employee basics
            System.out.print("Employee name: ");
            String name = in.nextLine().trim();

            System.out.print("Is the employee EI-exempt? (y/N): ");
            boolean eiExempt = readYesNo(in);

            // Period and pay inputs
            System.out.print("Pay period start (YYYY-MM-DD): ");
            LocalDate start = readDate(in);

            System.out.print("Pay period end (YYYY-MM-DD): ");
            LocalDate end = readDate(in);

            // Earnings lines (slices)
            System.out.println("\n-- Earnings lines (at least one) --");
            List<EarningSlice> slices = new ArrayList<>();

            // First line (default REGULAR)
            slices.add(readSlice(in, /*isFirst=*/true));

            // Optional additional lines
            while (true) {
                System.out.print("Add another earnings line? (y/N): ");
                if (!readYesNo(in)) break;
                slices.add(readSlice(in, /*isFirst=*/false));
            }

            // Vacation %
            System.out.print("Vacation rate (decimal, default 0.04 for 4%): ");
            String vacRaw = in.nextLine().trim();
            double vacationRate = vacRaw.isEmpty() ? 0.04 : parseDouble(vacRaw);

            // YTD (before this pay) — useful for CPP/EI caps
            System.out.println("\n-- Year-To-Date (before this pay) --");
            System.out.print("YTD Gross (default 0): ");
            double ytdGross = readOptionalDouble(in);

            System.out.print("YTD CPP employee (default 0): ");
            double ytdCpp = readOptionalDouble(in);

            System.out.print("YTD EI employee (default 0): ");
            double ytdEi = readOptionalDouble(in);

            System.out.print("YTD Income Tax (federal + provincial) (default 0): ");
            double ytdTax = readOptionalDouble(in);

            // Build and compute
            Employee emp = new Employee(name, eiExempt);
            PayStub stub = new PayStub(emp, List.copyOf(slices), vacationRate, start, end);
            stub.setYtdBefore(ytdGross, ytdCpp, ytdEi, ytdTax);
            stub.calculateAll();

            // Derive totals for display
            BigDecimal subtotalHourly = slices.stream()
                    .map(s -> s.getSubtotalHourlyPay().setScale(2, RM))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal gross = BigDecimal.valueOf(stub.getGrossPay()).setScale(2, RM);
            BigDecimal vacationPayRounded = gross.subtract(subtotalHourly).setScale(2, RM);

            // Output
            System.out.println("\n=== Results ===");
            System.out.printf(Locale.CANADA, "Employee: %s  (EI-exempt: %s)%n", name, eiExempt ? "Yes" : "No");
            System.out.printf(Locale.CANADA, "Period:   %s to %s%n", start, end);

            System.out.println("\n-- Earnings lines --");
            System.out.printf(Locale.CANADA, "%-14s %-10s %-12s %s%n", "Type", "Hours", "Rate", "Amount");
            for (EarningSlice s : slices) {
                BigDecimal hrs = s.hours.setScale(2, RM);
                BigDecimal rate = s.rate.setScale(2, RM);
                BigDecimal amt = s.getSubtotalHourlyPay().setScale(2, RM);
                System.out.printf(Locale.CANADA, "%-14s %10.2f %12.2f %10.2f%n",
                        s.type, hrs.doubleValue(), rate.doubleValue(), amt.doubleValue());
            }

            System.out.println("\n-- Earnings totals --");
            printMoney("Hourly pay (subtotal)", subtotalHourly);
            printMoney("Vacation pay", vacationPayRounded);
            printMoney("Gross pay", gross);

            System.out.println("\n-- Deductions --");
            printMoney("CPP (employee)", stub.getCPPDeduction());
            printMoney("EI (employee)", stub.getEIDeduction());
            printMoney("Federal income tax", stub.getFederalTaxDeduction());
            printMoney("Provincial income tax", stub.getProvincialTaxDeduction());
            printMoney("Total income tax", stub.getTotalIncomeTaxDeduction());

            System.out.println("\n-- Net --");
            printMoney("Net pay", stub.getNetPay());

            System.out.println("\nDone ✅");
        } catch (Exception ex) {
            System.err.println("\nError: " + ex.getMessage());
            System.err.println("Tip: check your date format (YYYY-MM-DD) and numeric inputs.");
        }
    }

    // ---------- slice input ----------

    private static EarningSlice readSlice(Scanner in, boolean isFirst) {
        EarningType type;
        if (isFirst) {
            System.out.print("Earning type [REGULAR/OVERTIME/STAT_HOLIDAY/VACATION_PAYOUT] (default REGULAR): ");
            String t = in.nextLine().trim();
            type = t.isEmpty() ? EarningType.REGULAR : parseType(t);
        } else {
            System.out.print("Earning type [REGULAR/OVERTIME/STAT_HOLIDAY/VACATION_PAYOUT]: ");
            type = parseType(in.nextLine().trim());
        }

        System.out.print("Hours (e.g., 40 or 44.5): ");
        BigDecimal hours = bd(readDouble(in));

        System.out.print("Rate (e.g., 17.85): ");
        BigDecimal rate = bd(readDouble(in));

        return new EarningSlice(type, hours, rate);
    }

    private static EarningType parseType(String s) {
        String up = s.toUpperCase(Locale.ROOT).trim();
        try {
            return EarningType.valueOf(up);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid earning type: " + s +
                    " (expected REGULAR, OVERTIME, STAT_HOLIDAY, VACATION_PAYOUT)");
        }
    }

    // ---------- helpers ----------

    private static boolean readYesNo(Scanner in) {
        String s = in.nextLine().trim().toLowerCase(Locale.ROOT);
        return s.equals("y") || s.equals("yes") || s.equals("true") || s.equals("1");
    }

    private static LocalDate readDate(Scanner in) {
        String s = in.nextLine().trim();
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: " + s + " (expected YYYY-MM-DD)");
        }
    }

    private static double readDouble(Scanner in) {
        String s = in.nextLine().trim();
        return parseDouble(s);
    }

    private static double readOptionalDouble(Scanner in) {
        String s = in.nextLine().trim();
        return s.isEmpty() ? 0.0 : parseDouble(s);
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + s);
        }
    }

    private static BigDecimal bd(double v) { return BigDecimal.valueOf(v); }

    private static void printMoney(String label, double amount) {
        printMoney(label, BigDecimal.valueOf(amount));
    }

    private static void printMoney(String label, BigDecimal amount) {
        System.out.printf(Locale.CANADA, "%-22s : $%,.2f%n", label, amount);
    }
}
