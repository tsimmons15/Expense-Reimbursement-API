package dev.simmons.service;

import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;

import java.util.List;

public interface ExpensesService {
    Expense createExpense(Expense expense);
    Employee createEmployee(Employee employee);

    Expense getExpenseById(int id);
    Employee getEmployeeById(int id);
    List<Expense> getAllExpenses();
    List<Expense> getExpensesByStatus(Expense.Status status);
    List<Expense> getExpensesByEmployee(int employeeId);
    List<Employee> getAllEmployees();

    Employee replaceEmployee(Employee employee);
    Expense replaceExpense(Expense expense);

    boolean deleteEmployee(int id);
    boolean deleteExpense(int id);

}
