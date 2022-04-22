package dev.simmons.utilities.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logging utility class for the banking application.
 */
public class Logger {
    /**
     * An enum representing the logging levels: INFO, DEBUG, WARNING, ERROR.
     */
    public enum Level {
        INFO, DEBUG, WARNING, ERROR;
    }

    /**
     * Writes a formatted representation of the given log level, timestamp and message.
     * @param level The log level for this line.
     * @param message The message to log.
     */
    public static void log(Level level, String message) {
        String info = String.format("|| %s :: %s :: %s%n", level.name(), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), message);
        System.out.print(info);
    }

    /**
     * Writes a formatted representation of the given log level, timestamp and message based off an exception message and stacktrace.
     * @param level The log level for this line.
     * @param ex The exception thrown to be logged.
     */
    public static void log(Level level, Exception ex) {
        log(level, ex.getMessage());
    }
}
