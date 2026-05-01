package com.warehouse.models.consumable;

import com.warehouse.models.Item;

import java.time.LocalDate;

public abstract class Consumable extends Item {
    protected LocalDate expirationDate;
    protected boolean isConsumed = false;

    public Consumable(String name, double weight, LocalDate expirationDate){
        super(name, weight);
        this.expirationDate = expirationDate;
    }

    // out of date
    public boolean isExpired(){
        return LocalDate.now().isAfter(expirationDate);
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public boolean isConsumed() {
        return isConsumed;
    }

    public void setConsumed(boolean consumed) {
        isConsumed = consumed;
    }
}
