package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class EconomyManager {

    private final MedievalEconomyPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final DecimalFormat dollarFormatter = new DecimalFormat("$#,###.##");
    private final DecimalFormat goldFormatter = new DecimalFormat("#,### Gold");

    public EconomyManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        dataFile = new File(plugin.getDataFolder(), "players.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat file players.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan data players.yml: " + e.getMessage());
        }
    }

    public void registerPlayer(UUID uuid, String name) {
        String path = uuid.toString();
        if (!dataConfig.contains(path)) {
            dataConfig.set(path + ".name", name);
            dataConfig.set(path + ".dollar", 0.0);
            dataConfig.set(path + ".gold", 0.0);
            dataConfig.set(path + ".total_earned_dollar", 0.0);
            dataConfig.set(path + ".total_spent_dollar", 0.0);
            dataConfig.set(path + ".items_sold", 0);
            dataConfig.set(path + ".items_bought", 0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            dataConfig.set(path + ".joined_date", sdf.format(new Date()));
            saveData();
        } else {
            dataConfig.set(path + ".name", name);
            if (!dataConfig.contains(path + ".dollar")) dataConfig.set(path + ".dollar", 0.0);
            if (!dataConfig.contains(path + ".gold")) dataConfig.set(path + ".gold", 0.0);
            saveData();
        }
    }

    // --- DOLLAR ---
    public double getDollar(UUID uuid) {
        return dataConfig.getDouble(uuid.toString() + ".dollar", 0.0);
    }

    public void setDollar(UUID uuid, double amount) {
        dataConfig.set(uuid.toString() + ".dollar", Math.max(0, amount));
        saveData();
    }

    public boolean addDollar(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getDollar(uuid);
        dataConfig.set(uuid.toString() + ".dollar", current + amount);
        
        double earned = dataConfig.getDouble(uuid.toString() + ".total_earned_dollar", 0.0);
        dataConfig.set(uuid.toString() + ".total_earned_dollar", earned + amount);
        saveData();
        return true;
    }

    public boolean withdrawDollar(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getDollar(uuid);
        if (current < amount) return false;
        
        dataConfig.set(uuid.toString() + ".dollar", current - amount);
        double spent = dataConfig.getDouble(uuid.toString() + ".total_spent_dollar", 0.0);
        dataConfig.set(uuid.toString() + ".total_spent_dollar", spent + amount);
        saveData();
        return true;
    }

    public boolean hasDollar(UUID uuid, double amount) {
        return getDollar(uuid) >= amount;
    }

    // --- GOLD ---
    public double getGold(UUID uuid) {
        return dataConfig.getDouble(uuid.toString() + ".gold", 0.0);
    }

    public void setGold(UUID uuid, double amount) {
        dataConfig.set(uuid.toString() + ".gold", Math.max(0, amount));
        saveData();
    }

    public boolean addGold(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getGold(uuid);
        dataConfig.set(uuid.toString() + ".gold", current + amount);
        saveData();
        return true;
    }

    public boolean withdrawGold(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getGold(uuid);
        if (current < amount) return false;
        
        dataConfig.set(uuid.toString() + ".gold", current - amount);
        saveData();
        return true;
    }

    public boolean hasGold(UUID uuid, double amount) {
        return getGold(uuid) >= amount;
    }

    // --- STATISTIK ---
    public void addItemsSold(UUID uuid, int count) {
        int current = dataConfig.getInt(uuid.toString() + ".items_sold", 0);
        dataConfig.set(uuid.toString() + ".items_sold", current + count);
        saveData();
    }

    public void addItemsBought(UUID uuid, int count) {
        int current = dataConfig.getInt(uuid.toString() + ".items_bought", 0);
        dataConfig.set(uuid.toString() + ".items_bought", current + count);
        saveData();
    }

    public double getTotalEarnedDollar(UUID uuid) {
        return dataConfig.getDouble(uuid.toString() + ".total_earned_dollar", 0.0);
    }

    public double getTotalSpentDollar(UUID uuid) {
        return dataConfig.getDouble(uuid.toString() + ".total_spent_dollar", 0.0);
    }

    public int getItemsSold(UUID uuid) {
        return dataConfig.getInt(uuid.toString() + ".items_sold", 0);
    }

    public int getItemsBought(UUID uuid) {
        return dataConfig.getInt(uuid.toString() + ".items_bought", 0);
    }

    public String getJoinedDate(UUID uuid) {
        return dataConfig.getString(uuid.toString() + ".joined_date", "Belum Tercatat");
    }

    public String formatDollar(double amount) {
        return dollarFormatter.format(amount);
    }

    public String formatGold(double amount) {
        return goldFormatter.format(amount);
    }

    public UUID getUUIDByName(String name) {
        for (String key : dataConfig.getKeys(false)) {
            String pName = dataConfig.getString(key + ".name");
            if (pName != null && pName.equalsIgnoreCase(name)) {
                try {
                    return UUID.fromString(key);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    public String getPlayerName(UUID uuid) {
        return dataConfig.getString(uuid.toString() + ".name", "Pemain");
    }
}
