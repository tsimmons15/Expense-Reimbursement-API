package dev.simmons.exceptions;

public class InvalidExpenseStatusException extends RuntimeException {
    public InvalidExpenseStatusException(String message) {
        super(message);
    }
}
