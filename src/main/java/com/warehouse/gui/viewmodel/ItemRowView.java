package com.warehouse.gui.viewmodel;

import com.warehouse.models.Item;
import com.warehouse.models.consumable.Consumable;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Bomb;
import com.warehouse.models.weapon.Gun;
import com.warehouse.models.weapon.Weapon;

import java.util.Locale;

public class ItemRowView {
    private final String type;
    private final String name;
    private final double weight;
    private final String expiration;
    private final int durability;
    private final int bullets;
    private final String status;

    public ItemRowView(Item item) {
        this.type = item.getClass().getSimpleName();
        this.name = item.getName();
        this.weight = item.getWeight();

        if (item instanceof Consumable consumable) {
            this.expiration = String.valueOf(consumable.getExpirationDate());
            if (consumable.isConsumed()) {
                if (item instanceof Food) {
                    this.status = "Consumed (Eaten)";
                } else if (item instanceof Drink) {
                    this.status = "Consumed (Drank)";
                } else {
                    this.status = "Consumed";
                }
            } else if (consumable.isExpired()) {
                this.status = "Expired";
            } else {
                this.status = "Available";
            }
        } else {
            this.expiration = "-";
            this.status = "N/A";
        }

        if (item instanceof Gun gun) {
            this.bullets = gun.getBullets();
            this.durability = gun.getDurability();
        } else if (item instanceof Bomb) {
            this.bullets = -1;
            this.durability = 100;
        } else if (item instanceof Weapon weapon) {
            this.bullets = -1;
            this.durability = weapon.getDurability();
        } else if (item instanceof Toolbox toolbox) {
            this.bullets = -1;
            this.durability = toolbox.getDurability();
        } else {
            this.bullets = -1;
            this.durability = -1;
        }
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public String getWeightDisplay() {
        return String.format(Locale.ROOT, "%.2f", weight);
    }

    public String getExpiration() {
        return expiration;
    }

    public int getDurability() {
        return durability;
    }

    public String getDurabilityDisplay() {
        return durability < 0 ? "-" : String.valueOf(durability);
    }

    public double getDurabilityProgress() {
        if (durability < 0) {
            return 0;
        }
        return Math.min(100, durability) / 100.0;
    }

    public int getBullets() {
        return bullets;
    }

    public String getBulletsDisplay() {
        return bullets < 0 ? "-" : String.valueOf(bullets);
    }

    public String getStatus() {
        return status;
    }
}


