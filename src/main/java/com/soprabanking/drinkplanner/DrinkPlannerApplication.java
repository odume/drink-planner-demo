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

@SpringBootApplication
public class DrinkPlannerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(DrinkPlannerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DrinkPlannerApplication.class, args);
    }

    @Bean
    public CommandLineRunner startHere(BeverageService beverageService) {
        return args -> {
            LOG.info("We start !");

            Flux.just(
                    new Beverage("Chimay Bleue", 9.5f, true),
                    new Beverage("Bush Ambrée", 12f, false),
                    new Beverage("Orval", 6.5f, true),
                    new Beverage("Hoogaerden", 4f, false),
                    new Beverage("Cuvée des Trolls", 5.5f, false),
                    new Beverage("Vittel", 0f, false))
                    .log()
                    .map(beverageService::save)
                    .filter(Beverage::getTrappist)
                    .count()
                    .subscribe(c -> LOG.info("Number of trappist beers: {}", c));
        };
    }
}
