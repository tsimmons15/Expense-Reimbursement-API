package dev.simmons.random;

import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class RandomTests {
    @Test
    void methodReflection() throws NoSuchMethodException {
        Expense exp = new Expense();
        exp.setAmount(10000);
        exp.setDate(1234567);
        exp.setStatus(Expense.Status.PENDING);
        exp.setIssuer(1234);
        exp.setId(12);
        Method[] methods = Expense.class.getDeclaredMethods();
        for(Method method : methods) {
            System.out.println(method.getName());
        }

        try {
            Field[] fields = Expense.class.getDeclaredFields();
            for(Field field : fields) {
                System.out.println("Field Name: " + field.getName());
                System.out.println("Field Type: " + field.getGenericType());
                String sentenceCased = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method getter = Expense.class.getDeclaredMethod("get" + sentenceCased);
                getter.invoke(exp);
            }
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    void casingTest() {
        RandomClass rc = new RandomClass();
        // Difference between using rc.getClass() vs. RandomClass.class is the this$0 since it's a declared *instance* and not just a class...
        List<Field> fields = Arrays.asList(rc.getClass().getDeclaredFields()).stream().filter(f -> f.getName().contains("this$")).collect(Collectors.toList());
        for (Field field : fields) {
            System.out.println(field.getName());
            System.out.println(field.getType());
            System.out.println(field.getGenericType());
        }
    }


    private class RandomClass{
        private int mixedCase;
        private int PascalCase;
        private int snake_case;

        public RandomClass() {

        }
    }
}
