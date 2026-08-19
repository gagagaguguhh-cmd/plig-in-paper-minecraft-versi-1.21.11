package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SettingsManager {

    private final MedievalEconomyPlugin plugin;
    private File settingsFile;
    private FileConfiguration settingsConfig;

    public SettingsManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        loadSettings();
    }

    public void loadSettings() {
        settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        if (!settingsFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                settingsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat settings.yml: " + e.getMessage());
            }
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
    }

    public void saveSettings() {
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan settings.yml: " + e.getMessage());
        }
    }

    public boolean isSellConfirmMode(String uuid) {
        return settingsConfig.getBoolean(uuid + ".sell_confirm_mode", true);
    }

    public void setSellConfirmMode(String uuid, boolean confirm) {
        settingsConfig.set(uuid + ".sell_confirm_mode", confirm);
        saveSettings();
    }

    public boolean isMonstersEnabled(String uuid) {
        return settingsConfig.getBoolean(uuid + ".monsters_enabled", true);
    }

    public void setMonstersEnabled(String uuid, boolean enabled) {
        settingsConfig.set(uuid + ".monsters_enabled", enabled);
        saveSettings();
    }
}
