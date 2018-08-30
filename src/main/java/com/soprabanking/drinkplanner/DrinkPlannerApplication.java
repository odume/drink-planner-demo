package com.soprabanking.drinkplanner;

import com.soprabanking.drinkplanner.model.Beverage;
import com.soprabanking.drinkplanner.service.BeverageService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.Instant;

import static java.time.Instant.now;

@SpringBootApplication
public class DrinkPlannerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(DrinkPlannerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DrinkPlannerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initBeverages(BeverageService beverageService) {
        return args -> {
            Instant beginning = now();
            LOG.info("We start !");

            Flux<ObjectId> ids = Flux.range(0, Integer.MAX_VALUE)
                    .map(i -> ObjectId.get());

            Flux<Beverage> beverages = Flux.just(
                    Tuples.of("Chimay Bleue", 9f, 33L, true),
                    Tuples.of("Bush Ambrée", 12f, 33L, false),
                    Tuples.of("Orval", 6.5f, 33L, true),
                    Tuples.of("Vittel", 0f, 50L, false),
                    Tuples.of("Hoegaarden", 4f, 25L, false),
                    Tuples.of("Cuvée des Trolls", 7f, 25L, false))
                    .zipWith(ids, (b, id) -> new Beverage(id, b.getT1(), b.getT2(), b.getT3(), b.getT4()));

            LOG.info("Created");

            Scheduler saving = Schedulers.newElastic("saving");
            Mono<Tuple2<Long, Long>> trappistCount = beverages
                    .doOnNext(b -> LOG.info("Processing {}", b.getName()))
                    .flatMap(b -> beverageService.save(b))
                    .filter(Beverage::isTrappist)
                    .count()
                    .elapsed(); // this is a zip to a Tuple containing the elapsed time and the count from previous step

            LOG.info("Transformed");

            trappistCount
                    .subscribeOn(Schedulers.newSingle("processing"))
                    .subscribe(c ->
                    LOG.info("All created in {} ms. Number of trappist beers {}", c.getT1(), c.getT2()));

            LOG.info("Done after {} ms !", Duration.between(beginning, now()).toMillis());
        };
    }

    @Bean
    public ReactiveRedisTemplate<String, Beverage> reactiveJsonPostRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        Jackson2JsonRedisSerializer<Beverage> serializer = new Jackson2JsonRedisSerializer<>(Beverage.class);

        RedisSerializationContext<String, Beverage> serializationContext =
                RedisSerializationContext.<String, Beverage>newSerializationContext(new StringRedisSerializer())
                        .value(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
