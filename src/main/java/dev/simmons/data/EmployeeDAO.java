package dev.simmons.data;

import dev.simmons.entities.Employee;
import java.util.List;

public interface EmployeeDAO {
    Employee createEmployee(Employee employee); // creates an employee

    Employee getEmployeeById(int id); // Get an employee by ID

    List<Employee> getAllEmployees();// get all instances of the employee

    Employee replaceEmployee(Employee employee);// update an instance
    
    boolean deleteEmployee(int id);
}
