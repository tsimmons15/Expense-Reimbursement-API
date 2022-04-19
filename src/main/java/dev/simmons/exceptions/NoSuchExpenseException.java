package dev.simmons.exceptions;

public class NoSuchExpenseException extends RuntimeException{
    public NoSuchExpenseException(String message) {
        super(message);
    }
}
