package dev.simmons.data;

import dev.simmons.entities.Employee;
import dev.simmons.exceptions.NoSuchEmployeeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestEmployeeDAO {
    private static EmployeeDAO empDao;
    private static Employee employee;
    @Test
    @Order(1)
    void createAnEmployee() {
        empDao = new PostgresEmployeeDAO();
        Employee emp = new Employee();

        emp.setFirstName("Testing");
        emp.setLastName("Testing");
        Employee received = empDao.createEmployee(emp);
        Assertions.assertNotNull(received);
        Assertions.assertNotEquals(0, received.getId());
        employee = received;
    }

    @Test
    @Order(2)
    void getEmployee() {
        Employee emp = empDao.getEmployeeById(employee.getId());
        Assertions.assertEquals(employee, emp);
    }

    @Test
    @Order(3)
    void getAllEmployees() {
        List<Employee> emps = empDao.getAllEmployees();
        Assertions.assertNotEquals(0, emps.size());
    }

    @Test
    @Order(4)
    void replaceEmployee() {
        Employee newEmp = new Employee();
        newEmp.setId(employee.getId());
        newEmp.setFirstName("Different");
        newEmp.setLastName("Names");
        Employee received = empDao.replaceEmployee(newEmp);
        Assertions.assertNotNull(received);
        Assertions.assertNotEquals(received, employee);
    }

    @Test
    @Order(5)
    void deleteEmployee() {
        Assertions.assertTrue(empDao.deleteEmployee(employee.getId()));
        Assertions.assertThrows(NoSuchEmployeeException.class, () -> {
            empDao.getEmployeeById(employee.getId());
        });
    }
}
