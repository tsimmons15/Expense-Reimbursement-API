package dev.simmons.service;

import dev.simmons.data.EmployeeDAO;
import dev.simmons.data.ExpenseDAO;
import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.*;
import dev.simmons.utilities.logging.Logger;

import java.util.List;

public class ExpensesServiceImpl implements ExpensesService{
    private EmployeeDAO empDao;
    private ExpenseDAO expDao;

    public ExpensesServiceImpl(EmployeeDAO empDao, ExpenseDAO expDao) {
        this.empDao = empDao;
        this.expDao = expDao;
    }

    @Override
    public Expense createExpense(Expense expense) {
        if (expense.getStatus() == null) {
            throw new InvalidExpenseStatusException("Unable to parse the status passed in. Check for typos.");
        }
        if (expense.getStatus() != Expense.Status.PENDING && expense.getIssuer() == 0) {
            // Invalid expense: won't be able to edit (can't edit a non-pending expense) but can't assign to an employee.
            throw new InvalidExpenseException("Unable to submit a non-pending expense not yet assigned an issuer.");
        }

        if (expense.getAmount() <= 0) {
            throw new NonpositiveExpenseException(expense.getAmount());
        }
        return expDao.createExpense(expense);
    }

    @Override
    public Employee createEmployee(Employee employee) {
        if (employee.getFirstName() == null || employee.getLastName() == null ||
                employee.getFirstName().equals("") || employee.getLastName().equals("")) {
            Logger.log(Logger.Level.WARNING, "Attempt to create an employee with invalid first/last name.");
            throw new InvalidEmployeeException("Invalid employee name. Must provide first and last name.");
        }
        return empDao.createEmployee(employee);
    }

    @Override
    public Expense getExpenseById(int id) {
        return expDao.getExpenseById(id);
    }

    @Override
    public Employee getEmployeeById(int id) {
        return empDao.getEmployeeById(id);
    }

    @Override
    public List<Expense> getAllExpenses() {
        return expDao.getAllExpenses();
    }

    @Override
    public List<Expense> getExpensesByStatus(Expense.Status status) {
        return expDao.getExpensesByStatus(status);
    }

    @Override
    public List<Expense> getExpensesByEmployee(int employeeId) {
        return expDao.getAllEmployeeExpenses(employeeId);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return empDao.getAllEmployees();
    }

    @Override
    public Employee replaceEmployee(Employee employee) {
        if (employee.getFirstName() == null || employee.getLastName() == null ||
            employee.getFirstName().equals("") || employee.getLastName().equals("")) {
            Logger.log(Logger.Level.WARNING, "Attempt to replace an employee with invalid first/last name.");
            throw new InvalidEmployeeException("Invalid employee name. Must provide first and last name.");
        }
        return empDao.replaceEmployee(employee);
    }

    @Override
    public Expense replaceExpense(Expense expense) {
        if (expense.getStatus() == null) {
            Logger.log(Logger.Level.WARNING, "Null/misspelled status was passed in to replace expense.");
            throw new InvalidExpenseStatusException("Unable to parse the status passed in. Check for typos.");
        }
        if (expense.getStatus() != Expense.Status.PENDING && expense.getIssuer() <= 0) {
            Logger.log(Logger.Level.WARNING, "Attempt to submit a non-pending expense with no issuer assigned.");
            throw new InvalidExpenseException("Unable to submit a non-pending expense not yet assigned an issuer.");
        }
        if (expense.getAmount() <= 0) {
            Logger.log(Logger.Level.WARNING, "Attempt to submit an expense with a negative amount.");
            throw new NonpositiveExpenseException(expense.getAmount());
        }
        return expDao.replaceExpense(expense);
    }

    @Override
    public boolean deleteEmployee(int id) {
        List<Expense> expenses = getExpensesByEmployee(id);

        if (expenses != null) {
            int nonpendingCount = expenses.stream().mapToInt(e -> (e.getStatus().equals(Expense.Status.PENDING)) ? 0 : 1).reduce(0, (i,j) -> i+j);
            if (nonpendingCount > 0) {
                Logger.log(Logger.Level.WARNING, "Attempt to delete employee (" + id + ") which has non-pending expense requests.");
                throw new EmployeeExpenseNotPendingException();
            }

            for (Expense e : expenses) {
                deleteExpense(e.getId());
            }
        }

        return empDao.deleteEmployee(id);
    }

    @Override
    public boolean deleteExpense(int id) {
        return expDao.deleteExpense(id);
    }
}
