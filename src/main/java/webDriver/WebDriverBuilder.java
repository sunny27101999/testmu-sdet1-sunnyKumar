package webDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.ConfigReader;
import utils.LoggerUtil;

import org.apache.logging.log4j.Logger;

/**
 * WebDriverBuilder — Factory class responsible for creating {@link WebDriver}
 * instances.
 *
 * <p>
 * The browser type is resolved from {@code config.properties} (key:
 * {@code browser}).
 * Supported values: {@code chrome}, {@code firefox}, {@code edge}
 * (case-insensitive).
 * WebDriverManager automatically downloads and sets up the matching driver
 * binary.
 */
public class WebDriverBuilder {

    private static final Logger log = LoggerUtil.getLogger(WebDriverBuilder.class);

    private WebDriverBuilder() {
        // Utility class — no instantiation
    }

    /**
     * Creates and returns a {@link WebDriver} instance based on the configured
     * browser.
     *
     * @return a ready-to-use WebDriver
     * @throws IllegalArgumentException if the browser name is unsupported
     */
    public static WebDriver createDriver() {
        String browser = ConfigReader.get("browser", "chrome").trim().toLowerCase();
        log.info("Creating WebDriver for browser: {}", browser);

        switch (browser) {
            case "chrome":
                return buildChrome();
            case "firefox":
                return buildFirefox();
            case "edge":
                return buildEdge();
            default:
                throw new IllegalArgumentException(
                        "Unsupported browser: '" + browser + "'. Valid options: chrome, firefox, edge");
        }
    }

    // -------------------------------------------------------------------------
    // Private builders
    // -------------------------------------------------------------------------

    private static WebDriver buildChrome() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        if (isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
        log.debug("ChromeOptions configured: {}", options.asMap());
        return new ChromeDriver(options);
    }

    private static WebDriver buildFirefox() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (isHeadless()) {
            options.addArguments("-headless");
        }
        log.debug("FirefoxOptions configured: {}", options.toJson());
        return new FirefoxDriver(options);
    }

    private static WebDriver buildEdge() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }
        log.debug("EdgeOptions configured: {}", options.asMap());
        return new EdgeDriver(options);
    }

    private static boolean isHeadless() {
        return Boolean.parseBoolean(ConfigReader.get("headless", "false"));
    }
}
