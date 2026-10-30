package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * CalculatorPage implements the Page Object Model (POM) for the
 * Annual Contribution Calculator section on the portal.
 *
 * Actual Application Selectors:
 * - Amount Input:      id="amount"
 * - Frequency Select:  id="frequency"
 * - Calculate Button:  id="calculate"
 * - Annual Result Div: id="annual-result"
 * - Error Message Div: id="error-msg"
 */
public class CalculatorPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Actual Application Locators
    private final By amountInput = By.id("amount");
    private final By frequencyDropdown = By.id("frequency");
    private final By calculateButton = By.id("calculate");
    private final By annualResult = By.id("annual-result");
    private final By errorMessage = By.id("error-msg");

    public CalculatorPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Clears and enters the contribution amount into the input field.
     */
    public CalculatorPage enterAmount(String amount) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(amountInput));
        input.clear();
        if (amount != null && !amount.isEmpty()) {
            input.sendKeys(amount);
        }
        return this;
    }

    /**
     * Selects payment frequency from the dropdown case-insensitively (Rule R2).
     * Matches either the option 'value' (e.g. 'weekly') or visible text (e.g. 'Weekly')
     * regardless of whether input is 'MONTHLY', 'monthly', or 'Monthly'.
     */
    public CalculatorPage selectFrequency(String frequency) {
        if (frequency == null || frequency.trim().isEmpty()) {
            return this;
        }
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(frequencyDropdown));
        Select select = new Select(dropdown);
        
        boolean matched = false;
        for (WebElement option : select.getOptions()) {
            String val = option.getAttribute("value");
            String text = option.getText();
            if ((val != null && val.equalsIgnoreCase(frequency.trim())) ||
                (text != null && text.equalsIgnoreCase(frequency.trim()))) {
                select.selectByVisibleText(text);
                matched = true;
                break;
            }
        }
        if (!matched) {
            throw new IllegalArgumentException("Unsupported payment frequency in dropdown: " + frequency);
        }
        return this;
    }

    /**
     * Clicks the Calculate button.
     */
    public CalculatorPage clickCalculate() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(calculateButton));
        button.click();
        return this;
    }

    /**
     * Helper to perform complete calculation entry in one step.
     */
    public CalculatorPage performCalculation(String amount, String frequency) {
        enterAmount(amount);
        selectFrequency(frequency);
        clickCalculate();
        return this;
    }

    /**
     * Retrieves the text displayed in the annual result container.
     */
    public String getAnnualResultText() {
        return driver.findElement(annualResult).getText().trim();
    }

    /**
     * Retrieves the text displayed in the error message container.
     */
    public String getErrorMessageText() {
        return driver.findElement(errorMessage).getText().trim();
    }

    /**
     * Checks if error message is present and non-empty.
     */
    public boolean isErrorDisplayed() {
        try {
            WebElement error = driver.findElement(errorMessage);
            return !error.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if annual result is present and non-empty.
     */
    public boolean isResultDisplayed() {
        try {
            WebElement result = driver.findElement(annualResult);
            return !result.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
