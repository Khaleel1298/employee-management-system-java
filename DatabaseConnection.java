import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/employee_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "Kanna@2004";

    public static Connection getConnection() throws SQLException {
        Map<String, String> environment = System.getenv();
        String url = environment.getOrDefault("DB_URL", DEFAULT_URL);
        String username = environment.getOrDefault("DB_USERNAME", DEFAULT_USERNAME);
        String password = environment.getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);

        List<ConnectionAttempt> attempts = new ArrayList<>();
        attempts.add(new ConnectionAttempt(url, username, password));

        if (!environment.containsKey("DB_USERNAME") && !environment.containsKey("DB_PASSWORD")) {
            attempts.add(new ConnectionAttempt(url, "root", "root"));
            attempts.add(new ConnectionAttempt(url, "root", "password"));
            attempts.add(new ConnectionAttempt(url, "root", "admin"));
            attempts.add(new ConnectionAttempt(url, "root", "mysql"));
            attempts.add(new ConnectionAttempt(url, "admin", "admin"));
            attempts.add(new ConnectionAttempt(url, "mysql", "mysql"));
        }

        SQLException lastException = null;
        for (ConnectionAttempt attempt : attempts) {
            try {
                return DriverManager.getConnection(attempt.url, attempt.username, attempt.password);
            } catch (SQLException exception) {
                lastException = exception;
            }
        }

        throw new SQLException(
                "Could not connect to MySQL. Set DB_URL, DB_USERNAME, and DB_PASSWORD environment variables or update the defaults in DatabaseConnection.java.",
                lastException
        );
    }

    private static final class ConnectionAttempt {
        private final String url;
        private final String username;
        private final String password;

        private ConnectionAttempt(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }
    }
}
