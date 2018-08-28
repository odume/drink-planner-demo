package com.soprabanking.drinkplanner.service;

import com.soprabanking.drinkplanner.BeverageRepository;
import com.soprabanking.drinkplanner.model.Beverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BeverageService {

    private static final Logger LOG = LoggerFactory.getLogger(BeverageService.class);

    private BeverageRepository repository;

    private BeverageCache cache;

    public BeverageService(BeverageRepository repository, BeverageCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    public Mono<Beverage> save(Beverage beverage) {
        Mono<Beverage> newBeverage = Mono.just(beverage)
                .flatMap(repository::save)
                .doOnSuccess(b -> LOG.info("{} saved successfully", b.getName()));

        return Mono.just(beverage)
                .doOnNext(b -> LOG.info("Saving Beverage {}", b.getName()))
                .map(Beverage::getName)
                .flatMap(repository::findByName)
                .doOnNext(b -> LOG.info("Already exists : {}", b))
                .switchIfEmpty(newBeverage);
    }

}
