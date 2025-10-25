import main.model.Employee;
import main.model.PayStub;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

            System.out.print("Hours worked this period (e.g., 40 or 44.5): ");
            double hours = readDouble(in);

            System.out.print("Hourly rate (e.g., 17.85): ");
            double hourlyRate = readDouble(in);

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
            PayStub stub = new PayStub(emp, hours, hourlyRate, vacationRate, start, end);
            stub.setYtdBefore(ytdGross, ytdCpp, ytdEi, ytdTax);
            stub.calculateAll();

            // Derive hourly + vacation for display so that hourly + vac == gross
            BigDecimal hoursBD = BigDecimal.valueOf(hours);
            BigDecimal rateBD = BigDecimal.valueOf(hourlyRate);
            BigDecimal hourlyPayRounded = hoursBD.multiply(rateBD).setScale(2, RM);
            BigDecimal gross = BigDecimal.valueOf(stub.getGrossPay()).setScale(2, RM);
            BigDecimal vacationPayRounded = gross.subtract(hourlyPayRounded).setScale(2, RM);

            // Output
            System.out.println("\n=== Results ===");
            System.out.printf(Locale.CANADA, "Employee: %s  (EI-exempt: %s)%n", name, eiExempt ? "Yes" : "No");
            System.out.printf(Locale.CANADA, "Period:   %s to %s%n", start, end);
            System.out.printf(Locale.CANADA, "Hours:    %.2f @ $%.2f/hr, Vacation rate: %.2f%%%n",
                    hours, hourlyRate, vacationRate * 100.0);

            System.out.println("\n-- Earnings --");
            printMoney("Hourly pay", hourlyPayRounded);
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

    private static void printMoney(String label, double amount) {
        printMoney(label, BigDecimal.valueOf(amount));
    }

    private static void printMoney(String label, BigDecimal amount) {
        System.out.printf(Locale.CANADA, "%-22s : $%,.2f%n", label, amount);
    }
}
