package utils;

import io.restassured.http.ContentType;
import org.json.JSONObject;

import static io.restassured.RestAssured.given;

public class GeminiUtil {

    private static final String API_URL = ConfigReader.get("gemini.api.url");
    private static final String API_KEY = ConfigReader.get("gemini.api.key");

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
    }

}
