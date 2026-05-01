package com.warehouse.models.weapon;

import com.warehouse.exception.BrokenItemException;

public class Bomb extends Weapon {
    private boolean isUsed = false;

    public Bomb(String name, double weight) {
        super(name, weight);
    }

    @Override
    public void use() {
        if (!isUsed) {
            System.out.println("Boom");
            isUsed = true;
        } else {
            throw new BrokenItemException(name + " has already exploded and cannot be used again.");
        }
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}

