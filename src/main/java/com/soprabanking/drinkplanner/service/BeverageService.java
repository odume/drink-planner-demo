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

    public BeverageService(BeverageRepository repository) {
        this.repository = repository;
    }

    public Mono<Beverage> save(Beverage beverage) {
        return Mono.just(beverage)
                .doOnNext(b -> LOG.info("Saving Beverage {}", b.getName()))
                .flatMap(repository::save)
                .doOnSuccess(b -> LOG.info("{} saved successfully", b.getName()));
    }

}
