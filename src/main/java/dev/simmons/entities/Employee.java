package dev.simmons.entities;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Employee implements Comparable<Employee>{
    private int id;
    private String firstName;
    private String lastName;

    public Employee() {
        firstName = "";
        lastName = "";
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
