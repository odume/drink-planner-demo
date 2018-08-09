package com.soprabanking.drinkplanner.mapper;

import com.soprabanking.drinkplanner.model.Beverage;
import com.soprabanking.drinkplanner.model.BeverageDTO;

public class BeverageMapper {

    public static Beverage toEntity(BeverageDTO dto) {
        return new Beverage(String.valueOf(dto.hashCode()), dto.getName(), dto.getAlcoholRate(), dto.getCapacity(), dto.isTrappist());
    }

    public static BeverageDTO toDto(Beverage entity) {
        return new BeverageDTO(entity.getName(), entity.getAlcoholRate(), entity.getCapacity(), entity.isTrappist());
    }

}
