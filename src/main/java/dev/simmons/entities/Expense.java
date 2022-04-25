package dev.simmons.entities;

import dev.simmons.annotation.DBEntity;
import dev.simmons.annotation.DbField;
import dev.simmons.annotation.ForeignKey;
import dev.simmons.annotation.PrimaryKey;
import dev.simmons.data.PostgresORM;

import java.text.DecimalFormat;
import java.util.Objects;

@DBEntity("Expense")
public class Expense implements Comparable<Expense>{
    @PrimaryKey
    @DbField(name = "expense_id", type = PostgresORM.DataTypes.INT)
    private int id;
    @DbField(name = "amount", type = PostgresORM.DataTypes.BIG_INT)
    private long amount;
    @DbField(name = "status", type = PostgresORM.DataTypes.STRING)
    private Status status;
    @DbField(name = "date", type = PostgresORM.DataTypes.BIG_INT)
    private long date;
    @ForeignKey(references = "Employee", column = "employee_id")
    @DbField(name = "issuer", type = PostgresORM.DataTypes.INT)
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
