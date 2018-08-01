package com.soprabanking.drinkplanner.model;


import lombok.Data;
import lombok.NonNull;

@Data
public class Beverage {

    private final @NonNull String name;
    private final @NonNull Float alcoholRate;

    private final @NonNull
    boolean trappist;
}
