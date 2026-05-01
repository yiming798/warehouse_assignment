package com.warehouse.service;

import com.warehouse.manager.Warehouse;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Gun;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseServiceTest {

    @Test
    void shouldUseEatDrinkAndRepairItems() {
        Warehouse warehouse = new Warehouse();
        WarehouseService service = new WarehouseService(warehouse);

        Gun gun = new Gun("Pistol", 2.0, 2);
        gun.setDurability(40);
        Food food = new Food("Apple", 1.0, LocalDate.now().plusDays(2));
        Drink drink = new Drink("Juice", 1.0, LocalDate.now().plusDays(2));
        Toolbox toolbox = new Toolbox("RepairKit", 1.0);

        warehouse.addItem(gun);
        warehouse.addItem(food);
        warehouse.addItem(drink);
        warehouse.addItem(toolbox);

        service.useItem("Pistol");
        assertEquals(1, gun.getBullets());
        assertEquals(30, gun.getDurability());

        service.eatFood("Apple");
        assertTrue(food.isConsumed());
        assertThrows(IllegalStateException.class, food::eat);

        service.drink("Juice");
        assertTrue(drink.isConsumed());
        assertThrows(IllegalStateException.class, drink::drink);

        assertThrows(IllegalArgumentException.class, () -> service.useItem("Apple"));
        assertThrows(IllegalArgumentException.class, () -> service.useItem("Juice"));

        service.repair("RepairKit", "Pistol");
        assertEquals(100, gun.getDurability());
        assertEquals(75, toolbox.getDurability());
    }

    @Test
    void removeByNameShouldReturnTrueWhenItemExists() {
        Warehouse warehouse = new Warehouse();
        WarehouseService service = new WarehouseService(warehouse);
        warehouse.addItem(new Toolbox("Box", 1.0));

        assertTrue(service.removeByName("Box"));
        assertFalse(service.removeByName("Box"));
    }

    @Test
    void addItemWithFeedbackShouldExposeEvictedItemWhenCapacityIsFull() {
        Warehouse warehouse = new Warehouse();
        WarehouseService service = new WarehouseService(warehouse);
        for (int i = 1; i <= 10; i++) {
            warehouse.addItem(new Toolbox("Tool" + i, i));
        }

        WarehouseService.AddItemResult result = service.addItemWithFeedback(new Toolbox("Heavy", 99.0));

        assertTrue(result.hasCapacityReplacement());
        assertEquals("Tool1", result.removedByCapacity().getName());
        assertEquals(Warehouse.CapacityEvictionReason.LIGHTEST_ITEM, result.evictionReason());
        assertEquals("Heavy", result.addedItem().getName());
    }
}

