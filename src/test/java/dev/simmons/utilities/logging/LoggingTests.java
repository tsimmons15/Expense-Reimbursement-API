package dev.simmons.utilities.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoggingTests {
    @Test
    public void writeLocation() throws IOException {
        File file = new File("./test.txt");
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter writer = new FileWriter(file);
        writer.write("This is text.");
        writer.close();

        file.delete();
    }

    @Test
    public void logInfo() {
        Logger.log(Logger.Level.INFO, "How's your day?");
        File file = new File(Logger.LOG_PATH);
        Assertions.assertTrue(file.exists());
        try {
            List<String> lines = Files.readAllLines(Paths.get(Logger.LOG_PATH));
            String line = lines.get(lines.size() - 1);
            String[] pieces = line.split(" :: ");
            Assertions.assertTrue(line.startsWith("INFO :: "));
            LocalDateTime timestamp = LocalDateTime.parse(pieces[1]);
            long diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - timestamp.toEpochSecond(ZoneOffset.UTC);
            Assertions.assertTrue(diff < 60 && diff >= 0);
        } catch (IOException ioe) {
            Assertions.fail();
        }
    }

    @Test
    public void logDebug() {
        Logger.log(Logger.Level.DEBUG, "What's going on?");
        File file = new File(Logger.LOG_PATH);
        Assertions.assertTrue(file.exists());
        try {
            List<String> lines = Files.readAllLines(Paths.get(Logger.LOG_PATH));
            String line = lines.get(lines.size() - 1);
            String[] pieces = line.split(" :: ");
            Assertions.assertTrue(line.startsWith("DEBUG :: "));
            LocalDateTime timestamp = LocalDateTime.parse(pieces[1]);
            long diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - timestamp.toEpochSecond(ZoneOffset.UTC);
            Assertions.assertTrue(diff < 60 && diff >= 0);
        } catch (IOException ioe) {
            Assertions.fail();
        }
    }

    @Test
    public void logWarning() {
        Logger.log(Logger.Level.WARNING, "You better be careful.");
        File file = new File(Logger.LOG_PATH);
        Assertions.assertTrue(file.exists());
        try {
            List<String> lines = Files.readAllLines(Paths.get(Logger.LOG_PATH));
            String line = lines.get(lines.size() - 1);
            String[] pieces = line.split(" :: ");
            Assertions.assertTrue(line.startsWith("WARNING :: "));
            LocalDateTime timestamp = LocalDateTime.parse(pieces[1]);
            long diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - timestamp.toEpochSecond(ZoneOffset.UTC);
            Assertions.assertTrue(diff < 60 && diff >= 0);
        } catch (IOException ioe) {
            Assertions.fail();
        }
    }

    @Test
    public void logError() {
        Logger.log(Logger.Level.ERROR, "Well, now you've done it!");
        File file = new File(Logger.LOG_PATH);
        Assertions.assertTrue(file.exists());
        try {
            List<String> lines = Files.readAllLines(Paths.get(Logger.LOG_PATH));
            String line = lines.get(lines.size() - 1);
            String[] pieces = line.split(" :: ");
            Assertions.assertTrue(line.startsWith("ERROR :: "));
            LocalDateTime timestamp = LocalDateTime.parse(pieces[1]);
            long diff = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - timestamp.toEpochSecond(ZoneOffset.UTC);
            Assertions.assertTrue(diff < 60 && diff >= 0);
        } catch (IOException ioe) {
            Assertions.fail();
        }
    }

}
