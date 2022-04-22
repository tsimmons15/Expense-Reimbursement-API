package dev.simmons.entities;

import java.text.DecimalFormat;
import java.util.Objects;

public class Expense implements Comparable<Expense>{
    private int id;
    private long amount;
    private Status status;
    private long date;
    private int issuer;

    public Expense() {
        this.status = Status.PENDING;
    }

    public Expense(Expense copy) {
        this.status = copy.getStatus();
        this.id = copy.getId();
        this.amount = copy.getAmount();
        this.issuer = copy.getIssuer();
        this.date = copy.getDate();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getIssuer() {
        return issuer;
    }

    public void setIssuer(int issuer) {
        this.issuer = issuer;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("0.00");
        return "Expense(" + getId() + ") for $" + df.format(getAmount()/100.0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Expense)) {return false;}
        return this.hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.amount, this.status, this.date, this.issuer);
    }

    @Override
    public int compareTo(Expense other) {
        return Integer.compare(this.id, other.getId());
    }

    public enum Status {
      PENDING, APPROVED, DENIED
    }
}
