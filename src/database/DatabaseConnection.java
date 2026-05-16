package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {

    public static Connection connect() {

        try {

            String url =
                    "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:6543/postgres";

            String user =
                    "postgres.kvtbvptrcdnenogrwtuc";

            String password =
                    "";

            Properties props =
                    new Properties();

            props.setProperty(
                    "user",
                    user
            );

            props.setProperty(
                    "password",
                    password
            );

            props.setProperty(
                    "sslmode",
                    "require"
            );

            Connection connection =
                    DriverManager.getConnection(
                            url,
                            props
                    );

            System.out.println(
                    "Database Connected!"
            );

            return connection;

        }
        catch (Exception e) {

            e.printStackTrace();

            return null;

        }

    }

}