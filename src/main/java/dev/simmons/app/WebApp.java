package dev.simmons.app;

import com.google.gson.JsonSyntaxException;
import dev.simmons.data.PostgresEmployeeDAO;
import dev.simmons.data.PostgresExpenseDAO;
import dev.simmons.entities.Employee;
import dev.simmons.entities.Expense;
import dev.simmons.exceptions.*;
import dev.simmons.service.ExpensesService;
import dev.simmons.service.ExpensesServiceImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.simmons.utilities.logging.Logger;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;
import java.util.Objects;
public class WebApp {
    private static ExpensesService service;
    private static Gson gson;
    public static void main(String[] args) {
        service = new ExpensesServiceImpl(new PostgresEmployeeDAO(), new PostgresExpenseDAO());
        Javalin server = Javalin.create();
        gson = new GsonBuilder().create();

        /*
         * +++++++++++++++++++++++++++
         * +      Landing Route      +
         * +++++++++++++++++++++++++++
         */
        server.get("/", ctx -> {
           ctx.status(200);
        });


        /*
         * +++++++++++++++++++++++++++++
         * +      Employee Routes      +
         * +++++++++++++++++++++++++++++
         */
        server.post("/employees",           WebApp::handleCreateEmployee);
        server.get("/employees",            WebApp::handleGetEmployees);
        server.get("/employees/{index}",    WebApp::handleGetEmployee);
        server.put("/employees/{index}",    WebApp::handleReplaceEmployee);
        server.delete("/employees/{index}", WebApp::handleDeleteEmployee);

        /*
         * ++++++++++++++++++++++++++++
         * +      Expense Routes      +
         * ++++++++++++++++++++++++++++
         */
        server.post("/expenses",                   WebApp::handleCreateExpense);
        server.post("/employees/{index}/expenses", WebApp::handleAssigningExpense);
        server.get("/expenses",                    WebApp::handleGetExpenses);
        server.get("/expenses/{index}",            WebApp::handleGetExpense);
        server.get("/employees/{index}/expenses",  WebApp::handleGetEmployeeExpenses);
        server.put("/expenses/{index}",            WebApp::handleReplaceExpense);
        server.patch("/expenses/{index}/approve",  WebApp::handleExpenseApproval);
        server.patch("/expenses/{index}/deny",     WebApp::handleExpenseDenial);
        server.delete("/expenses/{index}",         WebApp::handleExpenseDeletion);

        /*
         * ++++++++++++++++++++++++++++++++++++++
         * +      Error Handling Callbacks      +
         * ++++++++++++++++++++++++++++++++++++++
         */
        server.exception(JsonSyntaxException.class, (ex, ctx) -> {
            ctx.status(400);
            ctx.result("{\"error\":\"Error parsing request: assign expense to employee. Request to " + ctx.path() + " with body: (" + ctx.body() + ")\"}");
            Logger.log(Logger.Level.ERROR, "Error parsing request: assign expense to employee. Request to " + ctx.path() + " with body: (" + ctx.body() + ")");
        });
        server.exception(InvalidExpenseStatusException.class, (ex, ctx) -> {
           ctx.status(400);
           if (ctx.queryString() != null) {
               ctx.result("{\"error\":\"Unable to parse the query provided: " + ctx.queryString() + "\"}");
           } else {
               ctx.result("{\"error\":\"" + ex.getMessage() + "\"}");
           }
        });
        server.exception(ExpenseNotPendingException.class, (ex, ctx) -> {
            ctx.status(400); // One of the 400s since the user is making a mistake
            ctx.result("{\"error\":\"" + ex.getMessage() + "\"}");
        });
        server.exception(NonpositiveExpenseException.class, (ex, ctx) -> {
            ctx.status(400); // One of the 400s since the user is making a mistake
            ctx.result("{\"error\":\"" + ex.getMessage() + "\"}");
        });
        server.exception(InvalidExpenseException.class, (ex, ctx) -> {
           ctx.status(400);
           ctx.result("{\"error\":\"" + ex.getMessage() + "\"}");
        });
        server.exception(NoSuchExpenseException.class, (ex, ctx) -> {
           ctx.status(404);
           ctx.result("{\"error\":\"" + ex.getMessage() + "\"}");
        });
        server.exception(NoSuchEmployeeException.class, (ex, ctx) -> {
            ctx.status(404);
            ctx.result("{\"error\":\"" + ex.getMessage() + "\"}");
        });
        server.exception(InvalidEmployeeException.class, (ex, ctx) -> {
           ctx.status(404);
           ctx.result("{\"error\":\"" + ex.getMessage() + "\"}");
        });
        server.exception(EmployeeExpenseNotPendingException.class, (ex, ctx) -> {
           ctx.status(400);
           ctx.result("{\"error\":\"Unable to delete employee because they have an approved or denied expense request.\"}");
        });

        server.start(5000);
    }

    private static void handleExpenseDeletion(Context ctx) {
        String param = ctx.pathParam("index") + "";
        int id = Integer.parseInt(param);
        if (service.deleteExpense(id)) {
            ctx.status(200);
            ctx.result("{\"result\":\"Successfully deleted expense " + id + "\"}");
        } else {
            ctx.status(500);
            ctx.result("{\"error\": \"Unable to delete expense " + id + "\"}");
        }
    }

    private static void handleExpenseDenial(Context ctx) {
        String param = ctx.pathParam("index") + "";
        int id = Integer.parseInt(param);
        Expense exp = service.getExpenseById(id);
        if (exp == null) {
            throw new NoSuchExpenseException("Unable to find expense with id " + id);
        }
        exp.setStatus(Expense.Status.DENIED);
        exp = service.replaceExpense(exp);
        if (exp == null) {
            ctx.status(500);
            ctx.result("{\"error\": \"Unable to deny expense " + id + ".\"}");
        } else {
            ctx.status(200);
            ctx.result("{\"result\": \"Successfully denied expense " + id + ".\"}");
        }
    }

    private static void handleExpenseApproval(Context ctx) {
        String param = ctx.pathParam("index") + "";
        int id = Integer.parseInt(param);
        Expense exp = service.getExpenseById(id);
        if (exp == null) {
            throw new NoSuchExpenseException("Unable to find expense with id " + id);
        }
        exp.setStatus(Expense.Status.APPROVED);
        exp = service.replaceExpense(exp);
        if (exp == null) {
            ctx.status(500);
            ctx.result("{\"error\": \"Unable to approve expense " + id + ".\"}");
        } else {
            ctx.status(200);
            ctx.result("{\"result\": \"Successfully approved expense " + id + ".\"}");
        }
    }

    private static void handleReplaceExpense(Context ctx) {
        int index = Integer.parseInt(ctx.pathParam("index"));
        Expense expense = gson.fromJson(ctx.body(), Expense.class);
        if (expense == null) {
            ctx.status(404);
            ctx.result("{\"error\":\"Unable to parse the provided expense. Check the sent expense.\"}");
        } else {
            expense.setId(index);
            Expense received = service.replaceExpense(expense);
            if (received == null) {
                ctx.status(500);
                ctx.result("{\"error\":\"Was unable to update the provided expense: " + expense + ". Check that an expense with that id exists.\"}");
            } else {
                ctx.status(201);
                ctx.result("{\"result\":\"Expense updated\"}");
            }
        }
    }

    private static void handleGetEmployeeExpenses(Context ctx) {
        String param = ctx.pathParam("index") + "";
        int id = Integer.parseInt(param);
        List<Expense> expenses = service.getExpensesByEmployee(id);
        if (expenses == null) {
            ctx.status(500);
            ctx.result("{\"error\":\"Unable to get the list of expenses for employee " + id + ".\"}");
        } else {
            ctx.status(200);
            ctx.result("{\"result\": " + gson.toJson(expenses) + "}");
        }
    }

    private static void handleGetExpense(Context ctx) {
        String param = ctx.pathParam("index") + "";

        int id = Integer.parseInt(param);
        Expense exp = service.getExpenseById(id);
        if (exp == null) {
            ctx.status(500);
            ctx.result("{\"error\":\"Unable to get the expense " + id + ".\"}");
        } else {
            ctx.status(200);
            ctx.result("{\"result\": " + gson.toJson(exp) + "}");
        }
    }

    private static void handleGetExpenses(Context ctx) {
        List<Expense> expenses;
        if (ctx.queryString() != null && !Objects.equals(ctx.queryString(), "")) {
            String query = ctx.queryParam("status");
            try {
                query = query.toUpperCase();
                expenses = service.getExpensesByStatus(Expense.Status.valueOf(query));
            } catch (IllegalArgumentException | NullPointerException iae) {
                throw new InvalidExpenseStatusException("Unable to parse the status from query string: " + ctx.queryString());
            }
        } else {
            expenses = service.getAllExpenses();
        }
        if (expenses == null) {
            ctx.status(500);
            ctx.result("{\"error\":\"Unable to retrieve all expenses.\"}");
        } else {
            ctx.status(200);
            ctx.result("{\"result\": " + gson.toJson(expenses)  + "}");
        }
    }

    private static void handleAssigningExpense(Context ctx) {
        String param = ctx.pathParam("index") + "";
        int id = 0;
        Expense exp = null;
        id = Integer.parseInt(param);
        exp = gson.fromJson(ctx.body(), Expense.class);

        if (exp == null) {
            ctx.status(500);
            ctx.result("{\"error\":\"Unable to parse the provided expense. Check the syntax: '" + ctx.body() + "'\"}");
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
                ctx.status(500);
                ctx.result("{\"error\":\"Unable to assign the provided expense, " + exp + ", to employee " + id + "\"}");
            } else {
                ctx.status(201);
                ctx.result("{\"result\":\"Expense, " + received.getId() + ", successfully assigned to employee " + id + ".\"}");
            }
        }
    }

    private static void handleCreateExpense(Context ctx) {
        Expense exp = gson.fromJson(ctx.body(), Expense.class);
        if (exp == null) {
            ctx.status(500);
            ctx.result("{\"error\":\"Unable to parse the provided expense. Check the syntax: '" + ctx.body() + "'\"}");
        } else {
            Expense received = service.createExpense(exp);
            if (received == null) {
                ctx.status(500);
                ctx.result("{\"error\":\"Unable to save the provided expense: " + exp + "\"}");
            } else {
                ctx.status(201);
                ctx.result("{\"result\":\"Created new expense, " + received.getId() + "\"}");
            }
        }
    }

    private static void handleDeleteEmployee(Context ctx) {
        String param = ctx.pathParam("index") + "";
        int id = Integer.parseInt(param);
        if (service.deleteEmployee(id)) {
            ctx.status(200);
            ctx.result("{\"result\":\"Successfully deleted employee " + id + "\"}");
        } else {
            ctx.status(500);
            ctx.result("{\"error\": \"Unable to delete employee " + id + "\"}");
        }
    }

    private static void handleReplaceEmployee(Context ctx) {
        String param = ctx.pathParam("index") + "";
        int id = Integer.parseInt(param);
        Employee emp = gson.fromJson(ctx.body(), Employee.class);
        emp.setId(id);
        emp = service.replaceEmployee(emp);
        if (emp == null) {
            ctx.status(500);
            ctx.result("{\"error\": \"Unable to update the employee.\"}");
        } else {
            ctx.status(200);
            ctx.result("{\"result\": " + gson.toJson(emp) + "}");
        }
    }

    private static void handleGetEmployee(Context ctx) {
        String param = ctx.pathParam("index") + "";
        int id = Integer.parseInt(param);
        Employee emp = service.getEmployeeById(id);
        if (emp == null) {
            throw new NoSuchEmployeeException("Unable to find employee with id " + id);
        }
        ctx.status(200);
        ctx.result("{\"result\": " + gson.toJson(emp) + "}");
    }

    private static void handleGetEmployees(Context ctx) {
        List<Employee> employeeList = service.getAllEmployees();
        if (employeeList == null) {
            ctx.status(500);
            ctx.result("{\"error\":\"Unable to retrieve employee list.\"}");
        } else {
            ctx.status(200);
            ctx.result("{\"result\": " +  gson.toJson(employeeList) + "}");
        }
    }

    private static void handleCreateEmployee(Context ctx) {
        Employee emp = gson.fromJson(ctx.body(), Employee.class);
        if (emp == null) {
            ctx.status(500);
            ctx.result("{\"error\":\"Unable to parse the provided employee. Check the syntax: '" + ctx.body() + "'\"}");
        } else {
            Employee received = service.createEmployee(emp);
            if (received == null) {
                ctx.status(500);
                ctx.result("{\"error\":\"Unable to save the provided employee: " + emp + "\"}");
            } else {
                ctx.status(201);
                ctx.result("{\"result\":\"Created new employee, " + received.getId() + "\"}");
            }
        }
    }
}
