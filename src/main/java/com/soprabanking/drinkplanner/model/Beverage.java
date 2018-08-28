package com.soprabanking.drinkplanner.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Beverage {

    @Id
    @JsonIgnore
    private final @NonNull
    ObjectId id;

    @Indexed
    private final @NonNull String name;
    private final @NonNull Float alcoholRate;

    private final long capacity;
    private final boolean trappist;
}
