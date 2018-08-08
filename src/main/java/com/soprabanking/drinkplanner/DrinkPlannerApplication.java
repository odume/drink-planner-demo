package com.soprabanking.drinkplanner;

import com.soprabanking.drinkplanner.model.Beverage;
import com.soprabanking.drinkplanner.service.BeverageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@SpringBootApplication
public class DrinkPlannerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(DrinkPlannerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DrinkPlannerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initBeverages(BeverageService beverageService) {
        return args -> {
            LOG.info("We start !");

            Flux<String> ids = Flux.range(0, 10)
                    .map(String::valueOf);

            Flux<Beverage> beverages = Flux.just(
                    Tuples.of("Chimay Bleue", 9f, 33L, true),
                    Tuples.of("Bush Ambrée", 12f, 33L, false),
                    Tuples.of("Orval", 6.5f, 33L, true),
                    Tuples.of("Hoogaerden", 4f, 25L, false),
                    Tuples.of("Cuvée des Trolls", 7f, 25L, false),
                    Tuples.of("Vittel", 0f, 50L, false))
                    .zipWith(ids, (b, id) -> new Beverage(id, b.getT1(), b.getT2(), b.getT3(), b.getT4()));

            LOG.info("Created");

            Mono<Long> trappistCount = beverages.log()
                    .flatMap(b -> beverageService.save(b))
                    .filter(Beverage::isTrappist)
                    .count();

            LOG.info("Transformed");

            trappistCount.subscribe(c -> LOG.info("Number of trappist beers: {}", c));

            LOG.info("Done !");
        };
    }


}
