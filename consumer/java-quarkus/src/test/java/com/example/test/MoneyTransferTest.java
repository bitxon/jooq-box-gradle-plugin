package com.example.test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.example.api.Constants;
import com.example.api.MoneyTransfer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
@QuarkusTestResource(PostgresTestResourceLifecycleManager.class)
class MoneyTransferTest {

    @ParameterizedTest
    @CsvSource({
        "USD -> USD, 1, 2, 40, 300, 613"
    })
    void transfer(String description, int senderId, int recipientId, int transferAmount,
                  int expectedSenderMoney, int expectedRecipientMoney) {

        var requestBody = new MoneyTransfer(senderId, recipientId, transferAmount);

        //@formatter:off
        given()
            .body(requestBody)
            .contentType(ContentType.JSON)
        .when()
            .post("/accounts/transfers")
        .then()
            .statusCode(204);

        get("/accounts/" + senderId).then()
            .assertThat().body("moneyAmount", equalTo(expectedSenderMoney));
        get("/accounts/" + recipientId).then()
            .assertThat().body("moneyAmount", equalTo(expectedRecipientMoney));
        //@formatter:on
    }

    @Test
    void transferWithServerProblemDuringTransfer() {
        var transferAmount = 40;
        var senderId = 3;
        var senderMoneyAmountOriginal = 79;
        var recipientId = 4;
        var recipientMoneyAmountOriginal = 33;

        var requestBody = new MoneyTransfer(senderId, recipientId, transferAmount);

        //@formatter:off
        given()
            .body(requestBody)
            .header(Constants.DIRTY_TRICK_HEADER, Constants.DirtyTrick.FAIL_TRANSFER)
            .contentType(ContentType.JSON)
        .when()
            .post("/accounts/transfers")
        .then()
            .statusCode(500);
        //@formatter:on

        get("/accounts/" + senderId).then()
            .body("moneyAmount", is(senderMoneyAmountOriginal));
        get("/accounts/" + recipientId).then()
            .body("moneyAmount", is(recipientMoneyAmountOriginal));
    }
}
