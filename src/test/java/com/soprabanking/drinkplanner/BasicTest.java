package com.soprabanking.drinkplanner;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class BasicTest {

    private static final Logger logger = LoggerFactory.getLogger(BasicTest.class);

    @Test
    public void basic() throws InterruptedException {
        Mono<List<String>> hellos = Flux.range(1, 10)
                .flatMap(i -> transformInHello(i).subscribeOn(Schedulers.newElastic("myThreads")))
                .collectList();

        hellos.subscribe(list -> logger.info("final list : {}", list));

        Thread.sleep(10000);

    }

    private Mono<String> transformInHello(int number) {
        return Mono.just(number)
                .doOnNext(i -> logger.info("Treating {}", i))
                .map(i -> {
                    try {
                        Thread.sleep(500 + i * 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "Hello " + i;
                });
    }

}
