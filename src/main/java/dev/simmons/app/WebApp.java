package dev.simmons.app;

import com.google.gson.JsonSyntaxException;
import dev.simmons.data.PostgresEmployeeDAO;
import dev.simmons.data.PostgresExpenseDAO;
import dev.simmons.data.PostgresORM;
import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.*;
import dev.simmons.service.ExpensesService;
import dev.simmons.service.ExpensesServiceImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.simmons.service.ORMExpensesService;
import dev.simmons.utilities.logging.Logger;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;
public class WebApp {
    private static final int port = 5000;
    private static ExpensesService service;
    private static Gson gson;

    private static final String status = "status";
    private static final String index = "index";
    private static final String index_path = "/{" + index + "}";
    private static final String employees = "/employees";
    private static final String expenses = "/expenses";
    private static final String employeeById = employees + index_path;
    private static final String expenseById = expenses + index_path;


    public static void main(String[] args) {
        service = new ORMExpensesService(new PostgresORM<>(Employee.class), new PostgresORM<>(Expense.class));
        //service = new ExpensesServiceImpl(new PostgresEmployeeDAO(), new PostgresExpenseDAO());

        Javalin server = Javalin.create();
        gson = new GsonBuilder().create();

        /*
         * +++++++++++++++++++++++++++
         * +      Landing Route      +
         * +++++++++++++++++++++++++++
         */
        server.get("/", ctx -> ctx.status(200));

        /*
         * +++++++++++++++++++++++++++++
         * +      Employee Routes      +
         * +++++++++++++++++++++++++++++
         */
        server.post(employees,          WebApp::handleCreateEmployee);
        server.get(employees,           WebApp::handleGetEmployees);
        server.get(employeeById,        WebApp::handleGetEmployee);
        server.put(employeeById,        WebApp::handleReplaceEmployee);
        server.delete(employeeById,     WebApp::handleDeleteEmployee);

        /*
         * ++++++++++++++++++++++++++++
         * +      Expense Routes      +
         * ++++++++++++++++++++++++++++
         */
        server.post(expenses,                        WebApp::handleCreateExpense);
        server.post(employeeById + expenses,    WebApp::handleAssigningExpense);
        server.get(expenses,                         WebApp::handleGetExpenses);
        server.get(expenseById,                      WebApp::handleGetExpense);
        server.get(employeeById + expenses,     WebApp::handleGetEmployeeExpenses);
        server.put(expenseById,                      WebApp::handleReplaceExpense);
        server.patch(expenseById + "/approve",  WebApp::handleExpenseApproval);
        server.patch(expenseById + "/deny",     WebApp::handleExpenseDenial);
        server.delete(expenseById,                   WebApp::handleExpenseDeletion);

        /*
         * ++++++++++++++++++++++++++++++++++++++
         * +      Error Handling Callbacks      +
         * ++++++++++++++++++++++++++++++++++++++
         */
        server.exception(JsonSyntaxException.class, (ex, ctx) -> {
            ctx.status(bad_request);
            String response = "Error parsing request: assign expense to employee. " +
                    "Request to " + ctx.path() + " with body: (" + ctx.body() + ").";
            ctx.result(formatResponse(error, response));
            Logger.log(Logger.Level.ERROR, response);
        });
        server.exception(InvalidExpenseStatusException.class, (ex, ctx) -> {
           ctx.status(unprocessable);
           String response;
           if (ctx.queryString() != null) {
               response = formatResponse(error, "Unable to parse the query provided: " + ctx.queryString() + ".");
           } else {
               response = formatResponse(error, ex.getMessage());
           }
           ctx.result(response);
        });
        server.exception(ExpenseNotPendingException.class, (ex, ctx) -> {
            ctx.status(bad_request);
            ctx.result(formatResponse(error, ex.getMessage()));
        });
        server.exception(NonpositiveExpenseException.class, (ex, ctx) -> {
            ctx.status(bad_request);
            ctx.result(formatResponse(error, ex.getMessage()));
        });
        server.exception(InvalidExpenseException.class, (ex, ctx) -> {
           ctx.status(unprocessable);
           ctx.result(formatResponse(error, ex.getMessage()));
        });
        server.exception(NoSuchExpenseException.class, (ex, ctx) -> {
           ctx.status(not_found);
           ctx.result(formatResponse(error, ex.getMessage()));
        });
        server.exception(NoSuchEmployeeException.class, (ex, ctx) -> {
            ctx.status(not_found);
            ctx.result(formatResponse(error, ex.getMessage()));
        });
        server.exception(NoSuchEntityException.class, (ex, ctx) -> {
            ctx.status(not_found);
            ctx.result(formatResponse(error, ex.getMessage()));
        });
        server.exception(InvalidEmployeeException.class, (ex, ctx) -> {
           ctx.status(not_found);
           ctx.result(formatResponse(error, ex.getMessage()));
        });
        server.exception(EmployeeExpenseNotPendingException.class, (ex, ctx) -> {
           ctx.status(bad_request);
           String response = formatResponse(error, ex.getMessage());
           ctx.result(response);
        });

        server.start(port);
    }


    private static final String error = "error";
    private static final String result = "result";

    private static final int ok = 200;
    private static final int created = 201;
    private static final int bad_request = 400;
    private static final int not_found = 404;
    private static final int unprocessable = 422;
    private static final int internal_error = 500;

    private static void handleExpenseDeletion(Context ctx) {
        String response;
        int status = ok;

        String param = ctx.pathParam(index) + "";
        int id = Integer.parseInt(param);
        if (service.deleteExpense(id)) {
            ctx.status(ok);
            response = formatResponse(result, "Successfully deleted expense " + id + ".");
        } else {
            status = internal_error;
            response = formatResponse(error,"Unable to delete expense " + id + ".");
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleExpenseDenial(Context ctx) {
        String response;
        int status = ok;

        String param = ctx.pathParam(index) + "";
        int id = Integer.parseInt(param);
        Expense exp = service.getExpenseById(id);
        if (exp == null) {
            throw new NoSuchExpenseException(id);
        }
        exp.setStatus(Expense.Status.DENIED);
        exp = service.replaceExpense(exp);
        if (exp == null) {
            status = internal_error;
            response = formatResponse(error, "Unable to deny expense" +
                    " matching (id: " + id + ").");
        } else {
            response = formatResponse(result, "Successfully denied expense" +
                    " matching (id:  " + id + ").");
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleExpenseApproval(Context ctx) {
        String response;
        int status = ok;

        String param = ctx.pathParam(index) + "";
        int id = Integer.parseInt(param);
        Expense exp = service.getExpenseById(id);
        if (exp == null) {
            throw new NoSuchExpenseException(id);
        }
        exp.setStatus(Expense.Status.APPROVED);
        exp = service.replaceExpense(exp);
        if (exp == null) {
            status = internal_error;
            response = formatResponse(error, "Unable to approve expense" +
                    " matching (id: " + id + ").");
        } else {
            response = formatResponse(result, "Successfully approved expense" +
                    " matching (id: " + id + ").");
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleReplaceExpense(Context ctx) {
        String response;
        int status = ok;

        int id = Integer.parseInt(ctx.pathParam(index));
        Expense expense = gson.fromJson(ctx.body(), Expense.class);
        if (expense == null) {
            status = not_found;
            response = formatResponse(error, "Unable to parse the provided expense. " +
                    "Check the sent expense.");
        } else {
            expense.setId(id);
            Expense received = service.replaceExpense(expense);
            if (received == null) {
                status = internal_error;
                response = formatResponse(error, "Was unable to update the provided expense: " +
                        expense + ". " + "Check that an expense with that id exists.\"}");
            } else {
                response = formatResponse(result, "Expense matching " +
                        "(id: " + expense.getId() + ") was updated.");
            }
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleGetEmployeeExpenses(Context ctx) {
        String response;

        String param = ctx.pathParam(index) + "";
        int id = Integer.parseInt(param);
        List<Expense> expenses = service.getExpensesByEmployee(id);

        response = formatResponse(result, gson.toJson(expenses));


        ctx.status(ok);
        ctx.result(response);
    }

    private static void handleGetExpense(Context ctx) {
        String response;
        int status = ok;

        String param = ctx.pathParam(index) + "";

        int id = Integer.parseInt(param);
        Expense exp = service.getExpenseById(id);
        if (exp == null) {
            status = internal_error;
            response = formatResponse(error, "Unable to get the expense " + id + ".");
        } else {
            response = formatResponse(result, gson.toJson(exp));
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleGetExpenses(Context ctx) {
        String response;

        List<Expense> expenses;
        if (ctx.queryString() != null && !Objects.equals(ctx.queryString(), "")) {
            String query = ctx.queryParam(status);
            try {
                query = query.toUpperCase();
                expenses = service.getExpensesByStatus(Expense.Status.valueOf(query));
            } catch (IllegalArgumentException | NullPointerException iae) {
                throw new InvalidExpenseStatusException("Unable to parse the status from query string: " + ctx.queryString());
            }
        } else {
            expenses = service.getAllExpenses();
        }

        response = formatResponse(result, gson.toJson(expenses));

        ctx.status(ok);
        ctx.result(response);
    }

    private static void handleAssigningExpense(Context ctx) {
        String response;
        int status = ok;
        String param = ctx.pathParam(index) + "";
        int id;
        Expense exp;
        id = Integer.parseInt(param);
        exp = gson.fromJson(ctx.body(), Expense.class);

        if (exp == null) {
            status = bad_request;
            response = formatResponse(error,"Unable to parse the provided expense. Check the syntax: '" + ctx.body() + "'.");
        } else {
            if (exp.getId() > 0) {
                exp = service.getExpenseById(exp.getId());
            }
            exp.setIssuer(id);
            Expense received;
            if (exp.getId() > 0) {
                received = service.replaceExpense(exp);
            } else {
                received = service.createExpense(exp);
            }
            if (received == null) {
                status = internal_error;
                response = formatResponse(error, "Unable to assign the provided expense, " + exp + ", to employee " + id + ".");
            } else {
                response = formatResponse(error, "Expense, " + received.getId() + ", successfully assigned to employee " + id + ".");
            }
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleCreateExpense(Context ctx) {
        String response;
        int status = created;

        Expense exp = gson.fromJson(ctx.body(), Expense.class);
        if (exp == null) {
            status = bad_request;
            response = formatResponse(error, "Unable to parse the provided expense. " +
                    "Check the syntax: '" + ctx.body() + "'.");
            Logger.log(Logger.Level.WARNING, response);
        } else {
            Expense received = service.createExpense(exp);
            if (received == null) {
                status = internal_error;
                response = formatResponse(error, "Unable to save the provided expense: " + exp + ".");
            } else {
                response = formatResponse(result,"Created new expense, " + received.getId() + ".");
            }
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleDeleteEmployee(Context ctx) {
        String param = ctx.pathParam(index) + "";
        int id = Integer.parseInt(param);

        String response;
        int status = ok;

        if (service.deleteEmployee(id)) {
            response = formatResponse(result, "Successfully deleted employee " + id + ".");
        } else {
            status = internal_error;
            response = formatResponse(error, "Unable to delete employee " + id + ".");
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleReplaceEmployee(Context ctx) {
        String param = ctx.pathParam(index) + "";
        int id = Integer.parseInt(param);
        Employee emp = gson.fromJson(ctx.body(), Employee.class);
        emp.setId(id);
        emp = service.replaceEmployee(emp);

        String response;
        int status = ok;

        if (emp == null) {
            status = internal_error;
            response = formatResponse(error, "Unable to update the employee.");
        } else {
            response = formatResponse(result, gson.toJson(emp));
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static void handleGetEmployee(Context ctx) {
        String param = ctx.pathParam(index) + "";
        int id = Integer.parseInt(param);
        Employee emp = service.getEmployeeById(id);
        if (emp == null) {
            throw new NoSuchEmployeeException(id);
        }
        ctx.status(ok);
        ctx.result(formatResponse(result, gson.toJson(emp)));
    }

    private static void handleGetEmployees(Context ctx) {
        List<Employee> employeeList = service.getAllEmployees();
        String response;

        response = formatResponse(error, gson.toJson(employeeList));

        ctx.status(ok);
        ctx.result(response);
    }

    private static void handleCreateEmployee(Context ctx) {
        Employee emp = gson.fromJson(ctx.body(), Employee.class);
        int status = created;
        String response;
        if (emp == null) {
            status = bad_request;
            response = formatResponse(error, "Unable to parse the provided employee. Check the syntax: '" + ctx.body() + "'.");
        } else {
            Employee received = service.createEmployee(emp);
            if (received == null) {
                status = internal_error;
                response = formatResponse(error, "Unable to save the provided employee: " + emp + ".");
            } else {
                response = formatResponse(result, "Created new employee, " + received.getId() + ".");
            }
        }

        ctx.status(status);
        ctx.result(response);
    }

    private static String formatResponse(String label, String contents) {
        return "{\"" + label + "\": \"" + contents + "\"}";
    }
}
