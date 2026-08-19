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
    // UUID -> strength (double)
    private final Map<UUID, Double> strengthMap = new HashMap<>();
    // UUID -> speed (double)
    private final Map<UUID, Double> speedMap = new HashMap<>();
    // UUID -> agility (double)
    private final Map<UUID, Double> agilityMap = new HashMap<>();
    // UUID -> maxHealth (double)
    private final Map<UUID, Double> maxHealthMap = new HashMap<>();
    // UUID -> monstersKilled (int)
    private final Map<UUID, Integer> monstersKilledMap = new HashMap<>();

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
                double strength = config.getDouble(key + ".strength", 1.0);
                double speed = config.getDouble(key + ".speed", 1.0);
                double agility = config.getDouble(key + ".agility", 1.0);
                double maxHealth = config.getDouble(key + ".maxHealth", 20.0);
                int monstersKilled = config.getInt(key + ".monstersKilled", 0);

                initiatedMap.put(uuid, initiated);
                levelMap.put(uuid, level);
                expMap.put(uuid, exp);
                strengthMap.put(uuid, strength);
                speedMap.put(uuid, speed);
                agilityMap.put(uuid, agility);
                maxHealthMap.put(uuid, maxHealth);
                monstersKilledMap.put(uuid, monstersKilled);
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
            config.set(path + ".strength", getStrength(uuid));
            config.set(path + ".speed", getSpeed(uuid));
            config.set(path + ".agility", getAgility(uuid));
            config.set(path + ".maxHealth", getMaxHealth(uuid));
            config.set(path + ".monstersKilled", getMonstersKilled(uuid));
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

    // Status Methods
    public double getStrength(UUID uuid) {
        return strengthMap.getOrDefault(uuid, 1.0);
    }

    public void setStrength(UUID uuid, double strength) {
        strengthMap.put(uuid, Math.max(1.0, strength));
        saveData();
    }

    public double getSpeed(UUID uuid) {
        return speedMap.getOrDefault(uuid, 1.0);
    }

    public void setSpeed(UUID uuid, double speed) {
        speedMap.put(uuid, Math.max(1.0, speed));
        saveData();
    }

    public double getAgility(UUID uuid) {
        return agilityMap.getOrDefault(uuid, 1.0);
    }

    public void setAgility(UUID uuid, double agility) {
        agilityMap.put(uuid, Math.max(1.0, agility));
        saveData();
    }

    public double getMaxHealth(UUID uuid) {
        return maxHealthMap.getOrDefault(uuid, 20.0);
    }

    public void setMaxHealth(UUID uuid, double maxHealth) {
        maxHealthMap.put(uuid, Math.max(20.0, maxHealth));
        saveData();
    }

    public int getMonstersKilled(UUID uuid) {
        return monstersKilledMap.getOrDefault(uuid, 0);
    }

    public void addMonsterKill(UUID uuid) {
        int current = getMonstersKilled(uuid);
        monstersKilledMap.put(uuid, current + 1);
        saveData();
    }

    public double getDamageMultiplier(UUID uuid) {
        return 1.0 + (getStrength(uuid) - 1.0) * 0.1;
    }

    public double getSpeedMultiplier(UUID uuid) {
        return 1.0 + (getSpeed(uuid) - 1.0) * 0.05;
    }

    public double getAgilityMultiplier(UUID uuid) {
        return 1.0 + (getAgility(uuid) - 1.0) * 0.05;
    }
}
