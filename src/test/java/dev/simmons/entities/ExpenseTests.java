package dev.simmons.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpenseTests {
    @Test
    void testEquality() {
        Expense exp1 = new Expense();
        Expense exp2 = new Expense();
        Assertions.assertEquals("Expense(0) for $0.00", exp1.toString());
        Assertions.assertEquals(exp1, exp2);
        exp1.setIssuer(5);
        exp1.setId(5);
        exp1.setDate(1000);
        exp1.setAmount(4000);
        Assertions.assertNotEquals(exp1, exp2);
        Assertions.assertTrue(exp1.compareTo(exp2) > 0);
    }
}
