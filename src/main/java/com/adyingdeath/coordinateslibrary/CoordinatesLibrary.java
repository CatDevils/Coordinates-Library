package com.adyingdeath.coordinateslibrary;

import com.adyingdeath.coordinateslibrary.effect.ParticleEffect;
import com.adyingdeath.coordinateslibrary.menu.InventoryClickListener;
import com.adyingdeath.coordinateslibrary.menu.InventoryUtils;
import com.adyingdeath.coordinateslibrary.storage.CoordLib;
import com.adyingdeath.coordinateslibrary.storage.Coordinate;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoordinatesLibrary extends JavaPlugin implements Listener {

    private CoordLib coordinateLibrary;
    private InventoryUtils inventoryUtils;

    @Override
    public void onEnable() {
        this.coordinateLibrary = new CoordLib();
        this.inventoryUtils = new InventoryUtils(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this, inventoryUtils), this);
        this.getCommand("colib").setExecutor(this);

        saveDefaultConfig(); // 保存默认配置文件
    }

    @Override
    public void onDisable() {
        coordinateLibrary.saveCoordinates();
    }

    private Map<UUID, BukkitTask> trackingTasks = new HashMap<>();

    public void startTracking(Player player, String coordinateId) {
        stopTracking(player); // 如果已经有追踪内容先停止

        Coordinate coordinate = coordinateLibrary.getCoordinate(coordinateId);
        if (coordinate != null) {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
                Location loc = player.getLocation();
                Location coordLoc = coordinate.getLocation();
                double distance = loc.distance(coordLoc);

                // 计算面向方向
                double dx = coordLoc.getX() - loc.getX();
                double dz = coordLoc.getZ() - loc.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                float yaw = loc.getYaw();
                double angle = Math.toDegrees(Math.atan2(-dx, dz)) - yaw;
                angle = (angle + 360) % 360;

                String direction;
                if (angle > 45 && angle < 135) {
                    direction = "→";
                } else if (angle > 225 && angle < 315) {
                    direction = "←";
                } else if (angle > 315 || angle < 45) {
                    direction = "↑";
                } else {
                    direction = "↓";
                }

                String message = StringFormatter.of(getConfig().getString("messages.trackingMsg", ""),
                        "%horizontalDist%", String.format("%.1f", dist),
                        "%dist%", String.format("%.1f", distance),
                        "%indicator%", direction);
                player.sendActionBar(Component.text(message));
                if(distance < 50) {
                    if(distance < 3) {
                        player.sendMessage(Component.text(getConfig().getString("messages.followEnd", "")));
                        stopTracking(player); // 停止追踪
                    }

                    // 显示粒子效果

                    ParticleEffect.circle(Particle.CLOUD, coordLoc, (System.currentTimeMillis() % 1000 * 1.0d) / 1000.0 * 4.0, 20);
                }
            }, 0L, 1L); // 4 ticks = 0.2 seconds

            trackingTasks.put(player.getUniqueId(), task);
        }
    }

    public void stopTracking(Player player) {
        BukkitTask task = trackingTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        player.sendActionBar(Component.text(getConfig().getString("messages.stopFollow", "")));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("colib") && sender instanceof Player player) {
            if(args.length > 0){
                if (args[0].equalsIgnoreCase("reset")) {
                    stopTracking(player);
                    return true;
                }

                if (args[0].equalsIgnoreCase("add")) {
                    if (args.length > 1) {
                        String name = String.join(" ", args).substring(args[0].length()).trim();
                        coordinateLibrary.addCoordinate(player, player.getLocation(), name);
                        String message = StringFormatter.of(getConfig().getString("messages.addCoordinateSuccess", ""), "%name%", name);
                        player.sendMessage(Component.text(message));
                    } else {
                        player.sendMessage(Component.text(getConfig().getString("messages.addCoordinateUsage", "")));
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("p")) {
                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {

                    }, 0L, 1L);
                    return true;
                }
            }else{
                // 打开坐标库界面
                inventoryUtils.openMainMenu(player, coordinateLibrary.getCoordinates(), 0);
                return true;
            }
        }
        return false;
    }

    public CoordLib getCoordinateLibrary() {
        return coordinateLibrary;
    }
}