package com.soprabanking.drinkplanner.repository;

import com.soprabanking.drinkplanner.model.Beverage;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BeverageRepository extends ReactiveMongoRepository<Beverage, ObjectId> {

}
