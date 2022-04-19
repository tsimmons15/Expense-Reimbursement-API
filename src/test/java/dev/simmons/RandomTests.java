package dev.simmons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
