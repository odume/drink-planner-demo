package com.soprabanking.drinkplanner.controller;

import com.soprabanking.drinkplanner.BeverageRepository;
import com.soprabanking.drinkplanner.mapper.BeverageMapper;
import com.soprabanking.drinkplanner.model.BeverageDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@RestController
public class BeverageController {


    private BeverageRepository repository;

    public BeverageController(BeverageRepository repository) {
        this.repository = repository;
    }

    @GetMapping("beverages")
    public Flux<BeverageDTO> allBeverages() {
        return repository.findAll()
                .map(BeverageMapper::toDto)
                .delayElements(Duration.of(300, ChronoUnit.MILLIS));
    }
}
