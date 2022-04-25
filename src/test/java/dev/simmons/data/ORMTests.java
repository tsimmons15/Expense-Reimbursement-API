package dev.simmons.data;

import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ORMTests {
    private static Employee employee;
    private static Expense expense;
    @Test
    void createEmployee() {
        DataWrapperORM<Employee> orm = new PostgresORM<>();
        Employee emp = new Employee();
        emp.setFirstName("Testing");
        emp.setLastName("Testing");
        Employee received = null;
        try {
            received = orm.createEntity(emp);
            employee = new Employee(received);
        } catch (Exception e) {
            Assertions.fail();
        }

        Assertions.assertNotNull(received);
        Assertions.assertNotEquals(0, received.getId());
    }

    @Test
    void createExpense() {
        DataWrapperORM<Expense> orm = new PostgresORM<>();
        Expense exp = new Expense();
        exp.setAmount(100000);
        exp.setIssuer(0);
        exp.setStatus(Expense.Status.PENDING);
        exp.setDate(1234567890);
        Expense received = null;
        try {
            received = orm.createEntity(exp);
            expense = new Expense(received);
        } catch (Exception e) {
            Assertions.fail(e);
        }

        Assertions.assertNotNull(received);
        Assertions.assertNotEquals(0, received.getId());
    }

    @Test
    void getEmployeeById() {
        DataWrapperORM<Employee> orm = new PostgresORM<>();
        Employee received = null;
        try {
            received = new Employee(orm.getEntityById(employee.getId()));
        } catch (Exception e) {
            Assertions.fail(e);
        }

        Assertions.assertNotNull(received);
        Assertions.assertEquals(employee.getId(), received.getId());
    }

    @Test
    void getAllEmployees() {
        DataWrapperORM<Employee> orm = new PostgresORM<>();
        List<Employee> employees = null;

        try {
            employees = orm.getAllEntities();
        } catch (Exception e) {
            Assertions.fail(e);
        }

        Assertions.assertNotNull(employees);
        Assertions.assertTrue(employees.size() > 0);
    }

    @Test
    void getExpenseById() {
        DataWrapperORM<Expense> orm = new PostgresORM<>();
        Expense exp = null;

        try {
            exp = new Expense(orm.getEntityById(expense.getId()));
        } catch (Exception e) {
            Assertions.fail(e);
        }

        Assertions.assertNotNull(exp);
        Assertions.assertNotEquals(0, exp.getId());
    }

    @Test
    void getAllExpenses() {
        DataWrapperORM<Expense> orm = new PostgresORM<>();
        List<Expense> expenses = null;

        try {
            expenses = orm.getAllEntities();
        } catch (Exception e) {
            Assertions.fail(e);
        }

        Assertions.assertNotNull(expenses);
        Assertions.assertTrue(expenses.size() > 0);
    }

    @Test
    void updateEmployee() {
        DataWrapperORM<Employee> orm = new PostgresORM<>();
        Employee newEmp = new Employee(employee);
        newEmp.setLastName("Real Employee");

        Employee received = null;
        try {
            received = new Employee(orm.replaceEntity(newEmp));
        } catch (Exception e) {
            Assertions.fail(e);
        }

        Assertions.assertNotNull(received);
        Assertions.assertNotEquals(employee.getLastName(), received.getLastName());
    }

    @Test
    void updateExpense() {
        DataWrapperORM<Expense> orm = new PostgresORM<>();
        Expense newExp = new Expense(expense);
        newExp.setAmount(999999999);

        Expense received = null;

        try {
            received = new Expense(orm.replaceEntity(newExp));
        } catch (Exception e) {
            Assertions.fail(e);
        }

        Assertions.assertNotNull(received);
        Assertions.assertNotEquals(expense.getAmount(), received.getAmount());
    }

    @Test
    void deleteEmployee() {
        DataWrapperORM<Employee> orm = new PostgresORM<>();
        try {
            if (!orm.deleteEntity(employee.getId())) {
                Assertions.fail("Employee unsuccessfully deleted.");
            }

            Employee received = orm.getEntityById(employee.getId());
            Assertions.assertNull(received);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    void deleteExpense() {
        DataWrapperORM<Expense> orm = new PostgresORM<>();
        try {
            if (!orm.deleteEntity(expense.getId())) {
                Assertions.fail("Expense unsuccessfully deleted.");
            }

            Expense received = orm.getEntityById(expense.getId());
            Assertions.assertNull(received);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
}
