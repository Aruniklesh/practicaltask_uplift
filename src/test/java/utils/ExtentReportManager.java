package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

/**
 * ExtentReportManager initializes and configures the interactive Extent HTML report.
 * Ensures the target output directory is safely created on fresh checkouts.
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String reportPath = System.getProperty("user.dir") + File.separator + "target" + File.separator + "extent-reports" + File.separator + "ExecutionReport.html";
            File reportFile = new File(reportPath);
            if (reportFile.getParentFile() != null && !reportFile.getParentFile().exists()) {
                reportFile.getParentFile().mkdirs();
            }

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle("Contribution Portal QA Automation Report");
            sparkReporter.config().setReportName("Functional & Regression Test Execution Results");
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setEncoding("UTF-8");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("Application Under Test", "Contribution Portal");
            extent.setSystemInfo("Environment URL", "http://localhost:8000");
            extent.setSystemInfo("Test Runner", "TestNG");
            extent.setSystemInfo("Automation Framework", "Selenium Java (Page Object Model)");
            extent.setSystemInfo("Rules Reference", "README.md (Rules R1 to R8)");
        }
        return extent;
    }

    public static void setTest(ExtentTest test) {
        testThreadLocal.set(test);
    }

    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }

    public static void removeTest() {
        testThreadLocal.remove();
    }
}
