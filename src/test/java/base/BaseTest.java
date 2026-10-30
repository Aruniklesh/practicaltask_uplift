package base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import utils.DriverFactory;
import utils.TestListener;

/**
 * BaseTest serves as the parent class for all TestNG test classes.
 * It manages browser lifecycle per test class and resets page state before each test.
 * The @Listeners annotation guarantees ExtentReports generation in any execution mode.
 */
@Listeners(TestListener.class)
public class BaseTest {

    protected WebDriver driver;
    protected String baseUrl = "http://localhost:8000";

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        String urlProp = System.getProperty("app.url");
        if (urlProp != null && !urlProp.trim().isEmpty()) {
            baseUrl = urlProp.trim();
        }
        driver = DriverFactory.initDriver();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpMethod() {
        if (driver != null) {
            driver.get(baseUrl);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        DriverFactory.quitDriver();
    }

    /**
     * Getter for the current thread's WebDriver instance.
     */
    public WebDriver getDriver() {
        return DriverFactory.getDriver();
    }
}
