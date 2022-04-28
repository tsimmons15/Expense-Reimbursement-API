package dev.simmons.data;

import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import org.junit.jupiter.api.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ORMTests {
    private static Employee employee;
    private static Expense expense;
    @Test
    @Order(1)
    void createEmployee() {
        DataWrapperORM<Employee> orm = new PostgresORM<>(Employee.class);
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
    @Order(2)
    void createExpense() {
        DataWrapperORM<Expense> orm = new PostgresORM<>(Expense.class);
        Expense exp = new Expense();
        exp.setAmount(100000);
        exp.setIssuer(1);
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
    @Order(3)
    void getEmployeeById() {
        DataWrapperORM<Employee> orm = new PostgresORM<>(Employee.class);
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
    @Order(4)
    void getAllEmployees() {
        DataWrapperORM<Employee> orm = new PostgresORM<>(Employee.class);
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
    @Order(5)
    void getExpenseById() {
        DataWrapperORM<Expense> orm = new PostgresORM<>(Expense.class);
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
    @Order(6)
    void getAllExpenses() {
        DataWrapperORM<Expense> orm = new PostgresORM<>(Expense.class);
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
    @Order(7)
    void updateEmployee() {
        DataWrapperORM<Employee> orm = new PostgresORM<>(Employee.class);
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
    @Order(8)
    void updateExpense() {
        DataWrapperORM<Expense> orm = new PostgresORM<>(Expense.class);
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
    @Order(9)
    void deleteEmployee() {
        DataWrapperORM<Employee> orm = new PostgresORM<>(Employee.class);
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
    @Order(10)
    void deleteExpense() {
        DataWrapperORM<Expense> orm = new PostgresORM<>(Expense.class);
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
