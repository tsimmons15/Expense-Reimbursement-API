package dev.simmons.utilities.logging;

import java.io.File;
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
    public static final String LOG_FILE = "logs.log";
    public static final String LOG_DIR = "./logs";

    /**
     * Writes a formatted representation of the given log level, timestamp and message.
     * @param level The log level for this line.
     * @param message The message to log.
     */
    public static void log(Level level, String message) {
        // I was always told my imagination was my best quality.
        //try {
            /*Path path = Paths.get(LOG_DIR, LOG_FILE);
            if (!Files.exists(path)) {
                setupLoggingFile(path);
            }*/
            String info = String.format("|| %s :: %s :: %s\n", level.name(), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), message);
            //Files.write(path, info.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            System.out.println(info);

        /*} catch (IOException ioe) {
            // How do you handle an exception in your logger?
            //ioe.printStackTrace();
            System.out.println(ioe.getMessage());
        }*/
    }

    private static void setupLoggingFile(Path path) throws IOException {
        Path dir = Paths.get(LOG_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectory(Paths.get(LOG_DIR));
        }
        Files.createFile(path);
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
