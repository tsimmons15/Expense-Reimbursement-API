package dev.simmons.exceptions;

public class EmployeeExpenseNotPendingException extends RuntimeException {
    public EmployeeExpenseNotPendingException() {
        super("Unable to delete employee because they have an approved or denied expense request.");
    }
}
