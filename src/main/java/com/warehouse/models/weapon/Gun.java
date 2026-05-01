package com.warehouse.models.weapon;


import com.warehouse.exception.BrokenItemException;
import com.warehouse.exception.NoBulletsException;

public class Gun extends Weapon {
    private int bullets;


    public Gun(String name, double weight, int bullets) {
        super(name, weight);
        this.bullets = bullets;
    }

    public int getBullets() {
        return bullets;
    }

    public void repair(){
        this.durability = 100;
    }

    public void setBullets(int bullets) {
        this.bullets = bullets;
    }

    @Override
    public void use() throws NoBulletsException, BrokenItemException {
        if(durability <= 0){
            throw new BrokenItemException("The " + name + " is broken! Removed! ");
        }
        if(bullets <= 0){
            throw new NoBulletsException("Kada! " + name + " No bullets!");
        }
        System.out.println("Bang");
        bullets--;
        durability -= 10;

    }
}

