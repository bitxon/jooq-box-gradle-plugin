package com.example.api;

public record MoneyTransfer(
    Integer senderId,
    Integer recipientId,
    Integer moneyAmount
) {}
