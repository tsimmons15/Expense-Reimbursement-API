package dev.simmons.service;

import dev.simmons.data.EmployeeDAO;
import dev.simmons.data.ExpenseDAO;
import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;

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
        return expDao.createExpense(expense);
    }

    @Override
    public Employee createEmployee(Employee employee) {
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
        return empDao.replaceEmployee(employee);
    }

    @Override
    public Expense replaceExpense(Expense expense) {
        return expDao.replaceExpense(expense);
    }

    @Override
    public boolean deleteEmployee(int id) {
        List<Expense> expenses = getExpensesByEmployee(id);
        if (expenses != null) {
            for(Expense e: expenses) {
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
