package com.soprabanking.drinkplanner;

import com.soprabanking.drinkplanner.model.Beverage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BeverageRepository extends ReactiveMongoRepository<Beverage, Long> {

}
