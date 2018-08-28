package com.soprabanking.drinkplanner.service;

import com.soprabanking.drinkplanner.model.Beverage;
import com.soprabanking.drinkplanner.repository.BeverageCache;
import com.soprabanking.drinkplanner.repository.BeverageRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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

    public Mono<Beverage> findOne(String id) {
        Mono<Beverage> fromCache = Mono.just(id)
                .flatMap(cache::get);

        Mono<Beverage> fromDatabase = Mono.just(id)
                .map(ObjectId::new)
                .flatMap(repository::findById)
                .doOnNext(b -> LOG.info("Got beverage {} from database", b.getName()))
                .flatMap(cache::put);

        return Flux.concat(fromCache, fromDatabase).next();
    }

    public Flux<Beverage> findAll(Pageable page) {
        return repository.findAll()
                .skip(page.getOffset())
                .take(page.getPageSize());
    }

}
