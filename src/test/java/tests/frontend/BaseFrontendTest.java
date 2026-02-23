package tests.frontend;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.ConfigReader;
import utils.GeminiUtil; // Import GeminiUtil
import utils.LoggerUtil;
import webDriver.WebDriverBuilder;
import webDriver.WebDriverFacade;

import java.lang.reflect.Method;

/**
 * BaseFrontendTest — TestNG base class for all Selenium UI tests.
 *
 * <p>
 * Lifecycle managed by TestNG annotations:
 * <ul>
 * <li>{@link BeforeSuite} — initialise ExtentReports once per run</li>
 * <li>{@link BeforeMethod} — launch browser, navigate to base URL</li>
 * <li>{@link AfterMethod} — capture screenshot on failure, quit driver</li>
 * <li>{@link AfterSuite} — flush ExtentReports HTML file</li>
 * </ul>
 *
 * <p>
 * All UI test classes should extend this class.
 */
public class BaseFrontendTest {

    protected static final Logger log = LoggerUtil.getLogger(BaseFrontendTest.class);

    // =========================================================================
    // Extent Reports — shared across the suite
    // =========================================================================
    private static ExtentReports extent;

    /** Per-thread ExtentTest node so parallel tests write to their own section. */
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();

    protected static ExtentTest getExtentTest() {
        return extentTestThreadLocal.get();
    }

    // =========================================================================
    // Suite-level hooks
    // =========================================================================

    @BeforeSuite(alwaysRun = true)
    public void setUpExtentReports() {
        String reportPath = System.getProperty("user.dir")
                + "/testReport/ExtentReport.html";
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(reportPath);
        htmlReporter.config().setReportName("UI Automation Report");
        htmlReporter.config().setDocumentTitle("Test Execution Report");

        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Browser", ConfigReader.get("browser", "chrome"));
        log.info("ExtentReports initialised. Report will be at: {}", reportPath);
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownExtentReports() {
        if (extent != null) {
            extent.flush();
            log.info("ExtentReports flushed.");
        }
    }

    // =========================================================================
    // Method-level hooks
    // =========================================================================

    /**
     * Launches the browser and navigates to {@code base.url} before each test.
     *
     * @param method the current test method (used for ExtentReports node name)
     */
    @BeforeMethod(alwaysRun = true)
    public void setUpDriver(Method method) {
        String testName = method.getName();
        log.info("=== Starting test: {} ===", testName);

        ExtentTest extentTest = extent.createTest(testName);
        extentTestThreadLocal.set(extentTest);

        WebDriverFacade.setDriver(WebDriverBuilder.createDriver());
        String baseUrl = ConfigReader.get("base.url", "https://www.google.com");
        WebDriverFacade.navigateTo(baseUrl);
        log.info("Browser launched and navigated to: {}", baseUrl);
    }

    /**
     * After each test:
     * <ol>
     * <li>Captures a screenshot if the test failed and attaches it to the
     * report.</li>
     * <li>Logs PASS / FAIL / SKIP status to ExtentReports.</li>
     * <li>Quits the browser.</li>
     * </ol>
     *
     * @param result the TestNG result object for the finished test
     */

    /*
     * 
     * 
     * 
     * Task 3 — went with Option A (Failure Explainer)
     * 
     * Honest reason — I had 2 days.
     * 
     * Option B (Flaky Test Classifier) sounds great on paper but to classify flaky
     * tests
     * properly you need multiple test runs,
     * enough failure logs, and consistent data patterns.
     * That takes time I didn't have.
     * Option A made complete sense for this timeline. A test fails,
     * LLM instantly tells you what broke and how to fix it.
     * No extra runs, no data collection, no waiting.
     * Just a failed test and an instant answer in your report.
     * That's real value. That's what I built.
     */

    @AfterMethod(alwaysRun = true)
    public void tearDownDriver(ITestResult result) {
        ExtentTest extentTest = getExtentTest();

        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                log.info("TEST PASSED: {}", result.getName());
                if (extentTest != null)
                    extentTest.pass("Test passed.");
                break;

            case ITestResult.FAILURE:
                log.error("TEST FAILED: {}", result.getName(), result.getThrowable());
                if (extentTest != null) {
                    String screenshotPath = WebDriverFacade.takeScreenshot(result.getName());
                    extentTest.fail(result.getThrowable());
                    if (screenshotPath != null) {
                        extentTest.addScreenCaptureFromPath(screenshotPath,
                                "Failure Screenshot");
                    }

                    // --- LLM Integration for Frontend Failure ---
                    try {
                        String testName = result.getName();
                        String errorMessage = result.getThrowable() != null ? result.getThrowable().getMessage()
                                : "No error message.";
                        String currentUrl = WebDriverFacade.getCurrentUrl(); // Get current URL

                        String prompt = String.format(
                                "The UI test '%s' failed. " +
                                        "Error message: %s. " +
                                        "Current URL: %s. " +
                                        "A screenshot was captured. " +
                                        "Please provide a plain English explanation of what likely broke and suggest a fix.",
                                testName, errorMessage, currentUrl);

                        log.info("Calling Gemini LLM for explanation for test: {}", testName);
                        String llmExplanation = GeminiUtil.generateContent(prompt);
                        extentTest.info("<b>LLM Analysis:</b><br>" + llmExplanation);
                        log.info("Gemini LLM explanation received for test: {}", testName);
                    } catch (Exception e) {
                        log.error("Failed to get LLM explanation for frontend test failure: {}", e.getMessage());
                        if (extentTest != null) {
                            extentTest.info("LLM Analysis could not be generated due to an error: " + e.getMessage());
                        }
                    }
                }
                break;

            case ITestResult.SKIP:
                log.warn("TEST SKIPPED: {}", result.getName());
                if (extentTest != null)
                    extentTest.skip(result.getThrowable());
                break;

            default:
                break;
        }

        WebDriverFacade.quitDriver();
        extentTestThreadLocal.remove();
        log.info("=== Finished test: {} ===", result.getName());
    }
}
