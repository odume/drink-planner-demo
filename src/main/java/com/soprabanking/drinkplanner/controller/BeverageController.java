package com.soprabanking.drinkplanner.controller;

import com.soprabanking.drinkplanner.mapper.BeverageMapper;
import com.soprabanking.drinkplanner.model.BeverageDTO;
import com.soprabanking.drinkplanner.service.BeverageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@RestController
public class BeverageController {


    private BeverageService service;

    public BeverageController(BeverageService service) {
        this.service = service;
    }

    @GetMapping("beverages")
    public Flux<BeverageDTO> allBeverages(@RequestParam(required = false, defaultValue = "0") int page,
                                          @RequestParam(required = false, defaultValue = "50") int pageSize) {
        return service.findAll(PageRequest.of(page, pageSize))
                .map(BeverageMapper::toDto)
                .delayElements(Duration.of(300, ChronoUnit.MILLIS));
    }

    @GetMapping("beverages/{id}")
    public Mono<BeverageDTO> getBeverage(@PathVariable String id) {
        return service.findOne(id)
                .map(BeverageMapper::toDto);
    }
}
