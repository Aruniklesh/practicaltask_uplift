package tests;

import base.BaseTest;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.CalculationUtils;

import java.math.BigDecimal;

/**
 * ApplicationCoreLogicTest calls the application's own deployed JavaScript function
 * (window.annualFor, defined as a top-level function in a classic, non-module <script>
 * tag in index.html, and therefore globally callable) directly via JavascriptExecutor.
 *
 * Why this exists:
 * The Calculator's <select id="frequency"> only ever submits one of four fixed, lowercase
 * option values ("weekly", "fortnightly", "monthly", "annual") regardless of which option's
 * visible text Selenium matches. That makes it architecturally impossible to prove Rule R2
 * (case-insensitive frequency handling) through the Calculator UI — a test that types
 * "MONTHLY" into the dropdown selection logic still only ever causes the browser to submit
 * the literal string "monthly" to the app.
 *
 * This test instead invokes the real annualFor(amount, frequency) function used by BOTH the
 * calculator and the Members table directly in the loaded page's JS context, with frequency
 * strings in every case variant for all four known frequencies. This is still "real app code",
 * not a reimplementation — it just bypasses the one UI control that makes case variation
 * impossible to submit. It generalizes the single case-sensitivity defect found via member
 * M004 ("Monthly") to prove it is a systemic defect affecting ALL frequencies in ALL
 * non-lowercase-exact forms, not a one-off.
 */
public class ApplicationCoreLogicTest extends BaseTest {

    @DataProvider(name = "frequencyCaseVariants")
    public Object[][] frequencyCaseVariants() {
        return new Object[][]{
                // {amount, frequencySubmitted, canonicalFrequencyForExpectedCalc, description}
                {"100", "weekly", "weekly", "Exact lowercase weekly"},
                {"100", "Weekly", "weekly", "Title-case Weekly"},
                {"100", "WEEKLY", "weekly", "Uppercase WEEKLY"},
                {"100", "fortnightly", "fortnightly", "Exact lowercase fortnightly"},
                {"100", "Fortnightly", "fortnightly", "Title-case Fortnightly"},
                {"100", "FORTNIGHTLY", "fortnightly", "Uppercase FORTNIGHTLY"},
                {"100", "monthly", "monthly", "Exact lowercase monthly"},
                {"100", "Monthly", "monthly", "Title-case Monthly"},
                {"100", "MONTHLY", "monthly", "Uppercase MONTHLY"},
                {"100", "annual", "annual", "Exact lowercase annual"},
                {"100", "Annual", "annual", "Title-case Annual"},
                {"100", "ANNUAL", "annual", "Uppercase ANNUAL"},
        };
    }

    /**
     * Rule R1 & R2: calls the real annualFor() function from index.html with every
     * case variant of every known frequency and compares against the independently
     * calculated, spec-correct multiplier result.
     *
     * Expected outcome based on static analysis of the app's MULTIPLIERS lookup
     * (a plain JS object keyed only by exact lowercase strings, with "|| 52" fallback):
     *   - "weekly" variants: ALL pass, but for weekly's non-lowercase forms this is a false
     *     negative risk — the object lookup still misses and falls through to the "|| 52"
     *     default, which happens to equal weekly's own correct multiplier (52). The result
     *     looks correct by coincidence, not because case-insensitivity is actually implemented.
     *   - "fortnightly" variants: the exact-lowercase form reproduces BUG-001 (uses 24 instead
     *     of 26); the non-lowercase forms miss the lookup entirely and fall back to 52, an even
     *     larger deviation from the correct value of 26.
     *   - "monthly" / "annual" variants: the exact-lowercase forms are correct (12 / 1); any
     *     non-lowercase form misses the lookup and falls back to 52, reproducing and
     *     generalizing BUG-002 (previously only observed for member M004's "Monthly").
     */
    @Test(
            dataProvider = "frequencyCaseVariants",
            description = "annualFor() case-insensitivity check (R1/R2)"
    )
    public void testAnnualForDirectInvocation(String amount, String frequencySubmitted, String canonicalFrequency, String description) {
        getDriver().get(baseUrl);

        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        Object rawResult = js.executeScript(
                "return annualFor(arguments[0], arguments[1]);",
                Double.parseDouble(amount), frequencySubmitted
        );
        double actualAnnual = ((Number) rawResult).doubleValue();

        BigDecimal expectedAnnual = CalculationUtils.calculateAnnualContribution(amount, canonicalFrequency);

        System.out.println("================================================================================");
        System.out.printf("DIRECT JS CALL: annualFor(%s, '%s') | %s%n", amount, frequencySubmitted, description);
        System.out.printf("  EXPECTED (per R1/R2, spec-correct): %s%n", expectedAnnual.toPlainString());
        System.out.printf("  ACTUAL (real app annualFor() result): %s%n", actualAnnual);

        Assert.assertEquals(
                BigDecimal.valueOf(actualAnnual).setScale(2, CalculationUtils.DEFAULT_FINANCIAL_ROUNDING),
                expectedAnnual,
                String.format(
                        "annualFor(%s, '%s') [%s] -> expected %s per R1/R2 but app's real annualFor() returned %s. "
                                + "Rule Reference: R1/R2.",
                        amount, frequencySubmitted, description, expectedAnnual, actualAnnual
                )
        );
    }

    /**
     * Confirms the app's documented fallback behaviour for a completely unsupported
     * frequency string (not a case-variant of a known one). No business rule defines
     * the expected behaviour for this input, so this test does not assert a "correct"
     * value — it only documents, with evidence, the real app's observed fallback to the
     * weekly multiplier (52), flagged as a potential risk (silent fallback masking bad
     * data) rather than a confirmed defect.
     */
    @Test(description = "Unsupported frequency fallback behavior (documented, not a defect assertion)")
    public void testUnsupportedFrequencyFallbackBehaviorIsDocumented() {
        getDriver().get(baseUrl);
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        Object rawResult = js.executeScript("return annualFor(arguments[0], arguments[1]);", 100.0, "biweekly");
        double actual = ((Number) rawResult).doubleValue();

        System.out.println("================================================================================");
        System.out.println("UNSUPPORTED FREQUENCY RISK DOCUMENTATION: annualFor(100, 'biweekly')");
        System.out.println("  Observed actual: " + actual + " (app silently falls back to the weekly multiplier, 52)");
        System.out.println("  No business rule (R1-R8) defines expected behavior for this input.");
        System.out.println("  Documented as a potential risk in BUG_REPORTS.md, not asserted as a defect.");

        // Only asserts the fallback is deterministic/stable, not that it is "correct" (no rule defines "correct" here).
        Assert.assertEquals(actual, 5200.0, 0.0001,
                "Unsupported frequency fallback behavior changed from the previously observed value (100 * 52 = 5200). "
                        + "If this assertion fails, the app's fallback behavior has changed and BUG_REPORTS.md's risk note needs review.");
    }
}
