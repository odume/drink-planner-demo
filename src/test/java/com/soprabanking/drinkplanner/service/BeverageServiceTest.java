package com.soprabanking.drinkplanner.service;

import com.soprabanking.drinkplanner.model.Beverage;
import com.soprabanking.drinkplanner.repository.BeverageRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BeverageServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(BeverageServiceTest.class);

    @Autowired
    private BeverageService service;

    @Autowired
    private BeverageRepository repo;

    @Test
    public void save() {
    }

    @Test
    public void find() {

        Flux<Beverage> beverageFlux = repo.findAll()
                .doOnSubscribe(s -> LOG.info("Getting all beverages and findOne them"))
                .map(Beverage::getId)
                .map(ObjectId::toHexString)
                .doOnNext(id -> LOG.info("Try to findOne beverage with Id {}", id))
                .flatMap(service::findOne);

        StepVerifier.create(beverageFlux)
                .expectSubscription()
                .expectNextCount(6)
                .verifyComplete();
    }
}