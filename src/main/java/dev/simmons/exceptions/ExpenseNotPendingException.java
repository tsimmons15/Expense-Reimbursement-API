package dev.simmons.exceptions;

public class ExpenseNotPendingException extends RuntimeException {
    public ExpenseNotPendingException(int id) {
        super("Attempt to edit or delete expense(" + id + ") that is not pending.");
    }
}
