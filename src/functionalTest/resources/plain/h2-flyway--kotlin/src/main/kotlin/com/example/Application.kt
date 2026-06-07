package com.example

import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.DriverManager

fun main() {
    val url = "jdbc:h2:mem:test"
    DriverManager.getConnection(url, "sa", "").use { conn ->
        val controller = UserController(DSL.using(conn, SQLDialect.H2))
        val created = controller.createUser(
            UserController.UserDto(
                id = null,
                name = "Alice",
                pointsAmount = 0L,
                dateOfBirth = java.time.LocalDate.of(1990, 1, 1),
                addresses = listOf(
                    UserController.AddressDto(null, "US", "10001", "New York", "Broadway")
                )
            )
        )
        val found = controller.getUser(created.id!!)
        println("Created user ${created.id}, found user ${found.id}")
    }
}
