package tests;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import utils.CalculationUtils;
import utils.TestListener;

import java.math.BigDecimal;

/**
 * CalculationUtilsBoundaryTest verifies Rules R6 and R7 at rounding boundary values that
 * are NOT present in the client-provided data/members.json fixture (0.0000 / 0.0001 /
 * 0.0004 / 0.0005 / 0.9999 / 1.0000, plus exact half-up tie values).
 *
 * IMPORTANT SCOPE NOTE (read before trusting this as "full R6/R7 coverage"):
 * This test exercises only CalculationUtils — the independent Java reference engine that
 * MembersValidationTest uses to compute expected values. It does NOT exercise the real
 * application's order-rounding/status logic, because:
 *   1. That logic lives inline inside the fetch().then() callback in index.html and is not
 *      exposed as a global, directly-callable function (unlike annualFor).
 *   2. The real app only ever renders values present in data/members.json, which is
 *      client-owned fixture data this project must not modify, and it does not contain
 *      boundary values at these exact thresholds.
 * This test therefore proves CalculationUtils' own internal correctness and rounding-mode
 * consistency (defending every UI-level assertion that depends on it), but R6/R7 boundary
 * behavior of the LIVE APPLICATION at these specific thresholds remains genuinely untested
 * and is documented as a coverage gap in EDGE_CASE_COVERAGE.md.
 *
 * Does not extend BaseTest (no browser/driver needed for a pure calculation check), so
 * TestListener is attached directly here — otherwise this class's results would run and
 * pass correctly but never appear in the ExtentReports HTML (only in the raw Surefire XML).
 */
@Listeners(TestListener.class)
public class CalculationUtilsBoundaryTest {

    @DataProvider(name = "orderUnitsBoundaries")
    public Object[][] orderUnitsBoundaries() {
        return new Object[][]{
                // {rawNextOrderUnits, expectedRoundedUnits, expectedStatus, note}
                {0.00001, "0.0000", "Blocked", "Rounds down to exactly zero"},
                {0.00004, "0.0000", "Blocked", "Same raw value as member M007 (regression guard)"},
                {0.00006, "0.0001", "Ready", "Same raw value as member M008 (regression guard)"},
                {0.000449, "0.0004", "Ready", "Just below the 0.0004/0.0005 boundary"},
                {0.00045, "0.0005", "Ready", "Exact HALF_UP tie (ambiguous rounding boundary)"},
                {0.000451, "0.0005", "Ready", "Just above the 0.0004/0.0005 boundary"},
                {0.99989, "0.9999", "Ready", "Just below 1.0000"},
                {0.99996, "1.0000", "Ready", "Rounds up to exactly 1.0000, still non-zero -> Ready"},
                {0.0, "0.0000", "Blocked", "Exact zero (regression guard, matches member M006)"},
        };
    }

    @Test(
            dataProvider = "orderUnitsBoundaries",
            description = "Order Units rounding & status at R6/R7 boundaries"
    )
    public void testOrderUnitsRoundingAndStatusBoundary(double raw, String expectedRoundedUnits, String expectedStatus, String note) {
        String actualRoundedUnits = CalculationUtils.formatOrderUnits(raw);
        String actualStatus = CalculationUtils.determineOrderStatus(raw);

        System.out.println("================================================================================");
        System.out.printf("BOUNDARY CHECK (CalculationUtils only): raw=%s | %s%n", raw, note);
        System.out.printf("  Expected rounded units: %s | Actual: %s%n", expectedRoundedUnits, actualRoundedUnits);
        System.out.printf("  Expected status: %s | Actual: %s%n", expectedStatus, actualStatus);

        Assert.assertEquals(actualRoundedUnits, expectedRoundedUnits,
                String.format("R6 rounding mismatch for raw=%s (%s)", raw, note));
        Assert.assertEquals(actualStatus, expectedStatus,
                String.format("R7 status mismatch for raw=%s (%s). Rule Reference: R7.", raw, note));
    }

    @Test(description = "Fund Value rounding at a HALF_UP tie (R5)")
    public void testFundValueRoundingAtHalfUpBoundary() {
        BigDecimal fundValue = CalculationUtils.calculateFundValue(1.0, 2.005);
        Assert.assertEquals(fundValue, new BigDecimal("2.01"),
                "Fund Value at an exact HALF_UP tie (1.0 * 2.005) did not round as documented in CalculationUtils' rounding-mode baseline.");
    }
}
