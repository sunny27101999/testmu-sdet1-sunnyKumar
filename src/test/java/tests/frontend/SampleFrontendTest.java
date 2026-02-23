package tests.frontend;

import org.testng.Assert;
import org.testng.annotations.Test;
import webDriver.WebDriverFacade;

/**
 * SampleFrontendTest — Demonstrates a basic UI test using
 * {@link BaseFrontendTest}.
 *
 * <p>
 * This test opens Google (configured via {@code base.url} in config.properties)
 * and verifies that the page title contains "Google".
 *
 * <p>
 * Extend this pattern to write real page-object-based tests.
 */
public class SampleFrontendTest extends BaseFrontendTest {

    @Test(description = "Verify the browser opens and navigates to the configured base URL")
    public void verifyPageTitleContainsGoogle() {
        String title = WebDriverFacade.getDriver().getTitle();
        log.info("Page title: {}", title);

        getExtentTest().info("Verifying page title contains 'Google'. Actual: " + title);
        Assert.assertTrue(title.contains("Google"),
                "Expected page title to contain 'Google' but was: " + title);
    }

    @Test(description = "Verify the browser's current URL matches the configured base URL")
    public void verifyCurrentUrl() {
        String currentUrl = WebDriverFacade.getDriver().getCurrentUrl();
        log.info("Current URL: {}", currentUrl);

        getExtentTest().info("Verifying URL contains 'google'. Actual: " + currentUrl);
        Assert.assertTrue(currentUrl.contains("google"),
                "Expected URL to contain 'google', but was: " + currentUrl);
    }
}
