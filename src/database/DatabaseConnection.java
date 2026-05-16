package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    public static Connection connect() {

        try {

            Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/employee_pass_slip",
                    "postgres",
                    "PostGres@177"
            );

            System.out.println("Database Connected!");

            return conn;

        } catch (Exception e) {

            System.out.println("Connection Failed!");
            e.printStackTrace();

            return null;
        }
    }
}
