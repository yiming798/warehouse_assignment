package com.warehouse.manager;

import com.warehouse.exception.ItemAlreadyExistsException;
import com.warehouse.models.Item;
import com.warehouse.models.consumable.Consumable;
import com.warehouse.models.consumable.Drink;
import com.warehouse.models.consumable.Food;
import com.warehouse.models.tool.Toolbox;
import com.warehouse.models.weapon.Bomb;
import com.warehouse.models.weapon.Gun;

import java.util.*;


public class Warehouse {
    private final List<Item> items;
    private static final int MAX_CAPACITY = 10;
    private final Object lock = new Object();

    public Warehouse(){
        this.items = new ArrayList<>();
    }

    //sort
    public void sortItems(){
        synchronized (lock) {
            Collections.sort(items);
        }
    }

    public List<Item> getItems(){
        synchronized (lock) {
            return new ArrayList<>(items);
        }
    }

    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }

    public Item getItemByName(String name){
        synchronized (lock) {
            return getItemByNameUnsafe(name);
        }
    }

    private Item getItemByNameUnsafe(String name) {
        for(Item item : items) {
            if( item.getName().equalsIgnoreCase(name)){
                return item;
            }
        }
        return null;
    }

    public CapacityAction addItem(Item item) throws ItemAlreadyExistsException {
        synchronized (lock) {
            //1. check
            if(getItemByNameUnsafe(item.getName()) != null) throw new ItemAlreadyExistsException("Already exists!");

            Item removedByCapacity = null;
            CapacityEvictionReason evictionReason = null;

            //2. capacity: remove first already-used/consumed item, otherwise the lightest item
            if(items.size() >= MAX_CAPACITY) {
                Item alreadyUsedItem = findFirstUsedOrConsumedItemUnsafe();
                if (alreadyUsedItem != null) {
                    items.remove(alreadyUsedItem);
                    removedByCapacity = alreadyUsedItem;
                    evictionReason = CapacityEvictionReason.FIRST_USED_OR_CONSUMED_ITEM;
                } else {
                    PriorityQueue<Item> removalQueue = new PriorityQueue<>(Comparator.comparingDouble(Item::getWeight));
                    removalQueue.addAll(items);

                    Item leastValuable = removalQueue.poll();
                    if (leastValuable != null) {
                        items.remove(leastValuable);
                        removedByCapacity = leastValuable;
                        evictionReason = CapacityEvictionReason.LIGHTEST_ITEM;
                    }
                }
            }
            items.add(item);
            return new CapacityAction(removedByCapacity, evictionReason);
        }
    }

    private Item findFirstUsedOrConsumedItemUnsafe() {
        for (Item item : items) {
            if (isUsedOrConsumed(item)) {
                return item;
            }
        }
        return null;
    }

    private boolean isUsedOrConsumed(Item item) {
        if (item instanceof Consumable consumable) {
            return consumable.isConsumed();
        }
        if (item instanceof Bomb bomb) {
            return bomb.isUsed();
        }
        if (item instanceof Gun gun) {
            return gun.getDurability() < 100;
        }
        if (item instanceof Toolbox toolbox) {
            return toolbox.getDurability() < 100;
        }
        return false;
    }

    public enum CapacityEvictionReason {
        FIRST_USED_OR_CONSUMED_ITEM,
        LIGHTEST_ITEM
    }

    public record CapacityAction(Item removedItem, CapacityEvictionReason reason) {
        public boolean hasRemoval() {
            return removedItem != null;
        }
    }

    public boolean removeItemByName(String name) {
        synchronized (lock) {
            Item target = getItemByNameUnsafe(name);
            if(target != null){
                items.remove(target);
                return true;
            }
            return false;
        }
    }

    public List<Item> removeExpiredItems() {
        synchronized (lock) {
            List<Item> removedItems = new ArrayList<>();
            Iterator<Item> iterator = items.iterator();

            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item instanceof Consumable consumable && consumable.isExpired()) {
                    removedItems.add(item);
                    iterator.remove();
                }
            }

            return removedItems;
        }
    }
}

