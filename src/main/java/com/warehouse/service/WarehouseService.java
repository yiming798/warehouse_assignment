package com.warehouse.service;

import com.warehouse.manager.Warehouse;
import com.warehouse.models.Item;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Gun;
import com.warehouse.models.weapon.Weapon;
import com.warehouse.util.FileManager;
import com.warehouse.util.ItemSorter;

import java.util.ArrayList;
import java.util.List;

public class WarehouseService {
	private final Warehouse warehouse;

	public WarehouseService(Warehouse warehouse) {
		this.warehouse = warehouse;
	}

	public void loadFromDisk() {
		List<Item> loaded = FileManager.loadFromFile();
		for (Item item : loaded) {
			try {
				warehouse.addItem(item);
			} catch (RuntimeException ignored) {
				// Skip duplicates or invalid state when loading historical data.
			}
		}
	}

	public void saveToDisk() {
		FileManager.saveToFile(warehouse.getItems());
	}

	public List<Item> getAllItems() {
		return warehouse.getItems();
	}

	public int getCurrentUsage() {
		return warehouse.getItems().size();
	}

	public int getMaxCapacity() {
		return warehouse.getMaxCapacity();
	}

	public void addItem(Item item) {
		warehouse.addItem(item);
	}

	public AddItemResult addItemWithFeedback(Item item) {
		Warehouse.CapacityAction capacityAction = warehouse.addItem(item);
		return new AddItemResult(item, capacityAction);
	}

	public boolean removeByName(String name) {
		return warehouse.removeItemByName(name);
	}

	public Item searchByName(String name) {
		return warehouse.getItemByName(name);
	}

	public List<Item> sortItems(String criteria, String order) {
		List<Item> sorted = new ArrayList<>(warehouse.getItems());
		ItemSorter.sortItems(sorted, criteria, order);
		return sorted;
	}

	public void useItem(String name) {
		Item item = requireItem(name);
		if (item instanceof Weapon weapon) {
			weapon.use();
			return;
		}
		throw new IllegalArgumentException("Use only applies to weapon items (Gun/Bomb): " + name);
	}

	public void eatFood(String name) {
		Item item = requireItem(name);
		if (!(item instanceof Food food)) {
			throw new IllegalArgumentException("Not a food item: " + name);
		}
		food.eat();
	}

	public void drink(String name) {
		Item item = requireItem(name);
		if (!(item instanceof Drink drink)) {
			throw new IllegalArgumentException("Not a drink item: " + name);
		}
		drink.drink();
	}

	public void repair(String toolboxName, String gunName) {
		Item toolboxItem = requireItem(toolboxName);
		Item gunItem = requireItem(gunName);

		if (!(toolboxItem instanceof Toolbox toolbox)) {
			throw new IllegalArgumentException("Toolbox not found: " + toolboxName);
		}
		if (!(gunItem instanceof Gun gun)) {
			throw new IllegalArgumentException("Gun not found: " + gunName);
		}

		toolbox.repair(gun);
		if (toolbox.getDurability() <= 0) {
			warehouse.removeItemByName(toolbox.getName());
		}
	}

	private Item requireItem(String name) {
		Item item = warehouse.getItemByName(name);
		if (item == null) {
			throw new IllegalArgumentException("Item not found: " + name);
		}
		return item;
	}

	public record AddItemResult(Item addedItem, Warehouse.CapacityAction capacityAction) {
		public boolean hasCapacityReplacement() {
			return capacityAction != null && capacityAction.hasRemoval();
		}

		public Item removedByCapacity() {
			return capacityAction == null ? null : capacityAction.removedItem();
		}

		public Warehouse.CapacityEvictionReason evictionReason() {
			return capacityAction == null ? null : capacityAction.reason();
		}
	}
}
