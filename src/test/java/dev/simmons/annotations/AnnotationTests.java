package dev.simmons.annotations;

import dev.simmons.annotation.DBEntity;
import dev.simmons.annotation.DbField;
import dev.simmons.annotation.PrimaryKey;
import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

public class AnnotationTests {
    @Test
    public void getAnnotations() {
        Class emp = Employee.class;
        Class exp = Expense.class;

        System.out.println(Arrays.toString(emp.getAnnotations()));

        Field[] empFields = emp.getDeclaredFields();
        System.out.println(Arrays.toString(empFields));
        Arrays.asList(empFields).forEach(f -> {
            System.out.println("Field: " + f.getName());
            System.out.println(Arrays.toString(f.getAnnotationsByType(PrimaryKey.class)));
            System.out.println(Arrays.toString(f.getDeclaredAnnotations()));
            System.out.println(Arrays.toString(f.getAnnotations()));
            System.out.println(f.getAnnotatedType());
        });
        Field[] expFields = exp.getDeclaredFields();
        System.out.println(Arrays.toString(expFields));
        Arrays.asList(expFields).forEach(f -> {
            System.out.println("Field: " + f.getName());
            System.out.println(Arrays.toString(f.getDeclaredAnnotations()));
            System.out.println(Arrays.toString(f.getAnnotations()));
            System.out.println(f.getAnnotatedType());
        });
    }
}
