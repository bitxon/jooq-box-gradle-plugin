package com.example

import com.example.jooq.tables.records.AddressesRecord
import com.example.jooq.tables.records.UsersRecord
import com.example.jooq.tables.references.ADDRESSES
import com.example.jooq.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(private val dsl: DSLContext) {

    data class AddressDto(val id: Int?, val country: String, val zip: String, val city: String, val street: String)
    data class UserDto(val id: Int?, val name: String, val pointsAmount: Long?, val dateOfBirth: java.time.LocalDate?, val addresses: List<AddressDto>)

    @PostMapping
    fun createUser(@RequestBody request: UserDto): UserDto {
        val user = dsl.insertInto(USERS)
            .set(USERS.NAME, request.name)
            .set(USERS.POINTS_AMOUNT, request.pointsAmount)
            .set(USERS.DATE_OF_BIRTH, request.dateOfBirth)
            .returning()
            .fetchOne()!!

        val addresses = request.addresses.map { a ->
            dsl.insertInto(ADDRESSES)
                .set(ADDRESSES.USER_ID, user.id)
                .set(ADDRESSES.COUNTRY, a.country)
                .set(ADDRESSES.ZIP, a.zip)
                .set(ADDRESSES.CITY, a.city)
                .set(ADDRESSES.STREET, a.street)
                .returning()
                .fetchOne()!!
        }

        return toDto(user, addresses)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Int): UserDto {
        val user = dsl.fetchOne(USERS, USERS.ID.eq(id))!!
        val addresses = dsl.fetch(ADDRESSES, ADDRESSES.USER_ID.eq(id))
        return toDto(user, addresses)
    }

    private fun toDto(user: UsersRecord, addresses: List<AddressesRecord>): UserDto =
        UserDto(
            id = user.id,
            name = user.name!!,
            pointsAmount = user.pointsAmount,
            dateOfBirth = user.dateOfBirth,
            addresses = addresses.map { a ->
                AddressDto(a.id, a.country!!, a.zip!!, a.city!!, a.street!!)
            }
        )
}
