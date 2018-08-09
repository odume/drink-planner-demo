package com.soprabanking.drinkplanner.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class BeverageDTO {

    private final @NonNull
    String name;

    private final @NonNull
    Float alcoholRate;

    private final long capacity;

    private final boolean trappist;

}
