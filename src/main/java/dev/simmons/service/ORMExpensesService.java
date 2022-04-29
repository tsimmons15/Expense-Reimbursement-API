package dev.simmons.service;

import dev.simmons.data.PostgresORM;
import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.*;
import dev.simmons.utilities.logging.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ORMExpensesService implements ExpensesService{
    protected PostgresORM<Employee> empORM;
    protected PostgresORM<Expense> expORM;

    public ORMExpensesService(PostgresORM<Employee> emp, PostgresORM<Expense> exp) {
        empORM = emp;
        expORM = exp;
    }

    @Override
    public Expense createExpense(Expense expense) {
        if (expense.getStatus() == null) {
            throw new InvalidExpenseStatusException("Unable to parse the status passed in. Check for typos.");
        }
        if (expense.getStatus() != Expense.Status.PENDING) {
            throw new InvalidExpenseException("Unable to submit a non-pending expense. Submitted expenses must be approved or denied separately.");
        }
        if (expense.getIssuer() <= 0) {
            throw new InvalidExpenseException("Unable to submit a expense with no associated employee. Please identify the owning employee.");
        }
        if (expense.getAmount() <= 0) {
            throw new NonpositiveExpenseException(expense.getAmount());
        }

        Expense received = null;
        try {
            received = expORM.createEntity(expense);
        } catch (SQLException se) {
            if (se.getSQLState().equals("23503")) {
                Logger.log(Logger.Level.WARNING, "Attempt to create expense for a non-existent employee with id " + expense.getIssuer());
                throw new NoSuchEmployeeException(expense.getIssuer());
            }
            Logger.log(Logger.Level.ERROR, se);
        }
        return received;
    }

    @Override
    public Employee createEmployee(Employee employee) {
        if (employee.getFirstName() == null || employee.getLastName() == null ||
                employee.getFirstName().equals("") || employee.getLastName().equals("")) {
            Logger.log(Logger.Level.WARNING, "Attempt to create an employee with invalid first/last name.");
            throw new InvalidEmployeeException();
        }

        Employee received = null;
        try {
            received = empORM.createEntity(employee);
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return received;
    }

    @Override
    public Expense getExpenseById(int id) {
        Expense received = null;
        try {
            received = expORM.getEntityById(id);
        } catch (SQLException se) {
            if (se.getSQLState().equals("24000")) {
                Logger.log(Logger.Level.WARNING, "Search for non-existent expense with id " + id);
                throw new NoSuchExpenseException(id);
            }

            Logger.log(Logger.Level.ERROR, se);
        }
        return received;
    }

    @Override
    public Employee getEmployeeById(int id) {
        Employee received = null;

        try {
            received = empORM.getEntityById(id);
         } catch (SQLException se) {
            if (se.getSQLState().equals("24000")) {
                Logger.log(Logger.Level.WARNING, "Search for non-existent employee with id: " + id);
                throw new NoSuchEmployeeException(id);
            }
            Logger.log(Logger.Level.ERROR, se);
        }
        return received;
    }

    @Override
    public List<Expense> getAllExpenses() {
        List<Expense> expenses;
        try {
            expenses = expORM.getAllEntities();
        } catch (SQLException se) {
            expenses = new ArrayList<>();
        }
        return expenses;
    }

    @Override
    public List<Expense> getExpensesByStatus(Expense.Status status) {
        List<Expense> expenses;

        try {
            expenses = expORM.getAllEntities();
            if (expenses == null) {
                return new ArrayList<>();
            }

            expenses = expenses.stream().
                    filter(e -> e.getStatus().equals(status)).
                    collect(Collectors.toList());
        } catch (SQLException se) {
            expenses = new ArrayList<>();
        }
        return expenses;
    }

    @Override
    public List<Expense> getExpensesByEmployee(int employeeId) {
        List<Expense> expenses;

        try {
            expenses = expORM.getAllEntities();
            if (expenses == null) {
                return new ArrayList<>();
            }

            expenses = expenses.stream().
                    filter(e -> e.getIssuer() == employeeId).
                    collect(Collectors.toList());
        } catch (SQLException se) {
            expenses = new ArrayList<>();
        }
        return expenses;
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees;

        try {
            employees = empORM.getAllEntities();
        } catch (SQLException se) {
            employees = new ArrayList<>();
        }

        return employees;
    }

    @Override
    public Employee replaceEmployee(Employee employee) {
        if (employee.getFirstName() == null || employee.getLastName() == null ||
                employee.getFirstName().equals("") || employee.getLastName().equals("")) {
            Logger.log(Logger.Level.WARNING, "Attempt to replace an employee with invalid first/last name.");
            throw new InvalidEmployeeException();
        }

        Employee emp = null;
        try {
            emp = empORM.replaceEntity(employee);
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }

        return emp;
    }

    @Override
    public Expense replaceExpense(Expense expense) {
        if (expense.getStatus() == null) {
            Logger.log(Logger.Level.WARNING, "Null/misspelled status was passed in to replace expense.");
            throw new InvalidExpenseStatusException("Unable to parse the status passed in. Check for typos.");
        }
        if (expense.getIssuer() <= 0) {
            Logger.log(Logger.Level.WARNING, "Attempt to submit an expense with no issuer assigned.");
            throw new InvalidExpenseException("Unable to submit an expense not yet assigned an issuer.");
        }
        if (expense.getAmount() <= 0) {
            Logger.log(Logger.Level.WARNING, "Attempt to submit an expense with a negative amount.");
            throw new NonpositiveExpenseException(expense.getAmount());
        }

        Expense exp = null;
        try {
            exp = expORM.replaceEntity(expense);
        } catch (SQLException se) {
            if (se.getSQLState().equals("23503")) {
                throw new NoSuchEmployeeException(expense.getIssuer());
            } else if (se.getSQLState().equals("P0001")) {
                Logger.log(Logger.Level.WARNING, "Attempt to replace contents of non-pending expense.");
                throw new ExpenseNotPendingException(expense.getId());
            }
        }

        return exp;
    }

    @Override
    public boolean deleteEmployee(int id) {
        try {
            List<Expense> expenses = getExpensesByEmployee(id);

            if (expenses != null) {
                int nonpendingCount = expenses.stream().mapToInt(e -> (e.getStatus().equals(Expense.Status.PENDING)) ? 0 : 1).reduce(0, (i,j) -> i+j);
                if (nonpendingCount > 0) {
                    Logger.log(Logger.Level.WARNING, "Attempt to delete employee matching (id: " + id + ") which has non-pending expense requests.");
                    throw new EmployeeExpenseNotPendingException();
                }

                for (Expense e : expenses) {
                    deleteExpense(e.getId());
                }
            }

            return empORM.deleteEntity(id);
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return false;
    }

    @Override
    public boolean deleteExpense(int id) {
        try {
            return expORM.deleteEntity(id);
        } catch (SQLException se) {
            if (se.getSQLState().equals("P0001")) {
                Logger.log(Logger.Level.WARNING, "Attempt to delete non-pending expense.");
                throw new ExpenseNotPendingException(id);
            }

            Logger.log(Logger.Level.ERROR, se);
        }

        return false;
    }
}
