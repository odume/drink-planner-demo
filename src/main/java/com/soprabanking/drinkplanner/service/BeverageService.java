package com.soprabanking.drinkplanner.service;

import com.soprabanking.drinkplanner.model.Beverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BeverageService {

    private static final Logger LOG = LoggerFactory.getLogger(BeverageService.class);

    public Beverage save(Beverage beverage) {
        LOG.info("Saving Beverage {}", beverage.getName());
        return beverage;
    }

}
