package pages;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import utils.LoggerUtil;
import webDriver.WebDriverFacade;

/**
 * PageObject — Abstract base class for all Page Object classes.
 *
 * <p>
 * Every page object should extend this class. It provides:
 * <ul>
 * <li>Access to the current {@link WebDriver} instance.</li>
 * <li>{@link PageFactory} initialisation for {@code @FindBy} annotations.</li>
 * <li>Delegated access to {@link WebDriverFacade} utility methods.</li>
 * </ul>
 *
 * <h3>Example</h3>
 * 
 * <pre>
 *   public class LoginPage extends PageObject {
 *
 *       {@literal @}FindBy(id = "username")
 *       private WebElement usernameField;
 *
 *       public LoginPage(WebDriver driver) {
 *           super(driver);
 *       }
 *
 *       public void login(String user, String pass) {
 *           typeText(By.id("username"), user);
 *           typeText(By.id("password"), pass);
 *           clickElement(By.id("loginBtn"));
 *       }
 *   }
 * </pre>
 */
public abstract class PageObject {

    /** The WebDriver instance for this page. */
    protected final WebDriver driver;

    protected final Logger log = LoggerUtil.getLogger(getClass());

    /**
     * Constructs a PageObject and initialises {@link PageFactory}.
     *
     * @param driver the WebDriver bound to the current test thread
     */
    protected PageObject(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        log.debug("PageObject initialised: {}", getClass().getSimpleName());
    }

    // =========================================================================
    // Delegated facade helpers — keeps page-object code concise
    // =========================================================================

    protected void clickElement(By locator) {
        WebDriverFacade.clickElement(locator);
    }

    protected void typeText(By locator, String text) {
        WebDriverFacade.typeText(locator, text);
    }

    protected String getElementText(By locator) {
        return WebDriverFacade.getElementText(locator);
    }

    protected boolean isDisplayed(By locator) {
        return WebDriverFacade.isDisplayed(locator);
    }

    protected String getAttribute(By locator, String attribute) {
        return WebDriverFacade.getAttribute(locator, attribute);
    }
}
