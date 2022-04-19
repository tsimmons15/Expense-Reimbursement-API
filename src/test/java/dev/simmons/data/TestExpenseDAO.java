package dev.simmons.data;

import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.ExpenseNotPendingException;
import dev.simmons.exceptions.NonpositiveExpenseException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestExpenseDAO {
    private static ExpenseDAO expDao;
    private static Expense expense;


    @BeforeAll
    public static void setup() {
        expDao = new PostgresExpenseDAO();
        Expense exp = new Expense();
        exp.setAmount(100);
        exp.setDate(100);
        exp.setStatus(Expense.Status.PENDING);

        expense = expDao.createExpense(exp);
        Assertions.assertNotNull(expense, "Issue with the createExpense method in test setup: null expense was returned from create.");
    }

    @AfterAll
    public static void teardown() {
        Assertions.assertTrue(expDao.deleteExpense(expense.getId()), "Issue with deleteExpense in teardown: unable to delete test expense.");
        Assertions.assertNull(expDao.getExpenseById(expense.getId()), "Issue with deleteExpense in teardown: deleted test expense still found.");
    }

    @Test
    @Order(1)
    public void getExpenseById() {
        Expense received = expDao.getExpenseById(expense.getId());
        Assertions.assertNotNull(received, "Issue with the getExpenseById in test 2: expected not null.");
    }

    @Test
    @Order(2)
    public void getAllExpenses() {
        Assertions.assertNotEquals(0, expDao.getAllExpenses().size(), "Issue with the getAllExpenses method in test 3: expected length of > 0");
    }

    @Test
    @Order(3)
    public void getExpensesByStatus() {
        List<Expense> expenses = expDao.getExpensesByStatus(Expense.Status.APPROVED);
        Assertions.assertNotNull(expenses);
        Assertions.assertNotEquals(0, expenses.size(), "Issue with the getExpensesByStatus method in test 4: expected list length > 0");

        for (Expense e : expenses) {
            Assertions.assertEquals(Expense.Status.APPROVED, e.getStatus());
        }
    }

    @Test
    @Order(4)
    public void getAllExpensesByEmployee() {
        EmployeeDAO empDao = new PostgresEmployeeDAO();
        Employee employee = new Employee();
        employee.setFirstName("Testing");
        employee.setLastName("Testing");
        employee = empDao.createEmployee(employee);
        Assertions.assertNotNull(employee, "Issue with the createEmployee method in test 5: expected not null.");

        expense.setIssuer(employee.getId());
        expense = expDao.replaceExpense(expense);
        Assertions.assertNotNull(expense, "Issue with the replaceExpense method in test 5: expected not null.");

        List<Expense> expenses = expDao.getAllEmployeeExpenses(employee.getId());
        Assertions.assertNotNull(expenses);
        Assertions.assertNotEquals(0, expenses.size(), "Issue with the getAllEmployeeExpenses method in test 5: expected list length > 0");

        for(Expense e : expenses) {
            Assertions.assertEquals(employee.getId(), e.getIssuer(), "Issue with test 5, the queries where clause: expenses from employees other than the requested one returned.");
        }
    }

    @Test
    @Order(5)
    public void replaceApprovedOrDeniedFails() {
        final Expense approved = new Expense();
        final Expense denied = new Expense();
        approved.setDate(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        denied.setDate(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));

        approved.setAmount(10000);
        denied.setAmount(10000);

        approved.setStatus(Expense.Status.APPROVED);
        denied.setStatus(Expense.Status.DENIED);

        Expense received = expDao.createExpense(approved);
        Assertions.assertNotNull(received, "Issue with approved createExpense method in test 6: expected not null.");
        approved.setId(received.getId());
        received = expDao.createExpense(denied);
        Assertions.assertNotNull(received, "Issue with denied createExpense method in test 6: expected not null.");
        denied.setId(received.getId());

        Assertions.assertThrows(ExpenseNotPendingException.class, () -> {
            approved.setAmount(0);
            Assertions.assertNull(expDao.replaceExpense(approved));
        }, "Issue with the replaceExpense method in test 6: was able to edit an approved expense.");

        Assertions.assertThrows(ExpenseNotPendingException.class, () -> {
            denied.setAmount(0);
            Assertions.assertNull(expDao.replaceExpense(denied));
        }, "Issue with the replaceExpense method in test 6: was able to edit a denied expense.");

        Assertions.assertTrue(expDao.deleteExpense(approved.getId()), "Issue with the deleteExpense method in test 6: unable to delete approved expense.");
        Assertions.assertTrue(expDao.deleteExpense(denied.getId()), "Issue with the deleteExpense method in test 6: unable to delete approved expense.");
        Assertions.assertNull(expDao.getExpenseById(approved.getId()), "Issue with the getExpenseById method in test 6: deleted approved expense still found.");
        Assertions.assertNull(expDao.getExpenseById(denied.getId()), "Issue with the getExpenseById method in test 6: deleted denied expense still found.");
    }


    @Test
    public void negativeExpenseThrowsExceptionDuringInsert() {
        // NegativeExpenseException thrown if you try to create a negative expense
        // Negative expense is not inserted.
        int length = expDao.getAllExpenses().size();
        Assertions.assertThrows(NonpositiveExpenseException.class, () -> {
           Expense exp = new Expense();
           exp.setDate(100);
           exp.setAmount(-1);
           exp.setStatus(Expense.Status.PENDING);
           exp.setIssuer(0);
           Assertions.assertNull(expDao.createExpense(exp));
        }, "Issue with negativeExpenseThrown test: expected thrown exception not found during createExpense call.");
        Assertions.assertEquals(length, expDao.getAllExpenses().size(), "Issue with negativeExpenseThrown test: negative expense was added to database during createExpense call.");
    }

    @Test
    public void negativeExpenseThrowsExceptionDuringReplace() {
        // NegativeExpenseException thrown if you try to create a negative expense
        // Negative expense is not inserted.
        Expense exp = new Expense();
        exp.setDate(100);
        exp.setAmount(100);
        exp.setStatus(Expense.Status.PENDING);
        exp.setIssuer(0);
        Expense received = expDao.createExpense(exp);
        Assertions.assertNotNull(received);
        received.setAmount(-100);
        Assertions.assertThrows(NonpositiveExpenseException.class, () -> {
            Assertions.assertNull(expDao.replaceExpense(received));
        }, "Issue with negativeExpenseThrown test: expected thrown exception not found during replaceExpense call.");
        Assertions.assertEquals(exp.getAmount(), expDao.getExpenseById(exp.getId()).getAmount(), "Issue with negativeExpenseThrown test: expense with negative amount allowed to overwrite existing expense.");
        Assertions.assertTrue(expDao.deleteExpense(exp.getId()));
    }
}
