package dev.simmons.data;

import dev.simmons.entities.Employee;
import dev.simmons.exceptions.NoSuchEmployeeException;
import dev.simmons.utilities.connection.PostgresConnection;
import dev.simmons.utilities.logging.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresEmployeeDAO implements EmployeeDAO{
    private static final String emp_id = "employee_id";
    private static final String emp_first = "first_name";
    private static final String emp_last = "last_name";
    @Override
    public Employee createEmployee(Employee employee) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "insert into employee (first_name, last_name) values (?,?);";
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Failed to create employee(" + employee + ").");
                return null;
            }
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            employee.setId(id);

            return employee;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public Employee getEmployeeById(int id) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "select * from employee where employee_id = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);

            ResultSet rs = statement.executeQuery();
            rs.next();
            Employee employee = new Employee();
            employee.setId(rs.getInt(emp_id));
            employee.setFirstName(rs.getString(emp_first));
            employee.setLastName(rs.getString(emp_last));

            return employee;
        } catch (SQLException se) {
            if (se.getSQLState().equals("24000")) {
                Logger.log(Logger.Level.WARNING, "Search for non-existent employee with id: " + id);
                throw new NoSuchEmployeeException(id);
            }
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "select * from employee;";
            PreparedStatement statement = conn.prepareStatement(sql);

            ResultSet rs = statement.executeQuery();
            Employee emp;
            while (rs.next()) {
                emp = new Employee();
                emp.setId(rs.getInt(emp_id));
                emp.setFirstName(rs.getString(emp_first));
                emp.setLastName(rs.getString(emp_last));
                employees.add(emp);
            }
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }

        return employees;
    }

    @Override
    public Employee replaceEmployee(Employee employee) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "update employee set first_name = ?, last_name = ? where employee_id = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setInt(3, employee.getId());

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "No employee (" + employee + ") found to update.");
                throw new NoSuchEmployeeException(employee.getId());
            }

            return employee;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public boolean deleteEmployee(int id) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "delete from employee where employee_id = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Employee (" + id + ") not found to delete.");
                throw new NoSuchEmployeeException(id);
            }

            return true;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return false;
    }
}
