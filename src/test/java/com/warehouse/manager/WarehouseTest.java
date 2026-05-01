package com.warehouse.manager;

import com.warehouse.exception.ItemAlreadyExistsException;
import com.warehouse.models.Item;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Gun;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseTest {

    @Test
    void addItemShouldRejectDuplicateNames() {
        Warehouse warehouse = new Warehouse();
        warehouse.addItem(new Toolbox("Shared", 1.0));

        assertThrows(ItemAlreadyExistsException.class,
                () -> warehouse.addItem(new Gun("Shared", 2.0, 5)));
    }

    @Test
    void addItemShouldEvictLightestItemWhenNoUsedItemsExist() {
        Warehouse warehouse = new Warehouse();
        for (int i = 1; i <= 10; i++) {
            warehouse.addItem(new Toolbox("Tool" + i, i));
        }

        Warehouse.CapacityAction result = warehouse.addItem(new Toolbox("Heavy", 100));

        assertEquals(10, warehouse.getItems().size());
        assertNotNull(result.removedItem());
        assertEquals(Warehouse.CapacityEvictionReason.LIGHTEST_ITEM, result.reason());
        assertEquals("Tool1", result.removedItem().getName());
        assertNull(warehouse.getItemByName("Tool1"));
        assertNotNull(warehouse.getItemByName("Heavy"));
    }

    @Test
    void addItemShouldEvictFirstUsedOrConsumedItemWhenPresent() {
        Warehouse warehouse = new Warehouse();
        Food usedFood = new Food("UsedFood", 100.0, LocalDate.now().plusDays(5));
        usedFood.setConsumed(true);
        warehouse.addItem(usedFood);
        for (int i = 1; i <= 9; i++) {
            warehouse.addItem(new Toolbox("Tool" + i, i));
        }

        Warehouse.CapacityAction result = warehouse.addItem(new Toolbox("NewItem", 0.5));

        assertEquals(10, warehouse.getItems().size());
        assertNotNull(result.removedItem());
        assertEquals(Warehouse.CapacityEvictionReason.FIRST_USED_OR_CONSUMED_ITEM, result.reason());
        assertEquals("UsedFood", result.removedItem().getName());
        assertNull(warehouse.getItemByName("UsedFood"));
        assertNotNull(warehouse.getItemByName("NewItem"));
    }

    @Test
    void removeExpiredItemsShouldOnlyRemoveExpiredConsumables() {
        Warehouse warehouse = new Warehouse();
        Food expiredFood = new Food("Expired", 1.0, LocalDate.now().minusDays(1));
        Food freshFood = new Food("Fresh", 1.0, LocalDate.now().plusDays(1));
        Gun gun = new Gun("Gun", 2.0, 3);

        warehouse.addItem(expiredFood);
        warehouse.addItem(freshFood);
        warehouse.addItem(gun);

        List<Item> removed = warehouse.removeExpiredItems();

        assertEquals(1, removed.size());
        assertEquals("Expired", removed.get(0).getName());
        assertNull(warehouse.getItemByName("Expired"));
        assertNotNull(warehouse.getItemByName("Fresh"));
        assertNotNull(warehouse.getItemByName("Gun"));
    }
}

