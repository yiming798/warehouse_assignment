package com.warehouse.models.tool;

import com.warehouse.exception.BrokenItemException;
import com.warehouse.models.Item;
import com.warehouse.models.weapon.Gun;

public class Toolbox extends Item {
    private int durability = 100;

    public Toolbox(String name, double weight){
        super(name, weight);
    }

    public int getDurability(){
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public void repair(Gun gun) throws BrokenItemException {
        if(durability <= 0){
            throw new BrokenItemException("❌ This weapon repair tool is completely damaged and cannot be repaired! ");
        }
        if (gun.getDurability() >= 100) {
            throw new IllegalStateException("Repair skipped: " + gun.getName() + " is not damaged.");
        }

        gun.repair();
        this.durability -= 25;
        System.out.println("Repaired using the weapon repair tool. " + gun.getName() + "The gun's durability has been restored to 100%.");
        System.out.println("Toolbox remaining durability: " + durability + "%");

        if (durability <= 0) {
            System.out.println("⚠️ This weapon repair tool is completely broken and can no longer be used.");
        }
    }
}


