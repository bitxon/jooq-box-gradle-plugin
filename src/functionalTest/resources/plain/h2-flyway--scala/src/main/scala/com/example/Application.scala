package com.example

import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.DriverManager
import java.time.LocalDate

@main def main(): Unit =
  val url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  val conn = DriverManager.getConnection(url, "sa", "")
  try
    val controller = UserController(DSL.using(conn, SQLDialect.H2))
    val created = controller.createUser(
      UserDto(
        id = 0,
        name = "Alice",
        pointsAmount = 0L,
        dateOfBirth = LocalDate.of(1990, 1, 1),
        addresses = List(
          AddressDto(0, "US", "10001", "New York", "Broadway")
        )
      )
    )
    val found = controller.getUser(created.id)
    println(s"Created user ${created.id}, found user ${found.id}")
  finally
    conn.close()
