package com.adyingdeath.coordinateslibrary.menu;

import com.adyingdeath.coordinateslibrary.StringFormatter;
import com.adyingdeath.coordinateslibrary.storage.Coordinate;
import com.adyingdeath.coordinateslibrary.CoordinatesLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InventoryUtils implements Listener {

    private final CoordinatesLibrary plugin;

    public InventoryUtils(CoordinatesLibrary plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player, Collection<Coordinate> coordinates, int currentPage) {
        final int ITEMS_PER_PAGE = 45;
        int totalCoordinates = coordinates.size();
        int totalPages = (int) Math.ceil((double) totalCoordinates / ITEMS_PER_PAGE);
        List<Coordinate> coordinateList = new ArrayList<>(coordinates);

        MenuHolder holder = new MenuHolder(MenuType.MAIN_MENU);
        holder.data = currentPage;
        Inventory inventory = Bukkit.createInventory(holder, 54,
                Component.text(
                        StringFormatter.of(plugin.getConfig().getString("titles.coordLibrary", ""),
                        "%now%", currentPage + 1,
                                "%total%", totalPages)));

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = new ArrayList<>();
        // 填充坐标
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, totalCoordinates);
        for (int i = start; i < end; i++) {
            Coordinate coordinate = coordinateList.get(i);
            if (meta != null) {
                meta.displayName(
                        Component.text(coordinate.getName())
                                .decoration(TextDecoration.ITALIC, false)
                );
                lore.clear();
                Location loc = coordinate.getLocation();
                lore.add(Component.text("by " + coordinate.getOwner()));
                lore.add(Component.text("世界: " + loc.getWorld().getName()));
                lore.add(Component.text(String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ())));
                meta.lore(lore);
                meta.getPersistentDataContainer().set(NamespacedKey.fromString("com.adyingdeath.coordinateslibrary:coord_id"), PersistentDataType.STRING, coordinate.getId());
                item.setItemMeta(meta);
            }
            inventory.addItem(item);
        }

        // 翻页按钮（上一页和下一页）
        lore.clear();
        // itemID的前后一页按钮，你可以根据具体需求进行调整
        if (currentPage > 0) {
            item.setType(Material.ARROW);
            meta = item.getItemMeta();
            meta.lore(lore);
            meta.displayName(
                    Component.text(plugin.getConfig().getString("messages.prevPage", ""))
                            .decoration(TextDecoration.ITALIC, false)
            );
            item.setItemMeta(meta);
            inventory.setItem(45, item);
        }
        if (currentPage < totalPages - 1) {
            item.setType(Material.SPECTRAL_ARROW);
            meta = item.getItemMeta();
            meta.lore(lore);
            meta.displayName(
                    Component.text(plugin.getConfig().getString("messages.nextPage", ""))
                            .decoration(TextDecoration.ITALIC, false)
            );
            item.setItemMeta(meta);
            inventory.setItem(53, item);
        }

        player.openInventory(inventory);
    }

    // 打开具体坐标点的菜单
    public void openCoordinateMenu(Player player, Coordinate coordinate) {
        String coordMenuTitle = StringFormatter.of(plugin.getConfig().getString("titles.coordMenu", ""), "%name%", coordinate.getName());
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.COORD_OPTIONS), 27, Component.text(coordMenuTitle));

        // 用于设置物品的描述文本，在这里初始化方便后面连续复用
        List<Component> lore = new ArrayList<>();

        // 添加删除选项
        ItemStack deleteItem = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        if (deleteMeta != null) {
            deleteMeta.displayName(
                    Component.text(
                            plugin.getConfig().getString("messages.deleteOption", "")
                    ).decoration(TextDecoration.ITALIC, false)
            );

            lore.add(Component.text(plugin.getConfig().getString("messages.deleteOptionLore", "")));
            deleteItem.lore(lore);
            deleteItem.setItemMeta(deleteMeta);
        }
        inventory.addItem(deleteItem);

        // 追踪选项
        ItemStack followItem = new ItemStack(Material.COMPASS);
        ItemMeta followMeta = followItem.getItemMeta();
        if (followMeta != null) {
            followMeta.displayName(
                    Component.text(
                            plugin.getConfig().getString("messages.followOption", "")
                    ).decoration(TextDecoration.ITALIC, false)
            );
            lore.clear();
            lore.add(Component.text(plugin.getConfig().getString("messages.followOptionLore", "")));
            followMeta.lore(lore);
            followItem.setItemMeta(followMeta);
        }
        inventory.addItem(followItem);

        // 可以在这里添加更多选项，按照相同方式添加 ItemStack
        /*
        // 示例选项：传送到坐标点
        ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleportItem.getItemMeta();
        if (teleportMeta != null) {
            teleportMeta.setDisplayName("传送到此坐标");
            List<String> lore = new ArrayList<>();
            lore.add("点击传送到坐标点");
            teleportMeta.setLore(lore);
            teleportItem.setItemMeta(teleportMeta);
        }
        inventory.addItem(teleportItem);
        */

        player.openInventory(inventory);
        // 保存当前坐标点的信息，便于后续使用
        player.setMetadata("com.adyingdeath.coordinateslibrary.current", new FixedMetadataValue(plugin, coordinate.getId()));
    }

    // 打开确认菜单
    public void openConfirmMenu(Player player, Coordinate coordinate) {
        String menuTitle = StringFormatter.of(plugin.getConfig().getString("titles.confirmMenu", ""), "%name%", coordinate.getName());
        Inventory inventory = Bukkit.createInventory(new MenuHolder(MenuType.CONFIRM), 9, Component.text(menuTitle));

        // 确定选项
        ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(
                    Component.text(
                            plugin.getConfig().getString("messages.confirm", "")
                    )
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.GREEN)
            );

            confirmItem.setItemMeta(confirmMeta);
        }
        inventory.setItem(2, confirmItem);

        // 取消选项
        ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(
                    Component.text(
                                    plugin.getConfig().getString("messages.cancel", "")
                            )
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.RED)
            );

            cancelItem.setItemMeta(cancelMeta);
        }
        inventory.setItem(6, cancelItem);

        player.openInventory(inventory);
    }
}