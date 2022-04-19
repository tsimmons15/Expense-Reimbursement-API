package dev.simmons.utilities.connection;

import dev.simmons.utilities.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Static class holding connection information for connection to the postgreSQL database.
 */
public class PostgresConnection {
    private static final String dbName = "expenses";
    private static final String password = System.getenv("POSTGRES_PASSWORD");
    private static final String username = System.getenv("POSTGRES_USERNAME");
    private static final String urlAWS = "jdbc:postgresql://" + System.getenv("POSTGRES_AWS") + "/" + dbName;
    private static final String urlLocal = "jdbc:postgresql://localhost:5432/" + dbName;

    /**
     * Get connection with stored connection information.
     * @return A database connection if successfully connected, null otherwise.
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(urlLocal, username, password);
        } catch (SQLException e) {
            Logger.log(Logger.Level.WARNING, e);
            return null;
        }
    }
}
