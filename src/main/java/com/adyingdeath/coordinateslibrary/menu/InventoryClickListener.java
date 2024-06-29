package com.adyingdeath.coordinateslibrary.menu;

import com.adyingdeath.coordinateslibrary.storage.Coordinate;
import com.adyingdeath.coordinateslibrary.CoordinatesLibrary;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

public class InventoryClickListener implements Listener {

    private final CoordinatesLibrary plugin;
    private final InventoryUtils inventoryUtils;

    public InventoryClickListener(CoordinatesLibrary plugin, InventoryUtils inventoryUtils) {
        this.plugin = plugin;
        this.inventoryUtils = inventoryUtils;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        InventoryHolder inventoryHolder = event.getClickedInventory().getHolder();
        if (inventoryHolder == null) return;
        if (inventoryHolder instanceof MenuHolder holder){
            if (event.getCurrentItem() == null) return;
            event.setCancelled(true);
            // 检验菜单类别，进行不同的逻辑处理
            switch (holder.type) {
                case MAIN_MENU -> {
                    Material type = event.getCurrentItem().getType();
                    if (type.equals(Material.PAPER)) {
                        // 点击纸则为坐标点
                        // 获取坐标ID
                        String id = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(NamespacedKey.fromString("com.adyingdeath.coordinateslibrary:coord_id"), PersistentDataType.STRING); // 获取坐标ID
                        Coordinate coordinate = plugin.getCoordinateLibrary().getCoordinate(id);
                        if (coordinate != null) {
                            inventoryUtils.openCoordinateMenu(player, coordinate);
                        }
                    } else if (type.equals(Material.ARROW)) {
                        // 点击箭则为向前翻页
                        int currentPage = (int) holder.data;
                        inventoryUtils.openMainMenu(player, plugin.getCoordinateLibrary().getCoordinates(), currentPage - 1);
                    } else if (type.equals(Material.SPECTRAL_ARROW)) {
                        // 点击光灵箭则为向后翻页
                        int currentPage = (int) holder.data;
                        inventoryUtils.openMainMenu(player, plugin.getCoordinateLibrary().getCoordinates(), currentPage + 1);
                    }

                }
                case COORD_OPTIONS -> {
                    Material type = event.getCurrentItem().getType();
                    List<MetadataValue> values = player.getMetadata("com.adyingdeath.coordinateslibrary.current");
                    String id = values.get(0).asString();

                    if (type.equals(Material.BARRIER)) {
                        if (!values.isEmpty()) {
                            Coordinate coordinate = plugin.getCoordinateLibrary().getCoordinate(id);
                            //player.closeInventory();
                            inventoryUtils.openConfirmMenu(player, coordinate);
                        }
                    } else if (type.equals(Material.COMPASS)) {
                        plugin.startTracking(player, id);
                        player.sendMessage(Component.text(plugin.getConfig().getString("messages.followStartMsg", "")));
                        player.closeInventory();
                    }
                    // 可以在此处扩展更多选项的处理逻辑
                }
                case CONFIRM -> {
                    Material type = event.getCurrentItem().getType();
                    List<MetadataValue> values = player.getMetadata("com.adyingdeath.coordinateslibrary.current");
                    String id = values.get(0).asString();
                    if (type.equals(Material.LIME_STAINED_GLASS_PANE)) {
                        plugin.getCoordinateLibrary().removeCoordinate(id);
                        plugin.getCoordinateLibrary().saveCoordinates();
                        player.sendMessage(Component.text(plugin.getConfig().getString("messages.deleteConfirm", "")));
                        player.closeInventory();
                    } else if (type.equals(Material.RED_STAINED_GLASS_PANE)) {
                        player.closeInventory();
                    }
                }
            }
        }
    }
}