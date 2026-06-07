package com.example;

import com.example.jooq.Tables;
import com.example.jooq.tables.records.AddressesRecord;
import com.example.jooq.tables.records.UsersRecord;
import java.math.BigInteger;
import java.sql.Date;
import java.util.List;
import org.jooq.DSLContext;

public class UserController {

    public record AddressDto(Integer id, String country, String zip, String city, String street) {}
    public record UserDto(Integer id, String name, BigInteger pointsAmount, Date dateOfBirth, List<AddressDto> addresses) {}

    private final DSLContext dsl;

    public UserController(DSLContext dsl) {
        this.dsl = dsl;
    }

    public UserDto createUser(UserDto request) {
        UsersRecord user = dsl.insertInto(Tables.USERS)
            .set(Tables.USERS.NAME, request.name())
            .set(Tables.USERS.POINTS_AMOUNT, request.pointsAmount())
            .set(Tables.USERS.DATE_OF_BIRTH, request.dateOfBirth())
            .returning()
            .fetchOne();

        List<AddressesRecord> addresses = request.addresses().stream()
            .map(a -> dsl.insertInto(Tables.ADDRESSES)
                .set(Tables.ADDRESSES.USER_ID, user.getId())
                .set(Tables.ADDRESSES.COUNTRY, a.country())
                .set(Tables.ADDRESSES.ZIP, a.zip())
                .set(Tables.ADDRESSES.CITY, a.city())
                .set(Tables.ADDRESSES.STREET, a.street())
                .returning()
                .fetchOne())
            .toList();

        return toDto(user, addresses);
    }

    public UserDto getUser(int id) {
        UsersRecord user = dsl.fetchOne(Tables.USERS, Tables.USERS.ID.eq(id));
        List<AddressesRecord> addresses = dsl.fetch(Tables.ADDRESSES, Tables.ADDRESSES.USER_ID.eq(id));
        return toDto(user, addresses);
    }

    private UserDto toDto(UsersRecord user, List<AddressesRecord> addresses) {
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getPointsAmount(),
            user.getDateOfBirth(),
            addresses.stream()
                .map(a -> new AddressDto(a.getId(), a.getCountry(), a.getZip(), a.getCity(), a.getStreet()))
                .toList()
        );
    }
}
