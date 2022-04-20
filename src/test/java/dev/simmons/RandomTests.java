package dev.simmons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RandomTests {
    @Test
    public void shortCircuit() {
        count = 0;
        Assertions.assertEquals(0, count);
        if (returnABoolean() || returnABoolean()) {
            System.out.println("The booleans work is done");
        }
        Assertions.assertEquals(1, count);

        if (returnABoolean() && returnABoolean()) {
            System.out.println("The boolean work be done");
        }
        Assertions.assertEquals(3, count);
    }
    private static int count = 0;
    private boolean returnABoolean() {
        count++;
        return true;
    }

    @Test
    public void directoryTests() {
        Assertions.assertTrue(Files.exists(Paths.get("./logs")));
        Assertions.assertFalse(Files.exists(Paths.get("./logging")));
        try {
            Files.createDirectory(Paths.get("./logging"));
            Files.write(Files.createFile(Paths.get("./logging/logs.log")), "test".getBytes());
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
    }
}