package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class Application {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite::memory:";
        try (Connection conn = DriverManager.getConnection(url)) {
            var controller = new UserController(DSL.using(conn, SQLDialect.SQLITE));
            System.out.printf("Users: %d, Addresses: %d%n",
                controller.getUserCount(), controller.getAddressCount());
        }
    }
}
