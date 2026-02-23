package tests.backend;

import io.restassured.response.Response;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.LoggerUtil;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * SampleBackendTest — Demonstrates basic REST API tests using
 * {@link BaseBackendTest}.
 *
 * <p>
 * Calls the public JSONPlaceholder API
 * ({@code https://jsonplaceholder.typicode.com})
 * to illustrate GET, POST, and response validation patterns.
 */
public class SampleBackendTest extends BaseBackendTest {

    private static final Logger log = LoggerUtil.getLogger(SampleBackendTest.class);

    // -------------------------------------------------------------------------
    // GET tests
    // -------------------------------------------------------------------------

    @Test(description = "GET /posts/1 — verify status 200 and key fields", groups = { "api", "smoke" })
    public void getPostById_shouldReturn200() {
        log.info("Calling GET /posts/1");

        given()
                .spec(requestSpec)
                .when()
                .get("/posts/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("userId", notNullValue())
                .body("title", not(emptyOrNullString()))
                .body("body", not(emptyOrNullString()));
    }

    @Test(description = "GET /posts — verify the response contains 100 posts", groups = { "api", "regression" })
    public void getAllPosts_shouldReturn100Items() {
        log.info("Calling GET /posts");

        int count = given()
                .spec(requestSpec)
                .when()
                .get("/posts")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$")
                .size();

        log.info("Number of posts returned: {}", count);
        Assert.assertEquals(count, 100, "Expected 100 posts");
    }

    // -------------------------------------------------------------------------
    // POST test
    // -------------------------------------------------------------------------

    @Test(description = "POST /posts — verify resource is created with status 201", groups = { "api", "smoke" })
    public void createPost_shouldReturn201() {
        log.info("Calling POST /posts");

        String requestBody = "{"
                + "\"title\": \"Automation Test Post\","
                + "\"body\": \"Created by SampleBackendTest\","
                + "\"userId\": 1"
                + "}";

        Response response = given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("title", equalTo("Automation Test Post"))
                .body("id", notNullValue())
                .extract()
                .response();

        int createdId = response.jsonPath().getInt("id");
        log.info("Created post with id: {}", createdId);
        Assert.assertTrue(createdId > 0, "Expected a positive post ID");
    }
}
