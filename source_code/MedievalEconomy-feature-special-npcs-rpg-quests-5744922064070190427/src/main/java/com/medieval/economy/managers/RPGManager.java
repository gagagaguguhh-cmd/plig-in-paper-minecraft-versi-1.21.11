package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RPGManager {

    private final MedievalEconomyPlugin plugin;
    private final File file;
    private FileConfiguration config;

    // UUID -> initiated (boolean)
    private final Map<UUID, Boolean> initiatedMap = new HashMap<>();
    // UUID -> level (int)
    private final Map<UUID, Integer> levelMap = new HashMap<>();
    // UUID -> exp (long)
    private final Map<UUID, Long> expMap = new HashMap<>();

    public RPGManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "rpg_players.yml");
        loadData();
    }

    public void loadData() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Tidak bisa membuat rpg_players.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                boolean initiated = config.getBoolean(key + ".initiated", false);
                int level = config.getInt(key + ".level", 1);
                long exp = config.getLong(key + ".exp", 0L);

                initiatedMap.put(uuid, initiated);
                levelMap.put(uuid, level);
                expMap.put(uuid, exp);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void saveData() {
        if (config == null) return;
        for (UUID uuid : initiatedMap.keySet()) {
            String path = uuid.toString();
            config.set(path + ".initiated", isInitiated(uuid));
            config.set(path + ".level", getLevel(uuid));
            config.set(path + ".exp", getExp(uuid));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan rpg_players.yml: " + e.getMessage());
        }
    }

    public boolean isInitiated(UUID uuid) {
        return initiatedMap.getOrDefault(uuid, false);
    }

    public void setInitiated(UUID uuid, boolean initiated) {
        initiatedMap.put(uuid, initiated);
        saveData();
    }

    public int getLevel(UUID uuid) {
        return levelMap.getOrDefault(uuid, 1);
    }

    public long getExp(UUID uuid) {
        return expMap.getOrDefault(uuid, 0L);
    }

    public long getRequiredExpForNextLevel(int currentLevel) {
        return currentLevel * 100L;
    }

    public boolean addExp(UUID uuid, long amount) {
        int currentLevel = getLevel(uuid);
        long currentExp = getExp(uuid) + amount;
        long reqExp = getRequiredExpForNextLevel(currentLevel);

        boolean levelUp = false;
        while (currentExp >= reqExp) {
            currentExp -= reqExp;
            currentLevel++;
            reqExp = getRequiredExpForNextLevel(currentLevel);
            levelUp = true;
        }

        levelMap.put(uuid, currentLevel);
        expMap.put(uuid, currentExp);
        saveData();
        return levelUp;
    }
}
