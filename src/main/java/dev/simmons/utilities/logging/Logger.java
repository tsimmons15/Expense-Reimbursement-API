package dev.simmons.utilities.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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
     * The relative path the log is located in.
     */
    public static final String LOG_PATH = "./logs/logs.log";

    /**
     * Writes a formatted representation of the given log level, timestamp and message.
     * @param level The log level for this line.
     * @param message The message to log.
     */
    public static void log(Level level, String message) {
        // I was always told my imagination was my best quality.
        try {
            Path path = Paths.get(LOG_PATH);
            String info = String.format("%s :: %s :: %s\n", level.name(), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), message);
            Files.write(path, info.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException ioe) {
            // How do you handle an exception in your logger?
            ioe.printStackTrace();
        }
    }

    /**
     * Writes a formatted representation of the given log level, timestamp and message based off an exception message and stacktrace.
     * @param level The log level for this line.
     * @param ex The exception thrown to be logged.
     */
    public static void log(Level level, Exception ex) {
        log(level, ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
    }
}
