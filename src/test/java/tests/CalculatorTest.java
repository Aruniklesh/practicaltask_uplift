package tests;

import base.BaseTest;
import models.CalculatorTestCase;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.CalculatorPage;
import utils.CalculationUtils;
import utils.JsonUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * CalculatorTest implements Part 1 of the QA Automation Practical Assessment:
 * A data-driven test for the Annual Contribution Calculator driven by calculator-testdata.json.
 *
 * Validates Business Rules:
 * - Rule R1: Annual contribution = contribution amount × multiplier (Weekly=52, Fortnightly=26, Monthly=12, Annual=1)
 * - Rule R2: Payment frequency case-insensitivity ("Monthly" = "monthly" = "MONTHLY")
 * - Rule R3: Currency formatting with € and 2 decimal places (€#,##0.00)
 * - Rule R8: Input validation (Blank/non-numeric: 'Enter a valid amount', Negative: 'Amount cannot be negative', Zero allowed)
 *
 * Financial Precision:
 * - Uses java.math.BigDecimal for high-precision independent expected value calculations.
 * - Flags rounding mode ambiguity (defaulting to RoundingMode.HALF_UP).
 */
public class CalculatorTest extends BaseTest {

    private static final String TEST_DATA_PATH = "src/test/resources/testdata/calculator-testdata.json";

    /**
     * TestNG DataProvider that reads test cases from the JSON file using Jackson.
     */
    @DataProvider(name = "calculatorDataProvider")
    public Object[][] getCalculatorTestData() {
        List<CalculatorTestCase> testCases = JsonUtils.readCalculatorTestCases(TEST_DATA_PATH);
        Object[][] data = new Object[testCases.size()][1];
        for (int i = 0; i < testCases.size(); i++) {
            data[i][0] = testCases.get(i);
        }
        return data;
    }

    /**
     * Main Data-Driven Test Method.
     * Executes for each scenario in calculator-testdata.json.
     */
    @Test(
            dataProvider = "calculatorDataProvider",
            description = "Data-driven test verifying Annual Contribution Calculator against Rules R1, R2, R3, and R8"
    )
    public void testAnnualContributionCalculator(CalculatorTestCase testCase) {
        System.out.println("================================================================================");
        System.out.printf("RUNNING TEST: [%s] - %s%n", testCase.getTestCaseId(), testCase.getDescription());
        System.out.printf("  INPUTS -> Amount: '%s' | Frequency: '%s'%n", testCase.getAmountInput(), testCase.getFrequency());

        CalculatorPage calculatorPage = new CalculatorPage(getDriver());

        // Step 1: Enter input amount and select payment frequency
        calculatorPage.enterAmount(testCase.getAmountInput());
        calculatorPage.selectFrequency(testCase.getFrequency());

        // Step 2: Click the Calculate button
        calculatorPage.clickCalculate();

        // Step 3 & 4: Calculate independent expected results using BigDecimal & Business Rules
        if ("SUCCESS".equalsIgnoreCase(testCase.getExpectedType())) {
            // Independent calculation using BigDecimal
            BigDecimal calculatedAnnual = CalculationUtils.calculateAnnualContribution(
                    testCase.getAmountInput(), 
                    testCase.getFrequency()
            );
            String independentExpectedResult = "Annual contribution: " + CalculationUtils.formatEuro(calculatedAnnual);

            // Step 5: Capture actual results from UI
            String actualResult = calculatorPage.getAnnualResultText();
            String actualError = calculatorPage.getErrorMessageText();

            System.out.printf("  EXPECTED RESULT : %s%n", independentExpectedResult);
            System.out.printf("  ACTUAL RESULT   : %s%n", actualResult);

            // Step 6: Verify error message is NOT displayed
            Assert.assertFalse(
                    calculatorPage.isErrorDisplayed(),
                    String.format("[%s] Unexpected error message displayed: '%s'. Rule: %s",
                            testCase.getTestCaseId(), actualError, testCase.getRuleReference())
            );

            // Step 7: Assert actual calculation matches independent expected result
            try {
                Assert.assertEquals(
                        actualResult, 
                        independentExpectedResult,
                        String.format("[%s] Calculation mismatch for amount='%s', freq='%s'. Rule Ref: %s",
                                testCase.getTestCaseId(),
                                testCase.getAmountInput(),
                                testCase.getFrequency(),
                                testCase.getRuleReference())
                );
                System.out.printf("  STATUS          : PASSED ✅%n");
            } catch (AssertionError e) {
                System.err.printf("  STATUS          : FAILED ❌ -> %s%n", e.getMessage());
                throw e;
            }

        } else if ("ERROR".equalsIgnoreCase(testCase.getExpectedType())) {
            // Independent validation message using Business Rule R8
            String independentExpectedError = CalculationUtils.validateCalculatorInput(testCase.getAmountInput());

            // Step 5: Capture actual error message from UI
            String actualError = calculatorPage.getErrorMessageText();
            String actualResult = calculatorPage.getAnnualResultText();

            System.out.printf("  EXPECTED ERROR  : %s%n", independentExpectedError);
            System.out.printf("  ACTUAL ERROR    : %s%n", actualError);

            // Step 6: Assert actual error message matches expected validation message
            try {
                Assert.assertEquals(
                        actualError, 
                        independentExpectedError,
                        String.format("[%s] Validation error mismatch for amount='%s'. Rule Ref: %s",
                                testCase.getTestCaseId(),
                                testCase.getAmountInput(),
                                testCase.getRuleReference())
                );

                // Ensure annual result is blank/not displayed when an error occurs
                Assert.assertFalse(
                        calculatorPage.isResultDisplayed(),
                        String.format("[%s] Annual result should be empty on error, but found: '%s'",
                                testCase.getTestCaseId(), actualResult)
                );
                System.out.printf("  STATUS          : PASSED ✅%n");
            } catch (AssertionError e) {
                System.err.printf("  STATUS          : FAILED ❌ -> %s%n", e.getMessage());
                throw e;
            }
        }
        System.out.println("================================================================================\n");
    }
}
