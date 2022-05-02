package dev.simmons.exceptions;

public class NoSuchExpenseException extends NoSuchEntityException{
    public NoSuchExpenseException(int id) {
        super("Unable to find expense matching (id: " + id + "). " +
                "Make sure an expense with that id exists.");
    }
}
