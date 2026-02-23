package utils;

import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

public class GeminiUtil {

    private static final Logger log = LoggerUtil.getLogger(GeminiUtil.class);
    private static final String API_URL = ConfigReader.get("gemini.api.url");
    private static final String API_KEY = ConfigReader.get("gemini.api.key");
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 second

    public static String generateContent(String prompt) {
        if (API_KEY == null || API_KEY.equals("YOUR_FREE_GEMINI_API_KEY") || API_KEY.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Gemini API key is not configured in config.properties. Please replace 'YOUR_FREE_GEMINI_API_KEY' with your actual key.");
        }

        String url = API_URL + "?key=" + API_KEY;
        JSONObject requestBody = new JSONObject();
        requestBody.put("contents", new JSONObject[] {
                new JSONObject().put("parts", new JSONObject[] {
                        new JSONObject().put("text", prompt)
                })
        });

        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                String response = given()
                        .urlEncodingEnabled(false)
                        .contentType(ContentType.JSON)
                        .body(requestBody.toString())
                        .when()
                        .post(url)
                        .then()
                        .statusCode(200)
                        .extract().body().asString();

                JSONObject responseJson = new JSONObject(response);
                return responseJson.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            } catch (Exception e) {
                if (i < MAX_RETRIES) {
                    log.warn("Gemini API call failed. Retrying {}/{}... Error: {}", i + 1, MAX_RETRIES, e.getMessage());
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry delay interrupted: {}", ie.getMessage());
                        throw new RuntimeException("Gemini API call interrupted during retry delay", ie);
                    }
                } else {
                    log.error("Gemini API call failed after {} retries. Error: {}", MAX_RETRIES, e.getMessage());
                    throw new RuntimeException("Failed to get response from Gemini API after multiple retries", e);
                }
            }
        }
        // This part should technically be unreachable, but good practice to include a final throw
        throw new RuntimeException("Unexpected error: Gemini API call logic did not return or throw.");
    }

}
