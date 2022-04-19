package dev.simmons.utilities.connection;

import dev.simmons.utilities.connection.PostgresConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionTests {
    @Test
    public void canConnect() {
        try (Connection connection = PostgresConnection.getConnection()) {
            Assertions.assertNotNull(connection);
            Assertions.assertEquals("expenses", connection.getCatalog());
            Assertions.assertNotNull(connection);
        } catch (SQLException se) {
            Assertions.fail(se.getMessage());
        }
    }
}
