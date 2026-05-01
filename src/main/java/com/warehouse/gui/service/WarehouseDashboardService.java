package com.warehouse.gui.service;

import com.warehouse.models.Item;
import com.warehouse.models.consumable.Consumable;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Bomb;
import com.warehouse.models.weapon.Gun;
import com.warehouse.util.ItemSorter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WarehouseDashboardService {

    public List<Item> filterAndSort(List<Item> items, String search, String sortField, String sortOrder) {
        List<Item> filtered = applySearch(items, search);
        List<Item> sorted = new ArrayList<>(filtered);
        ItemSorter.sortItems(sorted, mapSortCriteria(sortField), mapSortOrder(sortOrder));
        return sorted;
    }

    public DashboardSnapshot snapshot(List<Item> items) {
        long totalItems = items.size();
        double totalWeight = items.stream().mapToDouble(Item::getWeight).sum();
        long foodItems = items.stream().filter(Food.class::isInstance).count();
        long drinkItems = items.stream().filter(Drink.class::isInstance).count();
        long gunItems = items.stream().filter(Gun.class::isInstance).count();
        long bombItems = items.stream().filter(Bomb.class::isInstance).count();
        long toolboxes = items.stream().filter(Toolbox.class::isInstance).count();
        long expired = items.stream().filter(item -> item instanceof Consumable consumable && consumable.isExpired()).count();
        long lowDurability = items.stream().filter(this::isLowDurability).count();
        long consumables = foodItems + drinkItems;
        long weapons = gunItems + bombItems;
        List<String> toolboxNames = items.stream()
                .filter(Toolbox.class::isInstance)
                .map(Item::getName)
                .toList();
        List<String> gunNames = items.stream()
                .filter(Gun.class::isInstance)
                .map(Item::getName)
                .toList();

        return new DashboardSnapshot(
                totalItems,
                totalWeight,
                expired,
                lowDurability,
                foodItems,
                drinkItems,
                gunItems,
                bombItems,
                consumables,
                weapons,
                toolboxes,
                toolboxNames,
                gunNames
        );
    }

    private List<Item> applySearch(List<Item> items, String search) {
        String currentSearch = safe(search);
        if (currentSearch.isBlank()) {
            return items;
        }
        String keyword = currentSearch.toLowerCase(Locale.ROOT);
        return items.stream()
                .filter(item -> item.getName() != null && item.getName().toLowerCase(Locale.ROOT).contains(keyword))
                .toList();
    }

    private String mapSortCriteria(String field) {
        if (field == null) {
            return "1";
        }
        return switch (field) {
            case "Name" -> "2";
            case "Type" -> "3";
            default -> "1";
        };
    }

    private String mapSortOrder(String order) {
        if (order == null) {
            return "1";
        }
        return ("Descending".equalsIgnoreCase(order) || "Desc".equalsIgnoreCase(order)) ? "2" : "1";
    }

    private boolean isLowDurability(Item item) {
        if (item instanceof Gun gun) {
            return gun.getDurability() > 0 && gun.getDurability() <= 25;
        }
        if (item instanceof Toolbox toolbox) {
            return toolbox.getDurability() > 0 && toolbox.getDurability() <= 25;
        }
        return false;
    }

    private String safe(String text) {
        return text == null ? "" : text.trim();
    }

    public record DashboardSnapshot(
            long totalItems,
            double totalWeight,
            long expiredItems,
            long lowDurabilityItems,
            long foodItems,
            long drinkItems,
            long gunItems,
            long bombItems,
            long consumableItems,
            long weaponItems,
            long toolboxItems,
            List<String> toolboxNames,
            List<String> gunNames
    ) {
    }
}


