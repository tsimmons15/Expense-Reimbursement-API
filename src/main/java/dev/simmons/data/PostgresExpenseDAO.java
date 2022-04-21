package dev.simmons.data;

import dev.simmons.entities.Expense;
import dev.simmons.exceptions.*;
import dev.simmons.utilities.connection.PostgresConnection;
import dev.simmons.utilities.logging.Logger;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresExpenseDAO implements ExpenseDAO{
    @Override
    public Expense createExpense(Expense expense) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "insert into expense (amount, status, date, issuer) values (?,?,?,?);";
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, expense.getAmount());
            statement.setString(2, expense.getStatus().name());
            statement.setLong(3, expense.getDate());
            if (expense.getIssuer() == 0) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, expense.getIssuer());
            }

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Unable to insert expense (" + expense + ").");
                return null;
            }
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            int id = rs.getInt(1);
            expense.setId(id);

            return expense;
        } catch (SQLException se) {
            if (se.getSQLState().equals("23503")) {
                Logger.log(Logger.Level.WARNING, "Attempt to create expense for a non-existent employee with id " + expense.getIssuer());
                throw new NoSuchEmployeeException("No such employee exists with id " + expense.getIssuer());
            }
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public Expense getExpenseById(int id) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "select * from expense where expense_id = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);

            ResultSet rs = statement.executeQuery();
            Expense exp = new Expense();
            rs.next();
            exp.setId(rs.getInt("expense_id"));
            exp.setIssuer(rs.getInt("issuer"));
            exp.setAmount(rs.getLong("amount"));
            exp.setDate(rs.getLong("date"));
            exp.setStatus(Expense.Status.valueOf(rs.getString("status")));

            return exp;
        } catch (SQLException se) {
            System.out.println("getExpenseById: " + se.getSQLState());
            if (se.getSQLState().equals("24000")) {
                Logger.log(Logger.Level.WARNING, "Search for non-existent expense with id " + id);
                throw new NoSuchExpenseException("No expense with id " + id + " was found. Make sure there is an expense with that id.");
            }

            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public List<Expense> getAllExpenses() {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "select * from expense;";
            PreparedStatement statement = conn.prepareStatement(sql);

            ResultSet rs = statement.executeQuery();
            List<Expense> expenses = new ArrayList<>();
            Expense exp;
            while (rs.next()) {
                exp = new Expense();
                exp.setIssuer(rs.getInt("issuer"));
                exp.setId(rs.getInt("expense_id"));
                exp.setStatus(Expense.Status.valueOf(rs.getString("status")));
                exp.setDate(rs.getLong("date"));
                exp.setAmount(rs.getLong("amount"));
                expenses.add(exp);
            }

            return expenses;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public List<Expense> getExpensesByStatus(Expense.Status status) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "select * from expense where status = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, status.name());

            ResultSet rs = statement.executeQuery();
            List<Expense> expenses = new ArrayList<>();
            Expense exp;
            while (rs.next()) {
                    exp = new Expense();
                    exp.setId(rs.getInt("expense_id"));
                    exp.setStatus(Expense.Status.valueOf(rs.getString("status")));
                    exp.setIssuer(rs.getInt("issuer"));
                    exp.setDate(rs.getLong("date"));
                    exp.setAmount(rs.getLong("amount"));
                    expenses.add(exp);
            }

            return expenses;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public List<Expense> getAllEmployeeExpenses(int employeeId) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "select * from expense where issuer = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, employeeId);

            ResultSet rs = statement.executeQuery();
            List<Expense> expenses = new ArrayList<>();
            Expense exp;
            while (rs.next()) {
                exp = new Expense();
                exp.setId(rs.getInt("expense_id"));
                exp.setDate(rs.getLong("date"));
                exp.setIssuer(rs.getInt("issuer"));
                exp.setStatus(Expense.Status.valueOf(rs.getString("status")));
                exp.setAmount(rs.getLong("amount"));
                expenses.add(exp);
            }

            return expenses;
        } catch (SQLException se) {
            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public Expense replaceExpense(Expense expense) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "update expense set amount = ?, status = ?, " +
                    "date = ?, issuer = ? where expense_id = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setLong(1,expense.getAmount());
            statement.setString(2, expense.getStatus().name());
            statement.setLong(3, expense.getDate());
            if (expense.getIssuer() == 0) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setInt(4, expense.getIssuer());
            }
            statement.setInt(5, expense.getId());

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Unable to update " + expense + ".");
                throw new NoSuchExpenseException("No expense matching (" + expense + ") was found. Make sure the expense exists.");
            }

            return expense;
        } catch (SQLException se) {
            if (se.getSQLState().equals("23503")) {
                throw new InvalidEmployeeException("Employee " + expense.getIssuer() + " does not exist.");
            } else if (se.getSQLState().equals("P0001")) {
                Logger.log(Logger.Level.WARNING, "Attempt to replace contents of non-pending expense.");
                throw new ExpenseNotPendingException(expense.getId());
            }

            Logger.log(Logger.Level.ERROR, se);
        }
        return null;
    }

    @Override
    public boolean deleteExpense(int id) {
        try (Connection conn = PostgresConnection.getConnection()) {
            String sql = "delete from expense where expense_id = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);

            int updated = statement.executeUpdate();
            if (updated != 1) {
                Logger.log(Logger.Level.WARNING, "Attempt to delete expense with id " + id + " unsuccessful because no expense was found.");
                throw new NoSuchExpenseException("Unable to find an expense with id (" + id + "). Make sure there is an expense with this id.");
            }

            return true;
        } catch (SQLException se) {
            if (se.getSQLState().equals("P0001")) {
                Logger.log(Logger.Level.WARNING, "Attempt to delete non-pending expense.");
                throw new ExpenseNotPendingException(id);
            }

            Logger.log(Logger.Level.ERROR, se);
        }
        return false;
    }
}
