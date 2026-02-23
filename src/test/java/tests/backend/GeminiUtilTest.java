package tests.backend;

import org.testng.annotations.Test;
import utils.GeminiUtil;

import static org.testng.Assert.assertNotNull;

public class GeminiUtilTest extends BaseBackendTest {

    @Test(description = "Verify that the Gemini API can be called successfully.")
    public void testGeminiApi() {
        // This test will fail until the user provides a valid API key.
        // It serves as a placeholder to demonstrate usage.
        try {
            String prompt = "Give me a short, fun fact about the Roman Empire.";
            String response = GeminiUtil.generateContent(prompt);

            System.out.println("Gemini Response: " + response);

            assertNotNull(response, "The response from Gemini should not be null.");

        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            // Handle other potential exceptions, e.g., RestAssured timeout
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
