package com.example.db;

import static com.example.generated.jooq.Tables.ACCOUNT;

import com.example.generated.jooq.tables.records.AccountRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.jooq.DSLContext;


@ApplicationScoped
public class AccountDao {

    @Inject
    DSLContext dslContext;

    public Optional<AccountRecord> findById(Integer id) {
        return dslContext.selectFrom(ACCOUNT)
            .where(ACCOUNT.ID.eq(id))
            .fetchOptional();
    }

    public void updateMoneyAmount(Integer id, Integer newMoneyAmount) {
        dslContext.update(ACCOUNT)
            .set(ACCOUNT.MONEY_AMOUNT, newMoneyAmount)
            .where(ACCOUNT.ID.eq(id))
            .execute();
    }
}
