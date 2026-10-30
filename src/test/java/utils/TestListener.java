package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestListener hooks into TestNG test lifecycle events to log execution
 * status to ExtentReports and capture visual screenshots upon failure.
 */
public class TestListener implements ITestListener {

    private static final ExtentReports extent = ExtentReportManager.getInstance();

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        Object[] parameters = result.getParameters();

        if (parameters.length > 0 && parameters[0] != null) {
            testName += " - " + parameters[0].toString();
        }

        ExtentTest test = extent.createTest(testName, description);
        ExtentReportManager.setTest(test);
        test.log(Status.INFO, "Execution started for: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, "Test PASSED: " + result.getMethod().getMethodName());
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, "Test FAILED: " + result.getMethod().getMethodName());
            test.log(Status.FAIL, result.getThrowable());

            // Capture screenshot and embed directly into Extent Report
            try {
                WebDriver driver = DriverFactory.getDriver();
                if (driver != null) {
                    String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
                    test.addScreenCaptureFromBase64String("data:image/png;base64," + base64Screenshot, "Failure Screenshot");
                }
            } catch (Exception e) {
                test.log(Status.WARNING, "Could not capture failure screenshot: " + e.getMessage());
            }
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, "Test SKIPPED: " + result.getMethod().getMethodName());
        }
        ExtentReportManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }
}
