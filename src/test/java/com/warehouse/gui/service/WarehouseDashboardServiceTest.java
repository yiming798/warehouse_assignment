package com.warehouse.gui.service;

import com.warehouse.models.Item;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Gun;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarehouseDashboardServiceTest {

    @Test
    void filterAndSortShouldApplySearchAndSortTogether() {
        WarehouseDashboardService service = new WarehouseDashboardService();
        List<Item> items = List.of(
                new Toolbox("Toolbox", 3.0),
                new Gun("Alpha", 1.0, 5),
                new Food("Apple", 2.0, LocalDate.now().plusDays(1)),
                new Food("Banana", 4.0, LocalDate.now().plusDays(1))
        );

        List<Item> filtered = service.filterAndSort(items, "a", "Name", "Ascending");

        assertEquals(List.of("Alpha", "Apple", "Banana"),
                filtered.stream().map(Item::getName).toList());
    }

    @Test
    void snapshotShouldComputeCountsAndRepairLists() {
        WarehouseDashboardService service = new WarehouseDashboardService();
        Food expired = new Food("Expired", 1.0, LocalDate.now().minusDays(1));
        Gun gun = new Gun("Gun", 2.0, 10);
        gun.setDurability(20);
        Toolbox toolbox = new Toolbox("Kit", 1.0);
        toolbox.setDurability(15);

        WarehouseDashboardService.DashboardSnapshot snapshot = service.snapshot(List.of(expired, gun, toolbox));

        assertEquals(3, snapshot.totalItems());
        assertEquals(1, snapshot.expiredItems());
        assertEquals(2, snapshot.lowDurabilityItems());
        assertEquals(1, snapshot.weaponItems());
        assertEquals(1, snapshot.toolboxItems());
        assertEquals(List.of("Kit"), snapshot.toolboxNames());
        assertEquals(List.of("Gun"), snapshot.gunNames());
    }
}


