package tests.backend;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import utils.ApiLoggerFilter; // Import ApiLoggerFilter
import utils.ConfigReader;
import utils.GeminiUtil; // Import GeminiUtil
import utils.LoggerUtil;

import java.lang.reflect.Method;

/**
 * BaseBackendTest — TestNG base class for all REST API tests using RestAssured.
 *
 * <p>
 * Lifecycle:
 * <ul>
 * <li>{@link BeforeSuite} — configure RestAssured global defaults once.</li>
 * <li>{@link BeforeClass} — build per-class {@link RequestSpecification}.</li>
 * <li>{@link AfterClass} — reset RestAssured spec.</li>
 * <li>{@link AfterSuite} — log suite completion.</li>
 * </ul>
 *
 * <p>
 * All API test classes should extend this class and use {@link #requestSpec}
 * as the base for their RestAssured calls.
 */
public class BaseBackendTest {

    protected static final Logger log = LoggerUtil.getLogger(BaseBackendTest.class);

    // =========================================================================
    // Extent Reports — shared across the suite
    // =========================================================================
    private static ExtentReports extent;

    /** Per-thread ExtentTest node so parallel tests write to their own section. */
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();

    protected static ExtentTest getExtentTest() {
        return extentTestThreadLocal.get();
    }

    /** Shared request specification. Subclasses may add headers, auth, etc. */
    protected RequestSpecification requestSpec;

    // =========================================================================
    // Suite-level hooks
    // =========================================================================

    @BeforeSuite(alwaysRun = true)
    public void setUpExtentReports() {
        String reportPath = System.getProperty("user.dir")
                + "/testReport/ApiExtentReport.html";
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(reportPath);
        htmlReporter.config().setReportName("API Automation Report");
        htmlReporter.config().setDocumentTitle("API Test Execution Report");

        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("API Base URL", ConfigReader.get("api.base.url", "https://jsonplaceholder.typicode.com"));
        log.info("ExtentReports initialised for API tests. Report will be at: {}", reportPath);
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownExtentReports() {
        if (extent != null) {
            extent.flush();
            log.info("ExtentReports flushed for API tests.");
        }
    }

    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        String apiBaseUrl = ConfigReader.get("api.base.url",
                "https://jsonplaceholder.typicode.com");
        RestAssured.baseURI = apiBaseUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        log.info("RestAssured configured. Base URI: {}", apiBaseUrl);
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        log.info("API test suite complete.");
    }

    // =========================================================================
    // Method-level hooks
    // =========================================================================

    /**
     * Creates an ExtentTest node for the current test method.
     *
     * @param method the current test method (used for ExtentReports node name)
     */
    @BeforeMethod(alwaysRun = true)
    public void setUpTestMethod(Method method) {
        String testName = method.getName();
        log.info("=== Starting API test: {} ===", testName);

        ExtentTest extentTest = extent.createTest(testName);
        extentTestThreadLocal.set(extentTest);
    }

    /**
     * After each test method:
     * <ol>
     * <li>Logs PASS / FAIL / SKIP status to ExtentReports.</li>
     * <li>Resets the ExtentTest thread local.</li>
     * </ol>
     *
     * @param result the TestNG result object for the finished test
     */
    @AfterMethod(alwaysRun = true)
    public void tearDownTestMethod(ITestResult result) {
        ExtentTest extentTest = getExtentTest();

        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                log.info("API TEST PASSED: {}", result.getName());
                if (extentTest != null)
                    extentTest.pass("Test passed.");
                break;

            case ITestResult.FAILURE:
                log.error("API TEST FAILED: {}", result.getName(), result.getThrowable());
                if (extentTest != null) {
                    extentTest.fail(result.getThrowable());

                    // --- LLM Integration for Backend Failure ---
                    try {
                        String testName = result.getName();
                        String errorMessage = result.getThrowable() != null ? result.getThrowable().getMessage()
                                : "No error message.";
                        String lastRequest = ApiLoggerFilter.getLastRequest();
                        String lastResponse = ApiLoggerFilter.getLastResponse();

                        String prompt = String.format(
                                "The API test '%s' failed. " +
                                        "Error message: %s. " +
                                        "Last API Request: %s. " +
                                        "Last API Response: %s. " +
                                        "Please provide a plain English explanation of what likely broke and suggest a fix.",
                                testName, errorMessage, lastRequest, lastResponse);

                        log.info("Calling Gemini LLM for explanation for API test: {}", testName);
                        String llmExplanation = GeminiUtil.generateContent(prompt);
                        extentTest.info("<b>LLM Analysis:</b><br>" + llmExplanation);
                        log.info("Gemini LLM explanation received for API test: {}", testName);
                    } catch (Exception e) {
                        log.error("Failed to get LLM explanation for backend test failure: {}", e.getMessage());
                        if (extentTest != null) {
                            extentTest.info("LLM Analysis could not be generated due to an error: " + e.getMessage());
                        }
                    }
                }
                break;

            case ITestResult.SKIP:
                log.warn("API TEST SKIPPED: {}", result.getName());
                if (extentTest != null)
                    extentTest.skip(result.getThrowable());
                break;

            default:
                break;
        }
        ApiLoggerFilter.clear(); // Clear captured request/response after each test
        extentTestThreadLocal.remove();
        log.info("=== Finished API test: {} ===", result.getName());
    }

    // =========================================================================
    // Class-level hooks
    // =========================================================================

    /**
     * Builds the shared {@link RequestSpecification} before each test class.
     * Override in subclasses to add authentication headers or custom base paths.
     */
    @BeforeClass(alwaysRun = true)
    public void setUpRequestSpec() {
        String apiBaseUrl = ConfigReader.get("api.base.url",
                "https://jsonplaceholder.typicode.com");

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(apiBaseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new ApiLoggerFilter()) // Add the API logger filter
                .log(LogDetail.URI)
                .build();

        log.info("RequestSpecification built for class: {}",
                getClass().getSimpleName());
    }

    @AfterClass(alwaysRun = true)
    public void tearDownRequestSpec() {
        RestAssured.reset();
        log.info("RestAssured reset after class: {}", getClass().getSimpleName());
    }
}
