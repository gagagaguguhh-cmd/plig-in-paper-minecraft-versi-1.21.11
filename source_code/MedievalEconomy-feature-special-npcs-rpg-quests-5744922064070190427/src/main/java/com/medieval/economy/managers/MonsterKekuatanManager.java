package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MonsterKekuatanManager {

    private final MedievalEconomyPlugin plugin;
    private final File file;
    private FileConfiguration config;

    // UUID -> kekuatan (int)
    private final Map<UUID, Integer> kekuatanMap = new HashMap<>();
    // UUID -> totalMonsterKilled (int)
    private final Map<UUID, Integer> totalMonsterKilledMap = new HashMap<>();
    // UUID -> lastRaidTime (long)
    private final Map<UUID, Long> lastRaidTimeMap = new HashMap<>();

    public MonsterKekuatanManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "monster_kekuatan.yml");
        loadData();
    }

    public void loadData() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Tidak bisa membuat monster_kekuatan.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int kekuatan = config.getInt(key + ".kekuatan", 1);
                int totalKilled = config.getInt(key + ".total_killed", 0);
                long lastRaid = config.getLong(key + ".last_raid", 0L);

                kekuatanMap.put(uuid, kekuatan);
                totalMonsterKilledMap.put(uuid, totalKilled);
                lastRaidTimeMap.put(uuid, lastRaid);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveData() {
        if (config == null) return;
        for (UUID uuid : kekuatanMap.keySet()) {
            String path = uuid.toString();
            config.set(path + ".kekuatan", getKekuatan(uuid));
            config.set(path + ".total_killed", getTotalMonsterKilled(uuid));
            config.set(path + ".last_raid", getLastRaidTime(uuid));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan monster_kekuatan.yml: " + e.getMessage());
        }
    }

    public int getKekuatan(UUID uuid) {
        return kekuatanMap.getOrDefault(uuid, 1);
    }

    public void setKekuatan(UUID uuid, int kekuatan) {
        kekuatanMap.put(uuid, Math.max(1, kekuatan));
        saveData();
    }

    public void addKekuatan(UUID uuid, int amount) {
        int current = getKekuatan(uuid);
        setKekuatan(uuid, current + amount);
    }

    public int getTotalMonsterKilled(UUID uuid) {
        return totalMonsterKilledMap.getOrDefault(uuid, 0);
    }

    public void addMonsterKill(UUID uuid, int amount) {
        int current = getTotalMonsterKilled(uuid);
        totalMonsterKilledMap.put(uuid, current + amount);
        saveData();
    }

    public long getLastRaidTime(UUID uuid) {
        return lastRaidTimeMap.getOrDefault(uuid, 0L);
    }

    public void setLastRaidTime(UUID uuid, long timestamp) {
        lastRaidTimeMap.put(uuid, timestamp);
        saveData();
    }

    public boolean canStartRaid(UUID uuid) {
        long lastRaid = getLastRaidTime(uuid);
        if (lastRaid == 0L) return true;
        long cooldownMs = 5 * 60 * 1000; // 5 menit cooldown
        return System.currentTimeMillis() - lastRaid >= cooldownMs;
    }

    public long getRaidCooldownRemaining(UUID uuid) {
        long lastRaid = getLastRaidTime(uuid);
        if (lastRaid == 0L) return 0;
        long cooldownMs = 5 * 60 * 1000;
        long elapsed = System.currentTimeMillis() - lastRaid;
        return Math.max(0, cooldownMs - elapsed);
    }
}

