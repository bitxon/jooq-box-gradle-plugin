package com.example

import com.example.jooq.Tables.*
import com.example.jooq.tables.records.AddressesRecord
import com.example.jooq.tables.records.UsersRecord
import org.jooq.DSLContext
import java.time.LocalDate
import scala.jdk.CollectionConverters.*

case class AddressDto(id: Int, country: String, zip: String, city: String, street: String)
case class UserDto(id: Int, name: String, pointsAmount: Long, dateOfBirth: LocalDate, addresses: List[AddressDto])

class UserController(private val dsl: DSLContext):

  def createUser(request: UserDto): UserDto =
    val user = dsl.insertInto(USERS)
      .set(USERS.NAME, request.name)
      .set(USERS.POINTS_AMOUNT, Long.box(request.pointsAmount))
      .set(USERS.DATE_OF_BIRTH, request.dateOfBirth)
      .returning()
      .fetchOne()

    val addresses = request.addresses.map { a =>
      dsl.insertInto(ADDRESSES)
        .set(ADDRESSES.USER_ID, user.getValue(USERS.ID))
        .set(ADDRESSES.COUNTRY, a.country)
        .set(ADDRESSES.ZIP, a.zip)
        .set(ADDRESSES.CITY, a.city)
        .set(ADDRESSES.STREET, a.street)
        .returning()
        .fetchOne()
    }

    toDto(user, addresses)

  def getUser(id: Int): UserDto =
    val user = dsl.fetchOne(USERS, USERS.ID.eq(id))
    val addresses = dsl.fetch(ADDRESSES, ADDRESSES.USER_ID.eq(id)).asScala.toList
    toDto(user, addresses)

  private def toDto(user: UsersRecord, addresses: List[AddressesRecord]): UserDto =
    UserDto(
      id = user.getValue(USERS.ID).intValue(),
      name = user.getValue(USERS.NAME),
      pointsAmount = user.getValue(USERS.POINTS_AMOUNT).longValue(),
      dateOfBirth = user.getValue(USERS.DATE_OF_BIRTH),
      addresses = addresses.map { a =>
        AddressDto(
          id = a.getValue(ADDRESSES.ID).intValue(),
          country = a.getValue(ADDRESSES.COUNTRY),
          zip = a.getValue(ADDRESSES.ZIP),
          city = a.getValue(ADDRESSES.CITY),
          street = a.getValue(ADDRESSES.STREET)
        )
      }
    )
