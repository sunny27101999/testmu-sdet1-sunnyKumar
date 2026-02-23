package utils;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader — Singleton utility that loads {@code config.properties} once
 * and exposes its values via {@link #get(String)} and
 * {@link #get(String, String)}.
 *
 * <p>
 * The properties file must be placed at:
 * {@code src/test/resources/config.properties}
 *
 * <h3>Usage</h3>
 * 
 * <pre>
 * String browser = ConfigReader.get("browser");
 * String baseUrl = ConfigReader.get("base.url", "http://localhost");
 * </pre>
 */
public class ConfigReader {

    private static final Logger log = LoggerUtil.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";

    private static final Properties PROPS = new Properties();

    static {
        loadProperties();
    }

    private ConfigReader() {
        // Utility class — no instantiation
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the value mapped to {@code key}.
     *
     * @param key the property key
     * @return the property value
     * @throws IllegalArgumentException if the key is not found
     */
    public static String get(String key) {
        String value = PROPS.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    "Property '" + key + "' not found in " + CONFIG_FILE);
        }
        return value.trim();
    }

    /**
     * Returns the value mapped to {@code key}, or {@code defaultValue} if absent.
     *
     * @param key          the property key
     * @param defaultValue fallback value
     * @return the property value or the default
     */
    public static String get(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue).trim();
    }

    // -------------------------------------------------------------------------
    // Internal loading
    // -------------------------------------------------------------------------

    private static void loadProperties() {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (is == null) {
                throw new RuntimeException(
                        "'" + CONFIG_FILE + "' not found on the classpath. "
                                + "Ensure it exists under src/test/resources/");
            }
            PROPS.load(is);
            log.info("Loaded {} properties from '{}'", PROPS.size(), CONFIG_FILE);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + CONFIG_FILE, e);
        }
    }
}
