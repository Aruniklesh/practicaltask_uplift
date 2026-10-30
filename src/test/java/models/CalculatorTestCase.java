package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model representing a test scenario for the Annual Contribution Calculator.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalculatorTestCase {
    private String testCaseId;
    private String description;
    private String amountInput;
    private String frequency;
    private String expectedType; // "SUCCESS" or "ERROR"
    private String expectedResult; // The expected result text or error text
    private String ruleReference;

    public CalculatorTestCase() {
    }

    public CalculatorTestCase(String testCaseId, String description, String amountInput, 
                              String frequency, String expectedType, String expectedResult, 
                              String ruleReference) {
        this.testCaseId = testCaseId;
        this.description = description;
        this.amountInput = amountInput;
        this.frequency = frequency;
        this.expectedType = expectedType;
        this.expectedResult = expectedResult;
        this.ruleReference = ruleReference;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmountInput() {
        return amountInput;
    }

    public void setAmountInput(String amountInput) {
        this.amountInput = amountInput;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public void setExpectedType(String expectedType) {
        this.expectedType = expectedType;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getRuleReference() {
        return ruleReference;
    }

    public void setRuleReference(String ruleReference) {
        this.ruleReference = ruleReference;
    }

    @Override
    public String toString() {
        return "[" + testCaseId + "] " + description + " (Input: '" + amountInput + "', Freq: '" + frequency + "')";
    }
}
