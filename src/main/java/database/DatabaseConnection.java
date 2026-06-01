package database;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {

    public static Connection connect() {

        try {

            Dotenv dotenv = Dotenv.load();

            String url = dotenv.get("DB_URL");
            String user = dotenv.get("DB_USER");
            String password = dotenv.get("DB_PASSWORD");

            Properties props = new Properties();

            props.setProperty("user", user);
            props.setProperty("password", password);
            props.setProperty("sslmode", "require");

            Connection connection =
                    DriverManager.getConnection(
                            url,
                            props
                    );

            System.out.println("Database Connected!");

            return connection;

        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }
    }
}