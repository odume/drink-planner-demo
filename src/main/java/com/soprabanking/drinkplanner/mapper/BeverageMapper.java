package com.soprabanking.drinkplanner.mapper;

import com.soprabanking.drinkplanner.model.Beverage;
import com.soprabanking.drinkplanner.model.BeverageDTO;
import org.bson.types.ObjectId;

public class BeverageMapper {

    public static Beverage toEntity(BeverageDTO dto) {
        return new Beverage(ObjectId.get(), dto.getName(), dto.getAlcoholRate(), dto.getCapacity(), dto.isTrappist());
    }

    public static BeverageDTO toDto(Beverage entity) {
        return new BeverageDTO(entity.getName(), entity.getAlcoholRate(), entity.getCapacity(), entity.isTrappist());
    }

}
