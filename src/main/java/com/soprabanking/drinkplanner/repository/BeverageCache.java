package com.soprabanking.drinkplanner.repository;

import com.soprabanking.drinkplanner.model.Beverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class BeverageCache {

    private static final Logger LOG = LoggerFactory.getLogger(BeverageCache.class);

    private final ReactiveRedisOperations<String, Beverage> beverageOps;

    public BeverageCache(ReactiveRedisOperations<String, Beverage> beverageOps) {
        this.beverageOps = beverageOps;
    }

    public Mono<Beverage> put(Beverage beverage) {
        return beverageOps.opsForValue()
                .set(beverage.getId().toHexString(), beverage)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new RuntimeException("Failed to cache")))
                .map(b -> beverage)
                .doOnSubscribe(s -> LOG.info("Trying to add {} to the cache", beverage.getName()))
                .doOnSuccess(b -> LOG.info("Added {} to the cache", b.getName()));
    }

    public Mono<Beverage> get(String key) {
        return beverageOps.opsForValue().get(key)
                .doOnSubscribe(s -> LOG.info("Trying to get beverage with id {} from cache", key))
                .doOnNext(b -> LOG.info("Got beverage {} from cache", b.getName()));
    }
}
