package tests.backend;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import utils.ConfigReader;
import utils.LoggerUtil;

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

    /** Shared request specification. Subclasses may add headers, auth, etc. */
    protected RequestSpecification requestSpec;

    // =========================================================================
    // Suite-level hooks
    // =========================================================================

    @BeforeSuite(alwaysRun = true)
    public void configureSuite() {
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
