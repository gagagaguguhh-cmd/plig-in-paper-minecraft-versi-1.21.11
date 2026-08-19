package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RTPManager {

    private final MedievalEconomyPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    private boolean netherUnlocked = false;
    private boolean endUnlocked = false;

    private final Random random = new Random();

    public RTPManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void loadData() {
        dataFile = new File(plugin.getDataFolder(), "rtp_data.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat rtp_data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        netherUnlocked = dataConfig.getBoolean("unlocked.nether", false);
        endUnlocked = dataConfig.getBoolean("unlocked.end", false);
    }

    public void saveData() {
        dataConfig.set("unlocked.nether", netherUnlocked);
        dataConfig.set("unlocked.end", endUnlocked);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan rtp_data.yml: " + e.getMessage());
        }
    }

    public boolean isNetherUnlocked() {
        return netherUnlocked;
    }

    public void unlockNether() {
        if (!netherUnlocked) {
            netherUnlocked = true;
            saveData();
            plugin.getServer().broadcast(netherUnlockedMessage());
        }
    }

    public boolean isEndUnlocked() {
        return endUnlocked;
    }

    public void unlockEnd() {
        if (!endUnlocked) {
            endUnlocked = true;
            saveData();
            plugin.getServer().broadcast(endUnlockedMessage());
        }
    }

    private net.kyori.adventure.text.Component netherUnlockedMessage() {
        return net.kyori.adventure.text.Component.text("🔥 [RTP System] Dimensi Nether telah terbuka untuk Random Teleport!", net.kyori.adventure.text.format.NamedTextColor.RED, net.kyori.adventure.text.format.TextDecoration.BOLD);
    }

    private net.kyori.adventure.text.Component endUnlockedMessage() {
        return net.kyori.adventure.text.Component.text("🔮 [RTP System] Dimensi The End telah terbuka untuk Random Teleport!", net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE, net.kyori.adventure.text.format.TextDecoration.BOLD);
    }

    public boolean isDimensionUnlocked(World world) {
        if (world.getEnvironment() == World.Environment.NORMAL) {
            return true;
        } else if (world.getEnvironment() == World.Environment.NETHER) {
            return netherUnlocked;
        } else if (world.getEnvironment() == World.Environment.THE_END) {
            return endUnlocked;
        }
        return false;
    }

    public Location findSafeLocation(World world, int maxRadius) {
        int maxAttempts = 30;

        for (int i = 0; i < maxAttempts; i++) {
            int x = (random.nextInt(maxRadius * 2) - maxRadius);
            int z = (random.nextInt(maxRadius * 2) - maxRadius);

            if (world.getEnvironment() == World.Environment.NORMAL) {
                int highestY = world.getHighestBlockYAt(x, z);
                Block feet = world.getBlockAt(x, highestY, z);
                Block ground = world.getBlockAt(x, highestY - 1, z);

                if (isSafeGround(ground.getType()) && feet.getType().isAir()) {
                    return new Location(world, x + 0.5, highestY, z + 0.5);
                }
            } else if (world.getEnvironment() == World.Environment.NETHER) {
                int y = 30 + random.nextInt(80);
                Block ground = world.getBlockAt(x, y - 1, z);
                Block feet = world.getBlockAt(x, y, z);
                Block head = world.getBlockAt(x, y + 1, z);

                if (isSafeGround(ground.getType()) && feet.getType().isAir() && head.getType().isAir()) {
                    return new Location(world, x + 0.5, y, z + 0.5);
                }
            } else if (world.getEnvironment() == World.Environment.THE_END) {
                int highestY = world.getHighestBlockYAt(x, z);
                if (highestY > 0) {
                    Block ground = world.getBlockAt(x, highestY - 1, z);
                    Block feet = world.getBlockAt(x, highestY, z);

                    if (isSafeGround(ground.getType()) && feet.getType().isAir()) {
                        return new Location(world, x + 0.5, highestY, z + 0.5);
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafeGround(Material material) {
        if (material.isAir() || material == Material.LAVA || material == Material.WATER) {
            return false;
        }
        return material.isSolid();
    }
}
