package com.adyingdeath.coordinateslibrary.storage;

import org.bukkit.Location;
import java.io.Serializable;

public class Coordinate implements Serializable {
    private final String id;      // 坐标点的唯一ID
    private final String owner;   // 创建者姓名
    private final Location location; // 坐标位置
    private final String name;    // 坐标点名称

    public Coordinate(String id, String owner, Location location, String name) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return id + " " + owner + " " + location.getWorld().getName() + " " + location.getX() + " " + location.getY() + " " + location.getZ() + " " + name;
    }

    public static Coordinate fromString(String str) {
        String[] parts = str.split(" ");
        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid coordinate string: " + str);
        }
        Location location = new Location(org.bukkit.Bukkit.getWorld(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[4]), Double.parseDouble(parts[5]));
        return new Coordinate(parts[0], parts[1], location, parts[6]);
    }
}