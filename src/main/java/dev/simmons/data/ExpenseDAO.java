package dev.simmons.data;

import dev.simmons.entities.Expense;
import java.util.List;

public interface ExpenseDAO {
    Expense createExpense(Expense expense);

    Expense getExpenseById(int id);

    List<Expense> getAllExpenses();
    List<Expense> getExpensesByStatus(Expense.Status status);
    List<Expense> getAllEmployeeExpenses(int employeeId);

    Expense replaceExpense(Expense expense);

    boolean deleteExpense(int id);
}
