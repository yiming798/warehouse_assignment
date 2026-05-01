package com.warehouse.util;

import com.warehouse.models.Item;
import com.warehouse.models.consumable.Consumable;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Bomb;
import com.warehouse.models.weapon.Gun;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
	private static final String FILE_PATH = "warehouse.txt";
	private static final String EXPIRED_LOG_PATH = "expired-removals.log";
	private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static void saveToFile(List<Item> items) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
			for (Item item : items) {
				writer.write(serialize(item));
				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to save warehouse data: " + e.getMessage(), e);
		}
	}

	public static List<Item> loadFromFile() {
		List<Item> loadedItems = new ArrayList<>();
		File file = new File(FILE_PATH);

		if (!file.exists()) {
			return loadedItems;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				try {
					Item item = deserialize(line);
					if (item != null) {
						loadedItems.add(item);
					}
				} catch (Exception lineException) {
					// Skip corrupted lines silently in GUI mode.
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to load warehouse data: " + e.getMessage(), e);
		}

		return loadedItems;
	}

	public static void appendExpiredRemovalLog(List<Item> removedItems) {
		if (removedItems == null || removedItems.isEmpty()) {
			return;
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(EXPIRED_LOG_PATH, true))) {
			String now = LocalDateTime.now().format(LOG_TIME_FORMAT);
			for (Item item : removedItems) {
				writer.write(serializeExpiredRemoval(now, item));
				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to write expired removal log: " + e.getMessage(), e);
		}
	}

	private static String serialize(Item item) {
		StringBuilder line = new StringBuilder();
		line.append(item.getClass().getSimpleName())
				.append(',').append(escape(item.getName()))
				.append(',').append(item.getWeight());

		if (item instanceof Food food) {
			line.append(',').append(food.getExpirationDate())
					.append(',').append(food.isConsumed());
		} else if (item instanceof Drink drink) {
			line.append(',').append(drink.getExpirationDate())
					.append(',').append(drink.isConsumed());
		} else if (item instanceof Gun gun) {
			line.append(',').append(gun.getBullets())
					.append(',').append(gun.getDurability());
		} else if (item instanceof Bomb bomb) {
			line.append(',').append(bomb.isUsed());
		} else if (item instanceof Toolbox toolbox) {
			line.append(',').append(toolbox.getDurability());
		}

		return line.toString();
	}

	private static String serializeExpiredRemoval(String timestamp, Item item) {
		StringBuilder line = new StringBuilder();
		line.append(timestamp)
				.append(" | name=").append(item.getName())
				.append(" | type=").append(item.getClass().getSimpleName())
				.append(" | weight=").append(item.getWeight());

		if (item instanceof Consumable consumable) {
			line.append(" | expiration=").append(consumable.getExpirationDate());
		}

		return line.toString();
	}

	private static Item deserialize(String line) {
		String[] parts = splitEscaped(line);
		if (parts.length < 3) {
			return null;
		}

		String type = parts[0];
		String name = unescape(parts[1]);
		double weight = Double.parseDouble(parts[2]);

		switch (type) {
			case "Food": {
				if (parts.length < 4) {
					return null;
				}
				Food food = new Food(name, weight, LocalDate.parse(parts[3]));
				if (parts.length >= 5) {
					food.setConsumed(Boolean.parseBoolean(parts[4]));
				}
				return food;
			}
			case "Drink": {
				if (parts.length < 4) {
					return null;
				}
				Drink drink = new Drink(name, weight, LocalDate.parse(parts[3]));
				if (parts.length >= 5) {
					drink.setConsumed(Boolean.parseBoolean(parts[4]));
				}
				return drink;
			}
			case "Gun": {
				if (parts.length < 4) {
					return null;
				}
				Gun gun = new Gun(name, weight, Integer.parseInt(parts[3]));
				if (parts.length >= 5) {
					gun.setDurability(Integer.parseInt(parts[4]));
				}
				return gun;
			}
			case "Bomb": {
				Bomb bomb = new Bomb(name, weight);
				if (parts.length >= 4) {
					bomb.setUsed(Boolean.parseBoolean(parts[3]));
				}
				return bomb;
			}
			case "Toolbox":
			case "GunRepair": {
				Toolbox toolbox = new Toolbox(name, weight);
				if (parts.length >= 4) {
					toolbox.setDurability(Integer.parseInt(parts[3]));
				}
				return toolbox;
			}
			default:
				return null;
		}
	}

	private static String escape(String value) {
		return value.replace("\\", "\\\\").replace(",", "\\,");
	}

	private static String unescape(String value) {
		StringBuilder result = new StringBuilder();
		boolean escaping = false;
		for (char c : value.toCharArray()) {
			if (escaping) {
				result.append(c);
				escaping = false;
			} else if (c == '\\') {
				escaping = true;
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private static String[] splitEscaped(String line) {
		List<String> parts = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean escaping = false;

		for (char c : line.toCharArray()) {
			if (escaping) {
				current.append(c);
				escaping = false;
			} else if (c == '\\') {
				escaping = true;
			} else if (c == ',') {
				parts.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}

		parts.add(current.toString());
		return parts.toArray(new String[0]);
	}
}
