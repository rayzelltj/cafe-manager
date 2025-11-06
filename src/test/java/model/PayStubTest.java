package model;
import org.payroll.model.Employee;
import org.payroll.model.EarningSlice;
import org.payroll.model.EarningType;
import org.payroll.model.PayStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PayStubTest {

    private PayStub payStub156;
    private PayStub payStub209;
    private PayStub payStub104;
    private PayStub payStub112;
    private PayStub payStub122;
    private PayStub payStub1242;
    private PayStub payStub163;
    private PayStub payStub216;

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    private static List<EarningSlice> slices(String hours, String rate) {
        return List.of(new EarningSlice(EarningType.REGULAR, bd(hours), bd(rate)));
    }

    @BeforeEach
    public void runBefore() {
        Employee employee1 = new Employee("Rayzell Tjandra", true);   // EI exempt
        Employee employee2 = new Employee("Fahad", false);
        Employee employee3 = new Employee("Sari", false);

        payStub216 = new PayStub(
                employee3,
                slices("80", "17.85"),
                0.04,
                LocalDate.of(2025, 9, 28),
                LocalDate.of(2025, 10, 11)
        );

        payStub1242 = new PayStub(
                employee1,
                slices("24", "17.40"),
                0.04,
                LocalDate.of(2024, 12, 22),
                LocalDate.of(2025, 1, 4)
        );

        payStub104 = new PayStub(
                employee1,
                slices("40", "17.40"),
                0.04,
                LocalDate.of(2025, 3, 16),
                LocalDate.of(2025, 3, 29)
        );

        payStub112 = new PayStub(
                employee1,
                slices("40", "17.40"),
                0.04,
                LocalDate.of(2025, 3, 30),
                LocalDate.of(2025, 4, 12)
        );

        payStub122 = new PayStub(
                employee1,
                slices("40", "17.40"),
                0.04,
                LocalDate.of(2025, 4, 13),
                LocalDate.of(2025, 4, 26)
        );

        payStub156 = new PayStub(
                employee1,
                slices("70", "17.85"),
                0.04,
                LocalDate.of(2025, 7, 6),
                LocalDate.of(2025, 7, 19)
        );

        payStub209 = new PayStub(
                employee1,
                slices("40", "17.85"),
                0.04,
                LocalDate.of(2025, 9, 14),
                LocalDate.of(2025, 9, 19)
        );

        payStub163 = new PayStub(
                employee2,
                slices("44.5", "17.85"),
                0.04,
                LocalDate.of(2025, 7, 6),
                LocalDate.of(2025, 7, 19)
        );

        // run calculations
        payStub1242.calculateAll();
        payStub104.calculateAll();
        payStub112.calculateAll();
        payStub122.calculateAll();
        payStub156.calculateAll();
        payStub209.calculateAll();
        payStub163.calculateAll();
        payStub216.calculateAll();
    }

    @Test
    public void testCalculateGrossPay() {
        assertEquals(434.30, payStub1242.getGrossPay());
        assertEquals(723.84, payStub104.getGrossPay());
        assertEquals(723.84, payStub112.getGrossPay());
        assertEquals(723.84, payStub122.getGrossPay());
        assertEquals(1299.48, payStub156.getGrossPay());
        assertEquals(742.56, payStub209.getGrossPay());
        assertEquals(826.10, payStub163.getGrossPay());
        assertEquals(1485.12, payStub216.getGrossPay());
    }

    @Test
    public void testCalculateCPP() {
        assertEquals(17.83, payStub1242.getCPPDeduction());
        assertEquals(35.06, payStub104.getCPPDeduction());
        assertEquals(35.06, payStub112.getCPPDeduction());
        assertEquals(35.06, payStub122.getCPPDeduction());
        assertEquals(69.31, payStub156.getCPPDeduction());
        assertEquals(36.17, payStub209.getCPPDeduction());
        assertEquals(41.14, payStub163.getCPPDeduction());
        assertEquals(80.36, payStub216.getCPPDeduction());
    }

    @Test
    public void testCalculateEI() {
        assertEquals(0.00, payStub156.getEIDeduction());
        assertEquals(13.55, payStub163.getEIDeduction());
        assertEquals(24.36, payStub216.getEIDeduction());
    }

    @Test
    public void testCalculateFederalTaxDeduction() {
        assertEquals(0.00, payStub1242.getFederalTaxDeduction());
        assertEquals(1.78, payStub104.getFederalTaxDeduction());
        assertEquals(1.78, payStub112.getFederalTaxDeduction());
        assertEquals(1.78, payStub122.getFederalTaxDeduction());
        assertEquals(77.45, payStub156.getFederalTaxDeduction());
        assertEquals(4.13, payStub209.getFederalTaxDeduction());
        assertEquals(13.23, payStub163.getFederalTaxDeduction());
        assertEquals(98.49, payStub216.getFederalTaxDeduction());
    }

    @Test
    public void testCalculateProvincialTaxDeduction() {
        assertEquals(0.00, payStub1242.getProvincialTaxDeduction());
        assertEquals(0.00, payStub104.getProvincialTaxDeduction());
        assertEquals(0.00, payStub112.getProvincialTaxDeduction());
        assertEquals(0.00, payStub122.getProvincialTaxDeduction());
        assertEquals(27.05, payStub156.getProvincialTaxDeduction());
        assertEquals(0.00, payStub209.getProvincialTaxDeduction());
        assertEquals(0.00, payStub163.getProvincialTaxDeduction());
        assertEquals(41.20, payStub216.getProvincialTaxDeduction());
    }

    @Test
    public void testCalculateTotalIncomeTaxDeduction() {
        assertEquals(0.00, payStub1242.getTotalIncomeTaxDeduction());
        assertEquals(1.78, payStub104.getTotalIncomeTaxDeduction());
        assertEquals(1.78, payStub112.getTotalIncomeTaxDeduction());
        assertEquals(1.78, payStub122.getTotalIncomeTaxDeduction());
        assertEquals(104.50, payStub156.getTotalIncomeTaxDeduction());
        assertEquals(4.13, payStub209.getTotalIncomeTaxDeduction());
        assertEquals(13.23, payStub163.getTotalIncomeTaxDeduction());
        assertEquals(139.69, payStub216.getTotalIncomeTaxDeduction());
    }

    @Test
    public void testCalculateNetPay() {
        assertEquals(416.47, payStub1242.getNetPay());
        assertEquals(687.00, payStub104.getNetPay());
        assertEquals(687.00, payStub112.getNetPay());
        assertEquals(687.00, payStub122.getNetPay());
        assertEquals(1125.67, payStub156.getNetPay());
        assertEquals(702.26, payStub209.getNetPay());
        assertEquals(758.18, payStub163.getNetPay());
        assertEquals(1240.71, payStub216.getNetPay());
    }
}