package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LoggerUtil — Thin wrapper around Log4j2 to provide named loggers.
 *
 * <h3>Usage</h3>
 * 
 * <pre>
 * private static final Logger log = LoggerUtil.getLogger(MyClass.class);
 * log.info("Starting test: {}", testName);
 * </pre>
 */
public class LoggerUtil {

    private LoggerUtil() {
        // Utility class — no instantiation
    }

    /**
     * Creates (or retrieves from cache) a Log4j2 {@link Logger} for the given
     * class.
     *
     * @param clazz the class whose name will be used as the logger name
     * @return a named {@link Logger}
     */
    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }

    /**
     * Creates (or retrieves from cache) a Log4j2 {@link Logger} for the given name.
     *
     * @param name the logger name
     * @return a named {@link Logger}
     */
    public static Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }
}
