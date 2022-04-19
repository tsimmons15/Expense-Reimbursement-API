package dev.simmons.exceptions;

public class InvalidEmployeeException extends RuntimeException{
    public InvalidEmployeeException(String message) {
        super(message);
    }
}
