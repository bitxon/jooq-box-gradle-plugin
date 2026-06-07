package com.example.mapper;

import com.example.api.Account;
import com.example.generated.jooq.tables.records.AccountRecord;
import org.springframework.stereotype.Component;

@Component
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

    public AccountRecord mapToDb(Account value) {
        if (value == null) {
            return null;
        }
        return new AccountRecord(
            value.id(),
            value.email(),
            value.firstName(),
            value.lastName(),
            value.dateOfBirth(),
            value.currency(),
            value.moneyAmount()
        );
    }
}
