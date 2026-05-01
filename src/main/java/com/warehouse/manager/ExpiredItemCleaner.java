package com.warehouse.manager;

import com.warehouse.models.Item;
import com.warehouse.util.ConsoleIO;
import com.warehouse.util.FileManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ExpiredItemCleaner {
    private static final int CHECK_INTERVAL_SECONDS = 30;

    private final Warehouse warehouse;
    private final ScheduledExecutorService scheduler;
    private final ExpiredItemsListener listener;

    @FunctionalInterface
    public interface ExpiredItemsListener {
        void onItemsRemoved(List<Item> removedItems);
    }

    public ExpiredItemCleaner(Warehouse warehouse) {
        this(warehouse, null);
    }

    public ExpiredItemCleaner(Warehouse warehouse, ExpiredItemsListener listener) {
        this.warehouse = warehouse;
        this.listener = listener;
        ThreadFactory daemonFactory = runnable -> {
            Thread thread = new Thread(runnable, "expired-item-cleaner");
            thread.setDaemon(true);
            return thread;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(daemonFactory);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::cleanExpiredItems, CHECK_INTERVAL_SECONDS, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void cleanExpiredItems() {
        List<Item> removedItems = warehouse.removeExpiredItems();
        if (!removedItems.isEmpty()) {
            FileManager.appendExpiredRemovalLog(removedItems);
            if (listener != null) {
                listener.onItemsRemoved(removedItems);
            }
            StringBuilder message = new StringBuilder();
            message.append("\n[Cleaner] ================= AUTO CLEAN REPORT =================");
            message.append(System.lineSeparator())
                    .append("[Cleaner] Removed expired items: ")
                    .append(removedItems.size());
            int index = 1;
            for (Item item : removedItems) {
                message.append(System.lineSeparator())
                        .append("[Cleaner] ")
                        .append(index++)
                        .append(") ")
                        .append(item.getName())
                        .append(" | type=")
                        .append(item.getClass().getSimpleName())
                        .append(" | weight=")
                        .append(String.format("%.2f", item.getWeight()));
            }
            message.append(System.lineSeparator())
                    .append("[Cleaner] =====================================================\n");
            ConsoleIO.enqueueLine(message.toString());
        }
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}


