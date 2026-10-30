package tests;

import base.BaseTest;
import models.Member;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.MembersTablePage;
import utils.CalculationUtils;
import utils.JsonUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MembersValidationTest implements Part 2 of the QA Automation Practical Assessment:
 * File-to-Screen Verification Test.
 *
 * Requirements:
 * 1. Reads member data from data/members.json using Jackson.
 * 2. Independently calculates expected values using Business Rules R1 through R7.
 * 3. Uses java.math.BigDecimal for all calculations to preserve precision.
 * 4. Navigates to the portal and locates table rows via data-member-id and cells via data-col.
 * 5. Compares actual on-screen values with independent expected values.
 * 6. Formats report output exactly as requested:
 *    Member ID: <id>
 *    Field: <field>
 *    Expected: <val>
 *    Actual: <val>
 *    Status: PASSED / FAILED
 *    Business Rule: <rule>
 */
public class MembersValidationTest extends BaseTest {

    private static final String MEMBERS_JSON_PATH = "data/members.json";

    /**
     * DataProvider reading all members from data/members.json.
     */
    @DataProvider(name = "membersDataProvider")
    public Object[][] getMembersData() {
        List<Member> members = JsonUtils.readMembersFromJson(MEMBERS_JSON_PATH);
        Object[][] data = new Object[members.size()][1];
        for (int i = 0; i < members.size(); i++) {
            data[i][0] = members.get(i);
        }
        return data;
    }

    /**
     * Table-integrity check (Phase 4 audit addition): confirms every member in
     * data/members.json has exactly one rendered row, there are no unexpected extra or
     * duplicate rows, and no two rows share the same data-member-id. The per-member
     * field test below only ever looks up rows by ID, so it would silently miss a
     * completely-missing member or a duplicated row; this test closes that gap.
     */
    @Test(description = "Verifies the rendered Members table has exactly one row per JSON member, with no missing or duplicate data-member-id values")
    public void testMembersTableIntegrity() {
        List<Member> expectedMembers = JsonUtils.readMembersFromJson(MEMBERS_JSON_PATH);
        MembersTablePage tablePage = new MembersTablePage(getDriver());
        tablePage.waitForTableToLoad();

        List<String> renderedIds = tablePage.getAllRenderedMemberIds();
        Set<String> uniqueRenderedIds = new HashSet<>(renderedIds);

        System.out.println("================================================================================");
        System.out.println("MEMBERS TABLE INTEGRITY CHECK");
        System.out.println("  Expected member count (from data/members.json): " + expectedMembers.size());
        System.out.println("  Rendered row count: " + renderedIds.size());
        System.out.println("  Unique rendered member IDs: " + uniqueRenderedIds.size());

        Assert.assertEquals(
                renderedIds.size(), expectedMembers.size(),
                "Rendered row count does not match the number of members in data/members.json "
                        + "(either rows are missing or unexpected extra rows were rendered)."
        );
        Assert.assertEquals(
                uniqueRenderedIds.size(), renderedIds.size(),
                "Duplicate data-member-id values found in the rendered table: " + renderedIds
        );
        for (Member m : expectedMembers) {
            Assert.assertTrue(
                    renderedIds.contains(m.getMemberId()),
                    "Member " + m.getMemberId() + " from data/members.json has no corresponding rendered row."
            );
        }
    }

    /**
     * Test method executed for each member record in data/members.json.
     */
    @Test(
            dataProvider = "membersDataProvider",
            description = "Validates on-screen member values against independent calculations from data/members.json"
    )
    public void testMemberRowCalculations(Member member) {
        String memberId = member.getMemberId();
        MembersTablePage tablePage = new MembersTablePage(getDriver());
        tablePage.waitForTableToLoad();

        SoftAssert softAssert = new SoftAssert();

        System.out.println("================================================================================");
        System.out.printf("STARTING VALIDATION FOR MEMBER: %s (%s)%n", memberId, member.getName());
        System.out.println("================================================================================");

        // 1. Verify Member Row Existence
        boolean rowExists = tablePage.isRowPresent(memberId);
        validateField(
                softAssert,
                memberId,
                "Row Existence",
                "Present",
                rowExists ? "Present" : "Missing",
                "Table Rendering"
        );
        if (!rowExists) {
            softAssert.assertAll();
            return;
        }

        // 2. Verify the rendered Member ID cell text itself (previously only row existence
        //    via the data-member-id attribute was checked; the displayed text of that
        //    column was not independently asserted).
        String actualMemberIdCell = tablePage.getCellValue(memberId, "member-id");
        validateField(
                softAssert,
                memberId,
                "Member ID (displayed cell)",
                memberId,
                actualMemberIdCell,
                "Data Integrity"
        );

        // 3. Verify Member Name
        String actualName = tablePage.getCellValue(memberId, "name");
        validateField(
                softAssert,
                memberId,
                "Member Name",
                member.getName(),
                actualName,
                "Data Integrity"
        );

        // 4. Verify Payment Frequency
        String actualFrequency = tablePage.getCellValue(memberId, "frequency");
        validateField(
                softAssert,
                memberId,
                "Payment Frequency",
                member.getFrequency(),
                actualFrequency,
                "Data Integrity"
        );

        // 5. Verify Contribution Amount (Rule R3: € sign and exactly 2 decimal places)
        String expectedContribution = CalculationUtils.formatEuro(member.getContributionAmount());
        String actualContribution = tablePage.getCellValue(memberId, "contribution");
        validateField(
                softAssert,
                memberId,
                "Contribution Amount",
                expectedContribution,
                actualContribution,
                "R3"
        );

        // 6. Verify Annual Contribution (Rule R1: multiplier, Rule R2: case-insensitive frequency)
        BigDecimal expectedAnnualVal = CalculationUtils.calculateAnnualContribution(
                member.getContributionAmount(), 
                member.getFrequency()
        );
        String expectedAnnual = CalculationUtils.formatEuro(expectedAnnualVal);
        String actualAnnual = tablePage.getCellValue(memberId, "annual");
        validateField(
                softAssert,
                memberId,
                "Annual Contribution",
                expectedAnnual,
                actualAnnual,
                "R1 / R2"
        );

        // 7. Verify Units Held (Rule R4: exactly 7 decimal places)
        String expectedUnitsHeld = CalculationUtils.formatUnitsHeld(member.getUnitsHeld());
        String actualUnitsHeld = tablePage.getCellValue(memberId, "units");
        validateField(
                softAssert,
                memberId,
                "Units Held",
                expectedUnitsHeld,
                actualUnitsHeld,
                "R4"
        );

        // 8. Verify Unit Price (Rule R3: € sign and exactly 2 decimal places)
        String expectedUnitPrice = CalculationUtils.formatEuro(member.getUnitPrice());
        String actualUnitPrice = tablePage.getCellValue(memberId, "price");
        validateField(
                softAssert,
                memberId,
                "Unit Price",
                expectedUnitPrice,
                actualUnitPrice,
                "R3"
        );

        // 9. Verify Fund Value (Rule R5: Units Held × Unit Price, rounded to 2 decimal places)
        BigDecimal expectedFundVal = CalculationUtils.calculateFundValue(
                member.getUnitsHeld(), 
                member.getUnitPrice()
        );
        String expectedFundValue = CalculationUtils.formatEuro(expectedFundVal);
        String actualFundValue = tablePage.getCellValue(memberId, "value");
        validateField(
                softAssert,
                memberId,
                "Fund Value",
                expectedFundValue,
                actualFundValue,
                "R5"
        );

        // 10. Verify Next Order Units (Rule R6: rounded to 4 decimal places)
        String expectedOrderUnits = CalculationUtils.formatOrderUnits(member.getNextOrderUnits());
        String actualOrderUnits = tablePage.getCellValue(memberId, "order-units");
        validateField(
                softAssert,
                memberId,
                "Next Order Units",
                expectedOrderUnits,
                actualOrderUnits,
                "R6"
        );

        // 11. Verify Order Status (Rule R7: Blocked if Next Order Units after 4-decimal rounding is 0, else Ready)
        String expectedOrderStatus = CalculationUtils.determineOrderStatus(member.getNextOrderUnits());
        String actualOrderStatus = tablePage.getCellValue(memberId, "order-status");
        validateField(
                softAssert,
                memberId,
                "Order Status",
                expectedOrderStatus,
                actualOrderStatus,
                "R7"
        );

        System.out.println("================================================================================\n");

        // Collate all assertion results for this member
        softAssert.assertAll();
    }

    /**
     * Helper to print comparison reports in the requested format and record soft assertions.
     */
    private void validateField(SoftAssert softAssert, String memberId, String fieldName, 
                               String expectedValue, String actualValue, String ruleRef) {
        boolean matches = (expectedValue != null && expectedValue.equals(actualValue));
        String status = matches ? "PASSED" : "FAILED";

        System.out.println("Member ID: " + memberId);
        System.out.println("Field: " + fieldName);
        System.out.println("Expected: " + expectedValue);
        System.out.println("Actual: " + actualValue);
        System.out.println("Status: " + status);
        System.out.println("Business Rule: " + ruleRef);
        System.out.println();

        softAssert.assertEquals(
                actualValue, 
                expectedValue,
                String.format("%nMember ID: %s%nField: %s%nExpected: %s%nActual: %s%nStatus: FAILED%nBusiness Rule: %s%n",
                        memberId, fieldName, expectedValue, actualValue, ruleRef)
        );
    }
}
