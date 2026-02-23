package webDriver;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.LoggerUtil;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WebDriverFacade — Thread-safe wrapper around {@link WebDriver}.
 *
 * <p>
 * Stores driver instances in a {@link ThreadLocal} so parallel tests do not
 * share a single browser session. Provides high-level helpers for element
 * interactions, explicit waits, and screenshot capture.
 *
 * <h3>Usage</h3>
 * 
 * <pre>
 * WebDriverFacade.setDriver(WebDriverBuilder.createDriver());
 * WebDriverFacade.getDriver().get("https://example.com");
 * WebDriverFacade.clickElement(By.id("submit"));
 * WebDriverFacade.quitDriver();
 * </pre>
 */
public class WebDriverFacade {

    private static final Logger log = LoggerUtil.getLogger(WebDriverFacade.class);

    /** Default explicit-wait timeout in seconds. */
    private static final int DEFAULT_WAIT_SECONDS = 10;

    /** Thread-local storage for parallel test support. */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    private WebDriverFacade() {
        // Utility class — no instantiation
    }

    // =========================================================================
    // Driver lifecycle
    // =========================================================================

    /**
     * Binds the given {@link WebDriver} to the current thread.
     *
     * @param driver the driver instance to bind
     */
    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
        log.info("WebDriver set for thread: {}", Thread.currentThread().getName());
    }

    /**
     * Returns the {@link WebDriver} bound to the current thread.
     *
     * @return the current thread's WebDriver
     * @throws IllegalStateException if no driver has been set for this thread
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "No WebDriver is set for thread: " + Thread.currentThread().getName()
                            + ". Call WebDriverFacade.setDriver() before using the driver.");
        }
        return driver;
    }

    /**
     * Quits the driver and removes it from {@link ThreadLocal} storage.
     * Safe to call even if no driver was set.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("WebDriver quit for thread: {}", Thread.currentThread().getName());
            } catch (Exception e) {
                log.warn("Error while quitting WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    /**
     * Navigates the driver to the given URL.
     *
     * @param url full URL string
     */
    public static void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        getDriver().get(url);
    }

    // =========================================================================
    // Element interactions
    // =========================================================================

    /**
     * Clicks a web element identified by the given {@link By} locator, after
     * waiting for it to be clickable.
     *
     * @param locator the element locator
     */
    public static void clickElement(By locator) {
        log.debug("Clicking element: {}", locator);
        waitForClickability(locator, DEFAULT_WAIT_SECONDS).click();
    }

    /**
     * Clears the field and types the given text into an element.
     *
     * @param locator the element locator
     * @param text    the text to type
     */
    public static void typeText(By locator, String text) {
        log.debug("Typing '{}' into element: {}", text, locator);
        WebElement element = waitForVisibility(locator, DEFAULT_WAIT_SECONDS);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Returns the visible (inner) text of an element.
     *
     * @param locator the element locator
     * @return the element's text content, trimmed
     */
    public static String getElementText(By locator) {
        String text = waitForVisibility(locator, DEFAULT_WAIT_SECONDS).getText().trim();
        log.debug("Text of element {}: '{}'", locator, text);
        return text;
    }

    /**
     * Returns {@code true} if the element is currently displayed on the page.
     *
     * @param locator the element locator
     * @return {@code true} when visible
     */
    public static boolean isDisplayed(By locator) {
        try {
            return getDriver().findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Returns the value of the named attribute on the element.
     *
     * @param locator       the element locator
     * @param attributeName the HTML attribute name
     * @return the attribute value, or {@code null} if absent
     */
    public static String getAttribute(By locator, String attributeName) {
        return waitForVisibility(locator, DEFAULT_WAIT_SECONDS).getAttribute(attributeName);
    }

    /**
     * Selects a dropdown option by visible text.
     *
     * @param locator     the &lt;select&gt; element locator
     * @param visibleText the option text to select
     */
    public static void selectByVisibleText(By locator, String visibleText) {
        log.debug("Selecting '{}' from dropdown: {}", visibleText, locator);
        new org.openqa.selenium.support.ui.Select(
                waitForVisibility(locator, DEFAULT_WAIT_SECONDS))
                .selectByVisibleText(visibleText);
    }

    // =========================================================================
    // Explicit waits
    // =========================================================================

    /**
     * Waits until the element located by {@code locator} is visible.
     *
     * @param locator    the element locator
     * @param timeoutSec maximum wait in seconds
     * @return the visible {@link WebElement}
     */
    public static WebElement waitForVisibility(By locator, int timeoutSec) {
        log.debug("Waiting up to {}s for visibility of: {}", timeoutSec, locator);
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the element is both visible and enabled (ready to be clicked).
     *
     * @param locator    the element locator
     * @param timeoutSec maximum wait in seconds
     * @return the clickable {@link WebElement}
     */
    public static WebElement waitForClickability(By locator, int timeoutSec) {
        log.debug("Waiting up to {}s for clickability of: {}", timeoutSec, locator);
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Waits until the element identified by {@code locator} is no longer
     * present or visible in the DOM.
     *
     * @param locator    the element locator
     * @param timeoutSec maximum wait in seconds
     */
    public static void waitForInvisibility(By locator, int timeoutSec) {
        log.debug("Waiting up to {}s for invisibility of: {}", timeoutSec, locator);
        new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // =========================================================================
    // Screenshots
    // =========================================================================

    /**
     * Captures a screenshot and saves it to
     * {@code target/screenshots/<fileName>_<timestamp>.png}.
     *
     * @param fileName base name for the screenshot file (no extension)
     * @return the absolute path of the saved file, or {@code null} on error
     */
    public static String takeScreenshot(String fileName) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String path = System.getProperty("user.dir")
                    + File.separator + "target"
                    + File.separator + "screenshots"
                    + File.separator + fileName + "_" + timestamp + ".png";

            File srcFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
            File destFile = new File(path);
            FileUtils.copyFile(srcFile, destFile);
            log.info("Screenshot saved: {}", path);
            return path;
        } catch (IOException e) {
            log.error("Failed to save screenshot for '{}': {}", fileName, e.getMessage());
            return null;
        }
    }
}
