package dev.simmons.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmployeeTests {
    @Test
    public void testEquality() {
        Employee emp1 = new Employee();
        Employee emp2 = new Employee();
        Assertions.assertEquals("#0", emp1.toString());
        Assertions.assertEquals(emp1, emp2);
        emp1.setId(5);
        emp1.setFirstName("Test");
        emp1.setLastName("Testing");
        Assertions.assertEquals("Test Testing, #5", emp1.toString());
        Assertions.assertNotEquals(emp1, emp2);
        Assertions.assertTrue(emp1.compareTo(emp2) > 0);
    }
}
