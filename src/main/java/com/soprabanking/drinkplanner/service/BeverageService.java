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
                .doOnNext(b -> tasteBlocking(b))
//                .flatMap(b -> tasteAsynchronously(b))
                .doOnNext(b -> LOG.info("Now Saving Beverage {}", b.getName()))
                .map(Beverage::getName)
                .flatMap(repository::findByName)
                .doOnNext(b -> LOG.info("Already exists : {}", b))
                .switchIfEmpty(newBeverage);
    }

    private void tasteBlocking(Beverage beverage) {
        LOG.info("Tasting {}...", beverage.getName());
        try {
            Thread.sleep(beverage.getAlcoholRate().longValue() * 100);
        } catch (InterruptedException e) {
            throw new RuntimeException("Someting whent wrong while tasting", e);
        }
        LOG.info("{} is tasted ok", beverage.getName());
    }

    private Mono<Beverage> tasteAsynchronously(Beverage beverage) {
        return Mono.just(beverage)
                .doOnNext(b -> LOG.info("Tasting {}...", b.getName()))
                .doOnNext(b -> {
                    try {
                        Thread.sleep(beverage.getAlcoholRate().longValue() * 100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Someting whent wrong while tasting", e);
                    }
                })
//                .delayElement(Duration.of(beverage.getAlcoholRate().longValue() * 100, ChronoUnit.MILLIS))
                .doOnNext(b -> LOG.info("{} is tasted ok", b.getName()));
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
