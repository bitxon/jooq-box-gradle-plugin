package com.example.mapper;

import com.example.api.Account;
import com.example.generated.jooq.tables.records.AccountRecord;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountMapper {

    public Account mapToApi(AccountRecord value) {
        if (value == null) {
            return null;
        }
        return new Account(
            value.getId(),
            value.getEmail(),
            value.getFirstName(),
            value.getLastName(),
            value.getDateOfBirth(),
            value.getCurrency(),
            value.getMoneyAmount()
        );
    }
}
