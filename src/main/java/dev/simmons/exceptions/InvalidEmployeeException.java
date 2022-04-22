package dev.simmons.exceptions;

public class InvalidEmployeeException extends RuntimeException{
    public InvalidEmployeeException() {
        super("Invalid employee name. Must provide first and last name.");
    }
}
