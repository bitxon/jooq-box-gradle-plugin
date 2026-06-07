package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class Application {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/test";
        try (Connection conn = DriverManager.getConnection(url, "root", "")) {
            var controller = new UserController(DSL.using(conn, SQLDialect.MYSQL));

            UserController.UserDto created = controller.createUser(
                new UserController.UserDto(null, "Alice", 0L, LocalDate.of(1990, 1, 1), List.of(
                    new UserController.AddressDto(null, "US", "10001", "New York", "Broadway")
                ))
            );
            UserController.UserDto found = controller.getUser(created.id());
            System.out.printf("Created user %d, found user %d%n", created.id(), found.id());
        }
    }
}
