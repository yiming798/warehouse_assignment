package com.warehouse.models.consumable;

import com.warehouse.exception.ExpiredItemException;
import java.time.LocalDate;

public class Food extends Consumable {
    public Food(String name, double weight, LocalDate expirationDate){
        super(name, weight, expirationDate);
    }

    public void eat() throws ExpiredItemException {
        if(isExpired()){
            throw new ExpiredItemException("❌ Can't eat " + name + " expired ");
        }
        if(isConsumed){
            throw new IllegalStateException("Can't eat " + name + ": already consumed.");
        }
        isConsumed = true;
        System.out.println("Eating " + name);
    }


}
