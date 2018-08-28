package com.soprabanking.drinkplanner.repository;

import com.soprabanking.drinkplanner.model.Beverage;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface BeverageRepository extends ReactiveMongoRepository<Beverage, ObjectId> {

    Mono<Beverage> findByName(String name);
}
