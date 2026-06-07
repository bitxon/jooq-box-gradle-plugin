package com.example;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.util.List;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class Application {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/overridedb";
        try (Connection conn = DriverManager.getConnection(url, "overrideuser", "overridepass")) {
            var controller = new UserController(DSL.using(conn, SQLDialect.MYSQL));

            UserController.UserDto created = controller.createUser(
                new UserController.UserDto(null, "Alice", BigInteger.ZERO, Date.valueOf("1990-01-01"), List.of(
                    new UserController.AddressDto(null, "US", "10001", "New York", "Broadway")
                ))
            );
            UserController.UserDto found = controller.getUser(created.id());
            System.out.printf("Created user %d, found user %d%n", created.id(), found.id());
        }
    }
}
