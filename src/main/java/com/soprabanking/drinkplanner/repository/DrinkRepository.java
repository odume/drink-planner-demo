package com.soprabanking.drinkplanner.repository;

import com.soprabanking.drinkplanner.model.Drink;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DrinkRepository extends ReactiveMongoRepository<Drink, ObjectId> {
}
