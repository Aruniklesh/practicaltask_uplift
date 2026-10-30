package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MembersTablePage implements the Page Object Model (POM) for the
 * Members table section populated dynamically from data/members.json.
 *
 * Actual Application Selectors:
 * - Table element: id="members-table"
 * - Row element:   tr[data-member-id='{memberId}']
 * - Cell element:  td[data-col='{column}']
 */
public class MembersTablePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By table = By.id("members-table");
    private final By tableRows = By.cssSelector("#members-table tbody tr[data-member-id]");

    public MembersTablePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Waits until the members table has dynamically rendered its data rows.
     */
    public MembersTablePage waitForTableToLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(table));
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        return this;
    }

    /**
     * Checks if a member row exists in the DOM.
     */
    public boolean isRowPresent(String memberId) {
        By rowLocator = By.cssSelector("tr[data-member-id='" + memberId + "']");
        return !driver.findElements(rowLocator).isEmpty();
    }

    /**
     * Checks if a specific cell exists within a member row.
     */
    public boolean isCellPresent(String memberId, String column) {
        By cellLocator = By.cssSelector("tr[data-member-id='" + memberId + "'] td[data-col='" + column + "']");
        return !driver.findElements(cellLocator).isEmpty();
    }

    /**
     * Retrieves the count of rendered member rows in the table.
     */
    public int getRowCount() {
        return driver.findElements(tableRows).size();
    }

    /**
     * Retrieves the list of all member IDs currently rendered in the table.
     */
    public List<String> getAllRenderedMemberIds() {
        waitForTableToLoad();
        List<WebElement> rows = driver.findElements(tableRows);
        List<String> memberIds = new ArrayList<>();
        for (WebElement row : rows) {
            memberIds.add(row.getAttribute("data-member-id"));
        }
        return memberIds;
    }

    /**
     * Retrieves the text content of a specific cell using memberId and column identifier.
     *
     * @param memberId Value of data-member-id attribute on the row (e.g. "M001")
     * @param column Value of data-col attribute on the cell (e.g. "annual", "order-status")
     * @return Trimmed text of the target cell
     */
    public String getCellValue(String memberId, String column) {
        By cellLocator = By.cssSelector("tr[data-member-id='" + memberId + "'] td[data-col='" + column + "']");
        WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(cellLocator));
        return cell.getText().trim();
    }

    /**
     * Collects all column values for a member row into a key-value Map.
     */
    public Map<String, String> getMemberRowData(String memberId) {
        waitForTableToLoad();
        Map<String, String> rowData = new HashMap<>();
        String[] columns = {
                "member-id", "name", "frequency", "contribution",
                "annual", "units", "price", "value", "order-units", "order-status"
        };
        for (String col : columns) {
            rowData.put(col, getCellValue(memberId, col));
        }
        return rowData;
    }
}
