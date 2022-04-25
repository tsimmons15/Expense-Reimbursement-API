package dev.simmons.entities;

import dev.simmons.annotation.DBEntity;
import dev.simmons.annotation.DbField;
import dev.simmons.annotation.PrimaryKey;
import dev.simmons.data.PostgresORM;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@DBEntity("Employee")
public class Employee implements Comparable<Employee>{
    @PrimaryKey
    @DbField(name = "employee_id", type = PostgresORM.DataTypes.INT)
    private int id;
    @DbField(name = "first_name", type = PostgresORM.DataTypes.STRING)
    private String firstName;
    @DbField(name = "last_name", type = PostgresORM.DataTypes.STRING)
    private String lastName;

    public Employee() {
        firstName = "";
        lastName = "";
    }

    public Employee(Employee copy) {
        firstName = String.copyValueOf(copy.getFirstName().toCharArray());
        lastName = String.copyValueOf(copy.getLastName().toCharArray());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Employee)) { return false; }
        return this.hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.firstName, this.lastName);
    }

    @Override
    public int compareTo(@NotNull Employee o) {
        return Integer.compare(this.id, o.getId());
    }

    @Override
    public String toString() {
        if (firstName.equals("") || lastName.equals("")) {
            return "#" + id;
        }
        return firstName + " " + lastName + ", #" + id;
    }
}
