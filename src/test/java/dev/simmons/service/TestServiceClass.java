package dev.simmons.service;

import dev.simmons.data.PostgresEmployeeDAO;
import dev.simmons.data.PostgresExpenseDAO;
import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.*;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

 class TestServiceClass {
    private static ExpensesService service;
    private static List<Employee> employees;
    private static List<Expense> expenses;
    private static Random rand;

    private static final int employeeId = 1;
    private static final int approvedId = 1;
    private static final int deniedId = 2;

    @BeforeAll
     static void setup() {
        service = new ExpensesServiceImpl(new PostgresEmployeeDAO(), new PostgresExpenseDAO());
        employees = new ArrayList<>();
        expenses = new ArrayList<>();
        rand = new Random();

        for (int i = 0; i < 5; i++) {
            Employee emp = new Employee();
            emp.setFirstName("Testing" + i);
            emp.setLastName("Testing" + i);
            emp = service.createEmployee(emp);
            Assertions.assertNotNull(emp);
            Assertions.assertNotEquals(0, emp.getId());
            employees.add(emp);
        }

        for (int i = 0; i < 10; i++) {
            int id = employees.get(rand.nextInt(employees.size())).getId();
             Expense exp = new Expense();
             exp.setDate(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
             exp.setAmount((long)(rand.nextDouble() * 10000));
             exp.setIssuer(id);
             exp.setStatus(Expense.Status.PENDING);
             exp = service.createExpense(exp);
             Assertions.assertNotNull(exp);
             Assertions.assertNotEquals(0, exp.getId());
             expenses.add(exp);
        }
    }

    @AfterAll
     static void teardown() {
        expenses.forEach((exp) -> {
            Assertions.assertTrue(service.deleteExpense(exp.getId()));
            Assertions.assertThrows(NoSuchExpenseException.class, () -> service.getExpenseById(exp.getId()));
        });

        employees.forEach((emp) -> {
            Assertions.assertTrue(service.deleteEmployee(emp.getId()));
            Assertions.assertThrows(NoSuchEmployeeException.class, () -> service.getEmployeeById(emp.getId()));
        });
    }

    @Test
     void getExpenseById() {
        int index = rand.nextInt(expenses.size());
        Expense e = expenses.get(index);
        Assertions.assertEquals(e, service.getExpenseById(e.getId()));
    }

    @Test
     void getEmployeeById() {
        int index = rand.nextInt(employees.size());
        Employee e = employees.get(index);
        Assertions.assertEquals(e, service.getEmployeeById(e.getId()));
    }

    @Test
     void getAllExpenses() {
        List<Expense> received = service.getAllExpenses();
        Assertions.assertNotNull(received);
        Assertions.assertTrue(received.size() >= expenses.size());
    }

    @Test
     void getExpensesByStatus() {
        List<Expense> pending = service.getExpensesByStatus(Expense.Status.PENDING);
        List<Expense> testPending = expenses.stream().filter(exp -> exp.getStatus().equals(Expense.Status.PENDING)).collect(Collectors.toList());
        Assertions.assertNotNull(pending);
        Assertions.assertTrue(pending.size() >= testPending.size());
        pending.forEach(exp -> Assertions.assertEquals(Expense.Status.PENDING, exp.getStatus()));
    }

    @Test
     void getExpensesByEmployee() {
        int index = rand.nextInt(employees.size());
        Employee e = employees.get(index);
        List<Expense> expenseList = service.getExpensesByEmployee(e.getId());
        Assertions.assertNotNull(expenseList);
        expenseList.forEach(exp -> Assertions.assertEquals(e.getId(), exp.getIssuer()));
    }

    @Test
     void getAllEmployees() {
        List<Employee> employeeList = service.getAllEmployees();
        Assertions.assertNotNull(employeeList);
        Assertions.assertTrue(employeeList.size() >= employees.size());
    }

    @Test
     void replaceEmployee() {
        int index = rand.nextInt(employees.size());
        Employee e = employees.get(index);
        e.setFirstName("Changed");
        e.setLastName("TestName");
        e = service.replaceEmployee(e);
        Assertions.assertNotNull(e);
        Employee received = service.getEmployeeById(e.getId());
        Assertions.assertNotNull(received);
        Assertions.assertEquals(e.getFirstName(), received.getFirstName());
        Assertions.assertEquals(e.getLastName(), received.getLastName());
        employees.set(index, e);
    }

    @Test
     void replaceExpense() {
        int index = rand.nextInt(expenses.size());
        Expense pendingExpense = new Expense(expenses.get(index));

        pendingExpense.setAmount(pendingExpense.getAmount()*2);
        Expense received = service.replaceExpense(pendingExpense);
        Assertions.assertNotNull(received);
        Assertions.assertEquals(expenses.get(index).getAmount()*2, received.getAmount());

        expenses.set(index, received);

        Expense approvedExpense = service.getExpenseById(approvedId);
        approvedExpense.setAmount(550);

        Assertions.assertThrows(ExpenseNotPendingException.class, () -> {
            Assertions.assertNull(service.replaceExpense(approvedExpense));
        }, "Replacing approved expense did not throw an exception.");

        Expense deniedExpense = service.getExpenseById(deniedId);
        deniedExpense.setAmount(550);
        Assertions.assertThrows(ExpenseNotPendingException.class, () -> {
            Assertions.assertNull(service.replaceExpense(deniedExpense));
        }, "Replacing denied expense did not throw an exception.");
    }

    @Test
     void negativeExpenseThrowsExceptionDuringInsert() {
        // NegativeExpenseException thrown if you try to create a negative expense
        // Negative expense is not inserted.
        int length = service.getAllExpenses().size();
        Expense exp = new Expense();
        exp.setDate(100);
        exp.setAmount(-1);
        exp.setStatus(Expense.Status.PENDING);
        exp.setIssuer(1);
        Assertions.assertThrows(NonpositiveExpenseException.class, () -> {
            Assertions.assertNull(service.createExpense(exp));
        }, "Issue with negativeExpenseThrown test: expected thrown exception not found during createExpense call.");
        Assertions.assertEquals(length, service.getAllExpenses().size(), "Issue with negativeExpenseThrown test: negative expense was added to database during createExpense call.");
    }

    @Test
     void negativeExpenseThrowsExceptionDuringReplace() {
        // NegativeExpenseException thrown if you try to create a negative expense
        // Negative expense is not inserted.
        int index = rand.nextInt(expenses.size());
        Expense exp = expenses.get(index);

        Expense received = new Expense();
        received.setId(exp.getId());
        received.setDate(exp.getDate());
        received.setStatus(exp.getStatus());
        received.setIssuer(exp.getIssuer());
        received.setAmount(-100);

        Assertions.assertThrows(NonpositiveExpenseException.class, () -> {
            Assertions.assertNull(service.replaceExpense(received));
        }, "Issue with negativeExpenseThrown test: expected thrown exception not found during replaceExpense call.");
        Assertions.assertEquals(exp.getAmount(), service.getExpenseById(exp.getId()).getAmount(), "Issue with negativeExpenseThrown test: expense with negative amount allowed to overwrite existing expense.");
    }

    @Test
     public void expenseThrowsExceptionInsertingWithNoIssuer() {
        int index = rand.nextInt(expenses.size());
        Expense exp = expenses.get(index);

        Expense received = new Expense();
        received.setAmount(exp.getAmount());
        received.setId(exp.getId());
        received.setDate(exp.getDate());
        received.setStatus(exp.getStatus());
        received.setIssuer(0);

        Assertions.assertThrows(InvalidExpenseException.class, () -> {
            Assertions.assertNull(service.createExpense(received));
        }, "Issue with invalidExpenseThrown test: " +
                "expected thrown exception not found during createExpense call.");
        Assertions.assertEquals(exp.getIssuer(), service.getExpenseById(exp.getId()).getIssuer(),
                "Issue with invalidExpenseThrown test: expense with no issuer allowed to be inserted.");
    }

     @Test
     public void expenseThrowsExceptionReplacingWithNoIssuer() {
         int index = rand.nextInt(expenses.size());
         Expense exp = expenses.get(index);

         Expense received = new Expense();
         received.setAmount(exp.getAmount());
         received.setId(exp.getId());
         received.setDate(exp.getDate());
         received.setStatus(exp.getStatus());
         received.setIssuer(0);

         Assertions.assertThrows(InvalidExpenseException.class, () -> {
             Assertions.assertNull(service.replaceExpense(received));
         }, "Issue with noIssuerTest test: expected thrown exception not found during replaceExpense call.");
         Assertions.assertEquals(exp.getIssuer(), service.getExpenseById(exp.getId()).getIssuer(),
                 "Issue with InvalidExpenseException test: " +
                         "expense with no issuer allowed to overwrite existing expense.");
     }
}
