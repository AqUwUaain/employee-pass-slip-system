package controllers;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserListController {

    public static ObservableList<User> getUsers() {

        ObservableList<User> userList =
                FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.connect()) {

            String query =
                    """
                    SELECT * FROM users
                    ORDER BY id ASC
                    """;

            PreparedStatement statement =
                    connection.prepareStatement(query);

            ResultSet resultSet =
                    statement.executeQuery();

            while(resultSet.next()) {

                userList.add(

                        new User(

                                resultSet.getInt("id"),

                                resultSet.getString("username"),

                                resultSet.getString("role")

                        )

                );

            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }

        return userList;

    }

}
