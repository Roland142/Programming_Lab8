package db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Открывает JDBC-соединение с PostgreSQL, используя credentials из ~/.pgpass. */
public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://pg/studs";

    public static Connection open() throws SQLException, IOException {
        String[] creds = PgPassReader.readCredentials();
        String user = creds[0];
        String pass = creds[1];

        Connection connection = DriverManager.getConnection(URL, user, pass);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + user);
        }
        return connection;
    }
}
