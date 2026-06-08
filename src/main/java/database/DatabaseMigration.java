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
                    "ALTER TABLE activity_logs ADD COLUMN IF NOT EXISTS employee_id INT DEFAULT 0",
                    "ALTER TABLE pass_slips ADD COLUMN IF NOT EXISTS duration_minutes BIGINT DEFAULT 0"
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
