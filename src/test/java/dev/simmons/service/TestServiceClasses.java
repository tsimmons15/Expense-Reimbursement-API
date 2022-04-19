package dev.simmons.service;

import dev.simmons.data.PostgresEmployeeDAO;
import dev.simmons.data.PostgresExpenseDAO;
import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.ExpenseNotPendingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TestServiceClasses {
    private static ExpensesService service;
    private static List<Employee> employees;
    private static List<Expense> expenses;
    private static Random rand;

    @BeforeAll
    public static void setup() {
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
             switch (rand.nextInt(Expense.Status.values().length)) {
                 case 0:
                     exp.setStatus(Expense.Status.PENDING);
                     break;
                 case 1:
                     exp.setStatus(Expense.Status.APPROVED);
                     break;
                 case 2:
                     exp.setStatus(Expense.Status.DENIED);
                     break;
             }
             exp = service.createExpense(exp);
             Assertions.assertNotNull(exp);
             Assertions.assertNotEquals(0, exp.getId());
             expenses.add(exp);
        }
        int id = employees.get(rand.nextInt(employees.size())).getId();
        Expense exp = new Expense();
        exp.setDate(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        exp.setAmount((long)(rand.nextDouble() * 10000));
        exp.setIssuer(id);
        exp.setStatus(Expense.Status.DENIED);
        exp = service.createExpense(exp);
        Assertions.assertNotNull(exp);
        Assertions.assertNotEquals(0, exp.getId());
        expenses.add(exp);

        id = employees.get(rand.nextInt(employees.size())).getId();
        exp = new Expense();
        exp.setDate(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        exp.setAmount((long)(rand.nextDouble() * 10000));
        exp.setIssuer(id);
        exp.setStatus(Expense.Status.APPROVED);
        exp = service.createExpense(exp);
        Assertions.assertNotNull(exp);
        Assertions.assertNotEquals(0, exp.getId());
        expenses.add(exp);
    }

    @AfterAll
    public static void teardown() {
        expenses.forEach((exp) -> {
            Assertions.assertTrue(service.deleteExpense(exp.getId()));
            Assertions.assertNull(service.getExpenseById(exp.getId()));
        });

        employees.forEach((emp) -> {
            Assertions.assertTrue(service.deleteEmployee(emp.getId()));
            Assertions.assertNull(service.getEmployeeById(emp.getId()));
        });
    }

    @Test
    public void getExpenseById() {
        int index = rand.nextInt(expenses.size());
        Expense e = expenses.get(index);
        Assertions.assertEquals(e, service.getExpenseById(e.getId()));
    }

    @Test
    public void getEmployeeById() {
        int index = rand.nextInt(employees.size());
        Employee e = employees.get(index);
        Assertions.assertEquals(e, service.getEmployeeById(e.getId()));
    }

    @Test
    public void getAllExpenses() {
        List<Expense> received = service.getAllExpenses();
        Assertions.assertNotNull(received);
        Assertions.assertTrue(received.size() >= expenses.size());
    }

    @Test
    public void getExpensesByStatus() {
        List<Expense> pending = service.getExpensesByStatus(Expense.Status.PENDING);
        List<Expense> testPending = expenses.stream().filter(exp -> exp.getStatus().equals(Expense.Status.PENDING)).collect(Collectors.toList());
        Assertions.assertNotNull(pending);
        Assertions.assertTrue(pending.size() >= testPending.size());
        pending.forEach(exp -> Assertions.assertEquals(Expense.Status.PENDING, exp.getStatus()));
    }

    @Test
    public void getExpensesByEmployee() {
        int index = rand.nextInt(employees.size());
        Employee e = employees.get(index);
        List<Expense> expenseList = service.getExpensesByEmployee(e.getId());
        Assertions.assertNotNull(expenseList);
        expenseList.forEach(exp -> Assertions.assertEquals(e.getId(), exp.getIssuer()));
    }

    @Test
    public void getAllEmployees() {
        List<Employee> employeeList = service.getAllEmployees();
        Assertions.assertNotNull(employeeList);
        Assertions.assertTrue(employeeList.size() >= employees.size());
    }

    @Test
    public void replaceEmployee() {
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
    public void replaceExpense() {
        List<Expense> pending = new ArrayList<>();
        List<Expense> approved = new ArrayList<>();
        List<Expense> denied = new ArrayList<>();

        expenses.forEach(exp -> {
            switch (exp.getStatus().name()) {
                case "PENDING":
                    pending.add(exp);
                    break;
                case "APPROVED":
                    approved.add(exp);
                    break;
                case "DENIED":
                    denied.add(exp);
                    break;
            }
        });

        int index = rand.nextInt(pending.size());
        Expense pendingExpense = pending.get(index);

        index = rand.nextInt(approved.size());
        Expense approvedExpense = approved.get(index);

        index = rand.nextInt(denied.size());
        Expense deniedExpense = denied.get(index);

        pendingExpense.setStatus(Expense.Status.DENIED);
        Expense received = service.replaceExpense(pendingExpense);
        Assertions.assertNotNull(received);
        Assertions.assertEquals(Expense.Status.DENIED, received.getStatus());

        Assertions.assertThrows(ExpenseNotPendingException.class, () -> {
            approvedExpense.setAmount(0);
            Assertions.assertNull(service.replaceExpense(approvedExpense));
        }, "Replacing approved expense did not throw an exception.");

        Assertions.assertThrows(ExpenseNotPendingException.class, () -> {
            deniedExpense.setAmount(0);
            Assertions.assertNull(service.replaceExpense(deniedExpense));
        }, "Replacing denied expense did not throw an exception.");
    }
}
