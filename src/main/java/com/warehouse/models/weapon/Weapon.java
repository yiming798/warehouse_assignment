package com.warehouse.models.weapon;

import com.warehouse.models.Item;
import com.warehouse.models.Usable;

public abstract class Weapon extends Item implements Usable {
    protected int durability = 100;

    public Weapon(String name, double weight){
        super(name, weight);
    }


    // abstract method to be implemented by subclasses
    public abstract void use();

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }
}
