package com.warehouse.util;

import com.warehouse.models.Item;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Bomb;
import com.warehouse.models.weapon.Gun;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemSorterTest {

    @Test
    void shouldSortByWeightAscendingAndDescending() {
        List<Item> items = new ArrayList<>(List.of(
                new Toolbox("C", 3.0),
                new Gun("A", 1.0, 5),
                new Food("B", 2.0, LocalDate.now().plusDays(1))
        ));

        ItemSorter.sortItems(items, "1", "1");
        assertEquals(List.of("A", "B", "C"), names(items));

        ItemSorter.sortItems(items, "1", "2");
        assertEquals(List.of("C", "B", "A"), names(items));
    }

    @Test
    void shouldSortByNameAndType() {
        List<Item> items = new ArrayList<>(List.of(
                new Toolbox("Toolbox", 3.0),
                new Bomb("Bomb", 1.0),
                new Gun("Gun", 2.0, 10),
                new Drink("Drink", 2.5, LocalDate.now().plusDays(1)),
                new Food("Food", 1.5, LocalDate.now().plusDays(1))
        ));

        ItemSorter.sortItems(items, "2", "1");
        assertEquals(List.of("Bomb", "Drink", "Food", "Gun", "Toolbox"), names(items));

        ItemSorter.sortItems(items, "3", "1");
        assertEquals(List.of("Food", "Drink", "Bomb", "Gun", "Toolbox"), names(items));
    }

    private List<String> names(List<Item> items) {
        return items.stream().map(Item::getName).collect(Collectors.toList());
    }
}

