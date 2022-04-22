package dev.simmons.exceptions;

public class NoSuchEmployeeException extends RuntimeException{
    public NoSuchEmployeeException(int id) {
        super("Unable to find employee matching (id: " + id + "). " +
                "Make sure an employee with that id exists.");
    }
}
