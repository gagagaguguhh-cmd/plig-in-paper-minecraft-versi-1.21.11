package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NPCManager {

    public enum NPCType {
        ECONOMIC_QUEST,
        RPG_STATS
    }

    public static class SpecialNPC {
        private final UUID entityUuid;
        private final NPCType type;
        private final Location homeLocation;

        public SpecialNPC(UUID entityUuid, NPCType type, Location homeLocation) {
            this.entityUuid = entityUuid;
            this.type = type;
            this.homeLocation = homeLocation;
        }

        public UUID getEntityUuid() {
            return entityUuid;
        }

        public NPCType getType() {
            return type;
        }

        public Location getHomeLocation() {
            return homeLocation;
        }
    }

    private final MedievalEconomyPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, SpecialNPC> activeNpcs = new HashMap<>();

    public NPCManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "special_npcs.yml");
        loadData();
    }

    public void loadData() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Tidak bisa membuat special_npcs.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String typeStr = config.getString(key + ".type", NPCType.ECONOMIC_QUEST.name());
                NPCType type = NPCType.valueOf(typeStr);
                String worldName = config.getString(key + ".world", "world");
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    double x = config.getDouble(key + ".x");
                    double y = config.getDouble(key + ".y");
                    double z = config.getDouble(key + ".z");
                    Location loc = new Location(world, x, y, z);
                    activeNpcs.put(uuid, new SpecialNPC(uuid, type, loc));
                }
            } catch (Exception ignored) {}
        }
    }

    public void saveData() {
        if (config == null) return;
        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }
        for (Map.Entry<UUID, SpecialNPC> entry : activeNpcs.entrySet()) {
            String path = entry.getKey().toString();
            SpecialNPC npc = entry.getValue();
            config.set(path + ".type", npc.getType().name());
            config.set(path + ".world", npc.getHomeLocation().getWorld().getName());
            config.set(path + ".x", npc.getHomeLocation().getX());
            config.set(path + ".y", npc.getHomeLocation().getY());
            config.set(path + ".z", npc.getHomeLocation().getZ());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan special_npcs.yml: " + e.getMessage());
        }
    }

    public void registerNPC(UUID uuid, NPCType type, Location loc) {
        registerNPC(uuid, type, loc, true);
    }

    public void registerNPC(UUID uuid, NPCType type, Location loc, boolean save) {
        activeNpcs.put(uuid, new SpecialNPC(uuid, type, loc));
        if (save) {
            saveData();
        }
    }

    public void unregisterNPC(UUID uuid) {
        activeNpcs.remove(uuid);
        saveData();
    }

    public SpecialNPC getNPC(UUID uuid) {
        return activeNpcs.get(uuid);
    }

    public boolean isSpecialNPC(UUID uuid) {
        return activeNpcs.containsKey(uuid);
    }

    public Map<UUID, SpecialNPC> getActiveNpcs() {
        return activeNpcs;
    }

    public SpecialNPC getNearestRPGStatsNPC(Location location) {
        SpecialNPC nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (SpecialNPC npc : activeNpcs.values()) {
            if (npc.getType() == NPCType.RPG_STATS) {
                if (npc.getHomeLocation().getWorld().equals(location.getWorld())) {
                    double distSq = npc.getHomeLocation().distanceSquared(location);
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearest = npc;
                    }
                }
            }
        }
        return nearest;
    }
}
