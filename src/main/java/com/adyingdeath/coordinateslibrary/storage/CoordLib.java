package com.adyingdeath.coordinateslibrary.storage;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

public class CoordLib {
    private final Map<String, Coordinate> coordinates = new LinkedHashMap<>(); // 使用LinkedHashMap保持插入顺序
    private int currentId = 0;

    public CoordLib() {
        loadCoordinates();
    }

    public void removeCoordinate(String id) {
        coordinates.remove(id);
    }
    public void addCoordinate(Player player, Location location, String name) {
        String id = String.format("%08X", currentId++);
        Coordinate coordinate = new Coordinate(id, player.getName(), location, name);
        coordinates.put(id, coordinate);
    }

    public Collection<Coordinate> getCoordinates() {
        return coordinates.values();
    }

    public Coordinate getCoordinate(String id) {
        return coordinates.get(id);
    }
    private void loadCoordinates() {
        File file = new File("plugins/CoordinatesLibrary/coordinates.data");
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            if((line = reader.readLine()) != null) {
                // 读取首行作为基本信息
                String[] config = line.split(";");
                if(config.length > 0){
                    this.currentId = Integer.parseInt(config[0]);
                }
            }
            while ((line = reader.readLine()) != null) {
                Coordinate coordinate = Coordinate.fromString(line);
                coordinates.put(coordinate.getId(), coordinate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCoordinates() {
        File file = new File("plugins/CoordinatesLibrary/coordinates.data");
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
            writer.write(String.join(";", String.valueOf(this.currentId)));
            writer.newLine();
            for (Coordinate coordinate : coordinates.values()) {
                writer.write(coordinate.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}