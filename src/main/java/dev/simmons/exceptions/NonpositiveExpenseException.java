package dev.simmons.exceptions;

public class NonpositiveExpenseException extends RuntimeException {
    public NonpositiveExpenseException(double amount) {
        super("Invalid expense amount: " + amount + ". Unable to submit non-positive expense amounts.");
    }
}
