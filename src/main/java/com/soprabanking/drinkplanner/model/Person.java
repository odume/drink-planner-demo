package com.soprabanking.drinkplanner.model;

import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Value
public class Person {

    @NotBlank
    private final String name;
    @NotBlank
    private final String firstname;
    @Positive
    @Max(15)
    private final BigDecimal maxAlcoholLevel;
}
