package com.warehouse.models.consumable;

import com.warehouse.exception.ExpiredItemException;

import java.time.LocalDate;

public class Drink extends Consumable {
    public Drink(String name, double weight, LocalDate expirationDate){
        super(name, weight, expirationDate);
    }

    public void drink(){
        if (isExpired()) {
            throw new ExpiredItemException("Can't drink " + name + ": expired.");
        }
        if (isConsumed) {
            throw new IllegalStateException("Can't drink " + name + ": already consumed.");
        }
        isConsumed = true;
        System.out.println("Drinking " + name);
    }



}
