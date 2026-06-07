package com.example;

import com.example.jooq.Tables;
import org.jooq.DSLContext;

public class UserController {

    private final DSLContext dsl;

    public UserController(DSLContext dsl) {
        this.dsl = dsl;
    }

    public int getUserCount() {
        return dsl.fetchCount(Tables.USERS);
    }

    public int getAddressCount() {
        return dsl.fetchCount(Tables.ADDRESSES);
    }
}
