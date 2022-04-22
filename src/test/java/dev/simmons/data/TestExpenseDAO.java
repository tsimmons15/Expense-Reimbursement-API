package dev.simmons.data;

import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.NoSuchExpenseException;
import org.junit.jupiter.api.*;

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
        Assertions.assertThrows(NoSuchExpenseException.class, () -> {
            expDao.getExpenseById(expense.getId());
        }, "Issue with deleteExpense in teardown: deleted test expense still found.");
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
        List<Expense> expenses = expDao.getExpensesByStatus(Expense.Status.PENDING);
        Assertions.assertNotNull(expenses);
        Assertions.assertNotEquals(0, expenses.size(), "Issue with the getExpensesByStatus method in test 4: expected list length > 0");

        for (Expense e : expenses) {
            Assertions.assertEquals(Expense.Status.PENDING, e.getStatus());
        }
    }

    @Test
    @Order(4)
    public void getAllExpensesByEmployee() {
        EmployeeDAO empDao = new PostgresEmployeeDAO();
        Employee employee = empDao.getEmployeeById(1);

        expense.setIssuer(employee.getId());
        expense = expDao.replaceExpense(expense);
        Assertions.assertNotNull(expense, "Issue with the replaceExpense method in test 5: expected not null.");

        List<Expense> expenses = expDao.getAllEmployeeExpenses(employee.getId());
        Assertions.assertNotNull(expenses);
        Assertions.assertNotEquals(0, expenses.size(), "Issue with the getAllEmployeeExpenses method in test 5: expected list length > 0");

        for(Expense e : expenses) {
            Assertions.assertEquals(employee.getId(), e.getIssuer(), "Issue with test 5, the queries where clause: expenses from employees other than the requested one returned.");
        }

        expense.setIssuer(0);
        expense = expDao.replaceExpense(expense);
        Assertions.assertNotNull(expense);
    }
}
