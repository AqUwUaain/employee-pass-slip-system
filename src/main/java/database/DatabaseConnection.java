package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {

    private static HikariDataSource dataSource;

    static {
        try {
            Dotenv dotenv = Dotenv.load();

            String jdbcUrl = dotenv.get("DB_URL") + "?sslmode=require&prepareThreshold=0";
            String user = dotenv.get("DB_USER");
            String password = dotenv.get("DB_PASSWORD");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);
            System.out.println("HikariCP pool initialized.");

        } catch (Exception e) {
            System.err.println("Failed to init pool: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection connect() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                return dataSource.getConnection();
            }
        } catch (SQLException e) {
            System.err.println("Pool connection failed: " + e.getMessage());
        }
        return null;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariCP pool shut down.");
        }
    }
}
