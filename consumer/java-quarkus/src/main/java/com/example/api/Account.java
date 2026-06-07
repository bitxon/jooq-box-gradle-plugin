package com.example.api;

import java.time.LocalDate;

public record Account(
    Integer id,
    String email,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String currency,
    Integer moneyAmount
) {}
