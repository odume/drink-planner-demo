package com.soprabanking.drinkplanner;

import com.soprabanking.drinkplanner.model.Beverage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BeverageRepository extends MongoRepository<Beverage, Long> {

}
