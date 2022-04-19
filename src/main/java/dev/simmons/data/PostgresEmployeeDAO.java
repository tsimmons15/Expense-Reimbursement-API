package dev.simmons.data;

import dev.simmons.entities.Employee;
import dev.simmons.utilities.connection.PostgresConnection;
import dev.simmons.utilities.logging.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresEmployeeDAO implements EmployeeDAO{
    @Override
    public Employee createEmployee(Employee employee) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "insert into employee (first_name, last_name) values (?,?);";
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.ERROR, "Unable to insert employe (" + employee + ").");
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
            employee.setId(rs.getInt("employee_id"));
            employee.setFirstName(rs.getString("first_name"));
            employee.setLastName(rs.getString("last_name"));

            return employee;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public List<Employee> getAllEmployees() {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "select * from employee;";
            PreparedStatement statement = conn.prepareStatement(sql);

            List<Employee> employees = new ArrayList<>();
            ResultSet rs = statement.executeQuery();
            Employee emp;
            while (rs.next()) {
                emp = new Employee();
                emp.setId(rs.getInt("employee_id"));
                emp.setFirstName(rs.getString("first_name"));
                emp.setLastName(rs.getString("last_name"));
                employees.add(emp);
            }

            return employees;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
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
                Logger.log(Logger.Level.ERROR, "Unable to update employee: (" + employee + ").");
                return null;
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
                Logger.log(Logger.Level.ERROR, "Unable to delete employee (" + id + ").");
                return false;
            }

            return true;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return false;
    }
}
