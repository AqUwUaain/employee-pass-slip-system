package database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseMigration {

    public static void runMigrations() {

        try {

            Connection connection =
                    DatabaseConnection.connect();

            if (connection == null) {
                System.err.println("Cannot run migrations: no database connection.");
                return;
            }

            Statement statement = connection.createStatement();

            String[] migrations = {
                    "ALTER TABLE employees ADD COLUMN IF NOT EXISTS manager VARCHAR(255) DEFAULT ''",
                    "ALTER TABLE employees ADD COLUMN IF NOT EXISTS email VARCHAR(255) DEFAULT ''",
                    "ALTER TABLE employees ADD COLUMN IF NOT EXISTS address TEXT DEFAULT ''",
                    "ALTER TABLE employees ADD COLUMN IF NOT EXISTS emergency_contact VARCHAR(255) DEFAULT ''",
                    "CREATE TABLE IF NOT EXISTS activity_logs (id SERIAL PRIMARY KEY, action VARCHAR(255), description TEXT, user_id INT, username VARCHAR(255), timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                    "ALTER TABLE activity_logs ADD COLUMN IF NOT EXISTS employee_id INT DEFAULT 0",
                    "ALTER TABLE pass_slips ADD COLUMN IF NOT EXISTS duration_minutes BIGINT DEFAULT 0",
                    "CREATE TABLE IF NOT EXISTS password_reset_requests (id SERIAL PRIMARY KEY, email VARCHAR(255) NOT NULL, requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, status VARCHAR(20) DEFAULT 'PENDING')",
                    "ALTER TABLE password_reset_requests ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP DEFAULT NULL",
                    "ALTER TABLE password_reset_requests ADD COLUMN IF NOT EXISTS used BOOLEAN DEFAULT FALSE",
                    "CREATE TABLE IF NOT EXISTS signatures (id SERIAL PRIMARY KEY, user_id INT NOT NULL, signature_name VARCHAR(255) NOT NULL, image_data BYTEA NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_signatures_user_id ON signatures (user_id)",
                    "ALTER TABLE pass_slips ADD COLUMN IF NOT EXISTS estimated_return TIMESTAMP DEFAULT NULL"
            };

            for (String sql : migrations) {
                try {
                    statement.executeUpdate(sql);
                } catch (Exception e) {
                    // Column already exists, ignore
                }
            }

            System.out.println("Database migrations completed.");

            statement.close();
            connection.close();

        } catch (Exception e) {
            System.err.println("Migration error: " + e.getMessage());
        }

    }
}
