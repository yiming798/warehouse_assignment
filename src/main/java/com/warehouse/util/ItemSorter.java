package com.warehouse.util;

import com.warehouse.models.Item;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Bomb;
import com.warehouse.models.weapon.Gun;

import java.util.List;

public class ItemSorter {

    // static function sortItems
    public static void sortItems(List<Item> items, String criteria, String order) {
        int size = items.size();
        for (int i = 0; i < size - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < size - 1 - i; j++) {
                int compareResult = compareByCriteria(items.get(j), items.get(j + 1), criteria, order);
                if (compareResult > 0) {
                    Item temp = items.get(j);
                    items.set(j, items.get(j + 1));
                    items.set(j + 1, temp);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    private static int compareByCriteria(Item o1, Item o2, String criteria, String order) {
        int result;

        if ("1".equals(criteria)) {
            // Weight sort relies on Item.compareTo (Comparable<Item>) as required by rubric.
            result = o1.compareTo(o2);
        } else if ("2".equals(criteria)) {
            result = o1.getName().compareToIgnoreCase(o2.getName());
        } else if ("3".equals(criteria)) {
            result = Integer.compare(getTypeValue(o1), getTypeValue(o2));
        } else {
            result = o1.compareTo(o2);
        }

        return "2".equals(order) ? -result : result;
    }


    private static int getTypeValue(Item item) {
        if (item instanceof Food) return 1;
        if (item instanceof Drink) return 2;
        if (item instanceof Gun || item instanceof Bomb) return 3;
        if (item instanceof Toolbox) return 4;
        return 5;
    }
}