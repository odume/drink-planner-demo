package com.soprabanking.drinkplanner.model;


import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Beverage {

    @Id
    private final @NonNull
    String id;

    private final @NonNull String name;
    private final @NonNull Float alcoholRate;

    private final long capacity;
    private final boolean trappist;
}
