package com.warehouse;

/**
 * Legacy CLI launcher removed from the GUI submission path.
 * Use {@link com.warehouse.gui.WarehouseApp} via {@link WarehouseGuiLauncher} instead.
 */
@Deprecated
public final class WarehouseDemo {
    private WarehouseDemo() {
        throw new UnsupportedOperationException("Legacy CLI launcher removed. Use the JavaFX GUI launcher instead.");
    }
}