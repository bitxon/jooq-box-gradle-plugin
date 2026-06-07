package com.example;

import com.example.jooq.tables.Addresses;
import com.example.jooq.tables.Users;

public class Application {
    public static void main(String[] args) {
        System.out.println(Users.USERS);
        System.out.println(Addresses.ADDRESSES);
    }
}
