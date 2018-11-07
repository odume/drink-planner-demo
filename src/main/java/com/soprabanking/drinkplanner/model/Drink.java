package com.soprabanking.drinkplanner.model;

import lombok.Value;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Value
@Document
public class Drink {

    @Id
    @NotNull
    private final ObjectId id;
    @NotBlank
    private final String name;
    @NotNull
    private final LocalDate date;
    @NotNull
    private final Map<Beverage, Integer> beverageStock;
    @NotNull
    private final Person organiser;
    @NotNull
    private final List<Person> participants;

}
