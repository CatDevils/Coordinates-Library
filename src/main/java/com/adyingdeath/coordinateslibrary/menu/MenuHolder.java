package com.adyingdeath.coordinateslibrary.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MenuHolder implements InventoryHolder {
    public Object data;
    public MenuType type;

    public MenuHolder(MenuType type) {
        this.type = type;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
