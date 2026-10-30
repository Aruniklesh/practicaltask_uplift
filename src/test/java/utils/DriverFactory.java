package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

/**
 * DriverFactory provides thread-safe WebDriver initialization and management
 * using ThreadLocal. It configures Chrome options and standard timeouts.
 */
public class DriverFactory {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    static {
        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            System.out.println("WebDriverManager setup notice (Selenium Manager will handle driver): " + e.getMessage());
        }
    }

    /**
     * Initializes a new ChromeDriver instance with standard options.
     * Supports headless execution via system property: -Dheadless=true
     *
     * @return Initialized WebDriver instance
     */
    public static WebDriver initDriver() {
        ChromeOptions options = new ChromeOptions();
        
        String headlessProp = System.getProperty("headless", "false");
        if (Boolean.parseBoolean(headlessProp)) {
            options.addArguments("--headless=new");
        }

        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));

        driverThreadLocal.set(driver);
        return driver;
    }

    /**
     * Retrieves the thread-local WebDriver instance for the current execution thread.
     */
    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    /**
     * Quits the current WebDriver instance and cleans up thread-local storage.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error quitting driver: " + e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
}
