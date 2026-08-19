package com.medieval.economy.listeners;

import com.medieval.economy.MedievalEconomyPlugin;
import com.medieval.economy.managers.EconomyManager;
import com.medieval.economy.managers.NPCManager;
import com.medieval.economy.managers.RPGManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.RandomGenerator;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpecialNPCUpgradeListener implements Listener {

    private final MedievalEconomyPlugin plugin;
    private final NPCManager npcManager;
    private final RPGManager rpgManager;
    private final EconomyManager economyManager;
    private final NamespacedKey npcTypeKey;
    
    // Cooldown untuk raid (dalam milliseconds) - 5 menit
    private final Map<UUID, Long> raidCooldownMap = new HashMap<>();
    private static final long RAID_COOLDOWN_MS = 5 * 60 * 1000;

    public SpecialNPCUpgradeListener(MedievalEconomyPlugin plugin, NPCManager npcManager, RPGManager rpgManager, EconomyManager economyManager, NamespacedKey npcTypeKey) {
        this.plugin = plugin;
        this.npcManager = npcManager;
        this.rpgManager = rpgManager;
        this.economyManager = economyManager;
        this.npcTypeKey = npcTypeKey;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        if (!pdc.has(npcTypeKey, PersistentDataType.STRING)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        String typeStr = pdc.get(npcTypeKey, PersistentDataType.STRING);
        NPCManager.NPCType type = NPCManager.NPCType.valueOf(typeStr);

        if (type == NPCManager.NPCType.MONSTER_RAID) {
            handleMonsterRaidNPCInteract(player);
        } else if (type == NPCManager.NPCType.STATUS_UPGRADE) {
            handleStatusUpgradeNPCInteract(player);
        } else if (type == NPCManager.NPCType.EQUIPMENT_UPGRADE) {
            handleEquipmentUpgradeNPCInteract(player);
        }
    }

    private void handleMonsterRaidNPCInteract(Player player) {
        UUID uuid = player.getUniqueId();
        if (!rpgManager.isInitiated(uuid)) {
            player.sendMessage(Component.text("⚔️ [Pemberi Misi Raid]: ", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                    .append(Component.text("Kamu harus menemui Tetua RPG Desa terlebih dahulu!", NamedTextColor.RED)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        openMonsterRaidGUI(player);
    }

    public void openMonsterRaidGUI(Player player) {
        UUID uuid = player.getUniqueId();
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("⚔️ Misi Raid Monster", NamedTextColor.DARK_RED));

        // Slot 13 - Informasi kekuatan player
        ItemStack infoItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("📊 Statistik Kekuatanmu", NamedTextColor.AQUA, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("💪 Kekuatan: ", NamedTextColor.GRAY).append(Component.text(String.format("%.1f", rpgManager.getStrength(uuid)), NamedTextColor.YELLOW)));
            lore.add(Component.text("⚡ Kecepatan: ", NamedTextColor.GRAY).append(Component.text(String.format("%.1f", rpgManager.getSpeed(uuid)), NamedTextColor.YELLOW)));
            lore.add(Component.text("🦅 Kelincahan: ", NamedTextColor.GRAY).append(Component.text(String.format("%.1f", rpgManager.getAgility(uuid)), NamedTextColor.YELLOW)));
            lore.add(Component.text("❤️ Darah Maksimal: ", NamedTextColor.GRAY).append(Component.text(String.format("%.1f", rpgManager.getMaxHealth(uuid)), NamedTextColor.RED)));
            lore.add(Component.text("☠️ Monster Dibunuh: ", NamedTextColor.GRAY).append(Component.text(rpgManager.getMonstersKilled(uuid), NamedTextColor.DARK_PURPLE)));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            
            long cooldownRemaining = getCooldownRemaining(uuid);
            if (cooldownRemaining > 0) {
                int minutes = (int) (cooldownRemaining / 60000);
                int seconds = (int) ((cooldownRemaining % 60000) / 1000);
                lore.add(Component.text("⏳ Cooldown: ", NamedTextColor.GRAY).append(Component.text(minutes + "m " + seconds + "s", NamedTextColor.RED)));
            } else {
                lore.add(Component.text("✅ Siap untuk raid!", NamedTextColor.GREEN));
            }
            
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("👉 KLIK UNTUK MULAI RAID!", NamedTextColor.GREEN, TextDecoration.BOLD));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(13, infoItem);

        // Background filler
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.text(" "));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    private void handleStatusUpgradeNPCInteract(Player player) {
        UUID uuid = player.getUniqueId();
        if (!rpgManager.isInitiated(uuid)) {
            player.sendMessage(Component.text("🧘 [Master Status]: ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                    .append(Component.text("Kamu harus menemui Tetua RPG Desa terlebih dahulu!", NamedTextColor.RED)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        openStatusUpgradeGUI(player);
    }

    public void openStatusUpgradeGUI(Player player) {
        UUID uuid = player.getUniqueId();
        Inventory gui = Bukkit.createInventory(null, 45, Component.text("🧘 Upgrade Status", NamedTextColor.LIGHT_PURPLE));

        double strength = rpgManager.getStrength(uuid);
        double speed = rpgManager.getSpeed(uuid);
        double agility = rpgManager.getAgility(uuid);
        double maxHealth = rpgManager.getMaxHealth(uuid);
        int level = rpgManager.getLevel(uuid);

        // Calculate costs based on current stats
        int strengthCost = calculateUpgradeCost(strength);
        int speedCost = calculateUpgradeCost(speed);
        int agilityCost = calculateUpgradeCost(agility);
        int healthCost = calculateUpgradeCost(maxHealth / 2); // Normalize to similar scale

        // Strength upgrade item
        ItemStack strengthItem = createUpgradeItem(Material.IRON_SWORD, "💪 Upgrade Kekuatan", 
            String.format("%.1f", strength), String.format("%.1f", strength + 1.0), strengthCost, NamedTextColor.RED);
        gui.setItem(10, strengthItem);

        // Speed upgrade item
        ItemStack speedItem = createUpgradeItem(Material.FEATHER, "⚡ Upgrade Kecepatan",
            String.format("%.1f", speed), String.format("%.1f", speed + 1.0), speedCost, NamedTextColor.AQUA);
        gui.setItem(12, speedItem);

        // Agility upgrade item
        ItemStack agilityItem = createUpgradeItem(Material.ARROW, "🦅 Upgrade Kelincahan",
            String.format("%.1f", agility), String.format("%.1f", agility + 1.0), agilityCost, NamedTextColor.GREEN);
        gui.setItem(14, agilityItem);

        // Health upgrade item
        ItemStack healthItem = createUpgradeItem(Material.GOLDEN_APPLE, "❤️ Upgrade Darah",
            String.format("%.1f", maxHealth), String.format("%.1f", maxHealth + 10.0), healthCost, NamedTextColor.RED);
        gui.setItem(16, healthItem);

        // Info item in center
        ItemStack infoItem = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("📊 Informasi Status", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Level RPG: ", NamedTextColor.GRAY).append(Component.text(level, NamedTextColor.YELLOW)));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Biaya upgrade meningkat", NamedTextColor.GRAY));
            lore.add(Component.text("seiring status yang lebih tinggi!", NamedTextColor.GRAY));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(22, infoItem);

        // Background filler
        ItemStack filler = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.text(" "));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    private void handleEquipmentUpgradeNPCInteract(Player player) {
        UUID uuid = player.getUniqueId();
        if (!rpgManager.isInitiated(uuid)) {
            player.sendMessage(Component.text("🔨 [Pandai Besi Legendaris]: ", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text("Kamu harus menemui Tetua RPG Desa terlebih dahulu!", NamedTextColor.RED)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        openEquipmentUpgradeGUI(player);
    }

    public void openEquipmentUpgradeGUI(Player player) {
        UUID uuid = player.getUniqueId();
        Inventory gui = Bukkit.createInventory(null, 54, Component.text("🔨 Upgrade Equipment Legendaris", NamedTextColor.GOLD));

        // Info about equipment upgrade
        ItemStack infoItem = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("📜 Panduan Upgrade", NamedTextColor.GOLD, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Upgrade senjata dan armor", NamedTextColor.GRAY));
            lore.add(Component.text("melebihi batas normal!", NamedTextColor.GRAY));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("⚠️ PERINGATAN:", NamedTextColor.RED, TextDecoration.BOLD));
            lore.add(Component.text("Upgrade ini sangat mahal", NamedTextColor.GRAY));
            lore.add(Component.text("dan memiliki efek di luar nalar!", NamedTextColor.GRAY));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("👉 Masukkan item di slot tengah", NamedTextColor.YELLOW));
            lore.add(Component.text("untuk melihat opsi upgrade!", NamedTextColor.YELLOW));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("💎 Level Upgrade:", NamedTextColor.AQUA));
            lore.add(Component.text("  +1: Basic (2x damage/armor)", NamedTextColor.WHITE));
            lore.add(Component.text("  +2: Advanced (5x damage/armor)", NamedTextColor.GREEN));
            lore.add(Component.text("  +3: Master (10x damage/armor)", NamedTextColor.BLUE));
            lore.add(Component.text("  +4: Legendary (25x damage/armor)", NamedTextColor.GOLD));
            lore.add(Component.text("  +5: GODLIKE (50x damage/armor)", NamedTextColor.DARK_PURPLE));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        gui.setItem(22, infoItem);

        // Upgrade level options (only visible when item is placed)
        ItemStack upgradeLevel1 = createUpgradeLevelItem(Material.IRON_INGOT, "+1 Basic", 5000);
        ItemStack upgradeLevel2 = createUpgradeLevelItem(Material.GOLD_INGOT, "+2 Advanced", 15000);
        ItemStack upgradeLevel3 = createUpgradeLevelItem(Material.DIAMOND, "+3 Master", 50000);
        ItemStack upgradeLevel4 = createUpgradeLevelItem(Material.NETHERITE_INGOT, "+4 Legendary", 150000);
        ItemStack upgradeLevel5 = createUpgradeLevelItem(Material.NETHER_STAR, "+5 GODLIKE", 500000);

        // These will be set dynamically when item is placed
        // For now, show them as locked
        gui.setItem(30, setLocked(upgradeLevel1));
        gui.setItem(31, setLocked(upgradeLevel2));
        gui.setItem(32, setLocked(upgradeLevel3));
        gui.setItem(39, setLocked(upgradeLevel4));
        gui.setItem(40, setLocked(upgradeLevel5));
        gui.setItem(41, setLocked(upgradeLevel5));

        // Background filler
        ItemStack filler = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.text(" "));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    private ItemStack createUpgradeLevelItem(Material material, String name, int cost) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("🔥 Upgrade " + name, NamedTextColor.GOLD, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Efek: Damage/Armor x" + getMultiplier(name), NamedTextColor.YELLOW));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("💰 Biaya: $" + cost, NamedTextColor.GOLD));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("👉 KLIK UNTUK UPGRADE!", NamedTextColor.GREEN, TextDecoration.BOLD));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int getMultiplier(String levelName) {
        switch (levelName) {
            case "+1 Basic": return 2;
            case "+2 Advanced": return 5;
            case "+3 Master": return 10;
            case "+4 Legendary": return 25;
            case "+5 GODLIKE": return 50;
            default: return 1;
        }
    }

    private ItemStack setLocked(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore();
            if (lore != null) {
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                lore.add(Component.text("🔒 TERKUNCI - Butuh item!", NamedTextColor.RED));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    // Store pending upgrade items per player
    private final Map<UUID, ItemStack> pendingUpgradeItems = new HashMap<>();

    private ItemStack createUpgradeItem(Material material, String name, String current, String next, int cost, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Status Saat Ini: ", NamedTextColor.GRAY).append(Component.text(current, NamedTextColor.WHITE)));
            lore.add(Component.text("Status Setelah Upgrade: ", NamedTextColor.GRAY).append(Component.text(next, NamedTextColor.GREEN)));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("💰 Biaya: ", NamedTextColor.GRAY).append(Component.text("$" + cost, NamedTextColor.GOLD)));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("👉 KLIK UNTUK UPGRADE!", NamedTextColor.GREEN, TextDecoration.BOLD));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private int calculateUpgradeCost(double currentValue) {
        // Biaya meningkat secara eksponensial
        return (int) Math.pow(currentValue, 2) * 10;
    }

    private long getCooldownRemaining(UUID uuid) {
        if (!raidCooldownMap.containsKey(uuid)) return 0;
        long elapsed = System.currentTimeMillis() - raidCooldownMap.get(uuid);
        return Math.max(0, RAID_COOLDOWN_MS - elapsed);
    }

    private Location generateRaidLocation(Player player) {
        Location playerLoc = player.getLocation();
        RandomGenerator rand = RandomGenerator.getDefault();
        
        // Generate koordinat 500-1500 blok dari player
        double angle = rand.nextDouble() * 2 * Math.PI;
        double distance = 500 + rand.nextDouble() * 1000;
        
        double x = playerLoc.getX() + Math.cos(angle) * distance;
        double z = playerLoc.getZ() + Math.sin(angle) * distance;
        
        // Cari Y surface
        Location loc = new Location(playerLoc.getWorld(), x, 256, z);
        while (loc.getBlockY() > 0 && !loc.getBlock().getType().isSolid()) {
            loc.subtract(0, 1, 0);
        }
        loc.add(0, 1, 0); // Naik ke atas permukaan
        
        return loc;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = event.getView().title();
        UUID uuid = player.getUniqueId();

        if (title.toString().contains("Misi Raid Monster")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot != 13) return;

            long cooldownRemaining = getCooldownRemaining(uuid);
            if (cooldownRemaining > 0) {
                int minutes = (int) (cooldownRemaining / 60000);
                int seconds = (int) ((cooldownRemaining % 60000) / 1000);
                player.sendMessage(Component.text("⏳ Tunggu " + minutes + "m " + seconds + "s sebelum raid lagi!", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Generate koordinat raid
            Location raidLoc = generateRaidLocation(player);
            
            // Set cooldown
            raidCooldownMap.put(uuid, System.currentTimeMillis());

            player.closeInventory();
            player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
            player.sendMessage(Component.text("⚔️ [MISI RAID DIMULAI]", NamedTextColor.DARK_RED, TextDecoration.BOLD));
            player.sendMessage(Component.text("📍 Koordinat Target: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.0f, %.0f, %.0f", raidLoc.getX(), raidLoc.getY(), raidLoc.getZ()), NamedTextColor.AQUA)));
            player.sendMessage(Component.text("🗺️ Jarak: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%.0f", player.getLocation().distance(raidLoc)) + " blok", NamedTextColor.YELLOW)));
            player.sendMessage(Component.text("⏳ Waktu: ", NamedTextColor.GRAY)
                    .append(Component.text("5 menit cooldown", NamedTextColor.RED)));
            player.sendMessage(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            player.sendMessage(Component.text("💀 Bunuh monster di lokasi target", NamedTextColor.GRAY));
            player.sendMessage(Component.text("untuk meningkatkan kekuatanmu!", NamedTextColor.GRAY));
            player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.8f);

        } else if (title.toString().contains("Upgrade Status")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            
            int cost = 0;
            String statType = null;
            double increment = 0;

            if (slot == 10) { // Strength
                double currentStrength = rpgManager.getStrength(uuid);
                cost = calculateUpgradeCost(currentStrength);
                statType = "strength";
                increment = 1.0;
            } else if (slot == 12) { // Speed
                double currentSpeed = rpgManager.getSpeed(uuid);
                cost = calculateUpgradeCost(currentSpeed);
                statType = "speed";
                increment = 1.0;
            } else if (slot == 14) { // Agility
                double currentAgility = rpgManager.getAgility(uuid);
                cost = calculateUpgradeCost(currentAgility);
                statType = "agility";
                increment = 1.0;
            } else if (slot == 16) { // Health
                double currentMaxHealth = rpgManager.getMaxHealth(uuid);
                cost = calculateUpgradeCost(currentMaxHealth / 2);
                statType = "health";
                increment = 10.0;
            } else {
                return;
            }

            if (statType == null) return;

            // Check if player has enough money
            double playerBalance = economyManager.getDollar(uuid);
            if (playerBalance < cost) {
                player.sendMessage(Component.text("❌ Dollar tidak mencukupi! Dibutuhkan: $" + cost, NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            // Deduct money and upgrade
            economyManager.removeDollar(uuid, cost);
            
            switch (statType) {
                case "strength":
                    rpgManager.setStrength(uuid, rpgManager.getStrength(uuid) + increment);
                    break;
                case "speed":
                    rpgManager.setSpeed(uuid, rpgManager.getSpeed(uuid) + increment);
                    break;
                case "agility":
                    rpgManager.setAgility(uuid, rpgManager.getAgility(uuid) + increment);
                    break;
                case "health":
                    rpgManager.setMaxHealth(uuid, rpgManager.getMaxHealth(uuid) + increment);
                    break;
            }

            player.sendMessage(Component.text("✅ Status berhasil diupgrade!", NamedTextColor.GREEN, TextDecoration.BOLD));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            openStatusUpgradeGUI(player); // Refresh GUI

        } else if (title.toString().contains("Upgrade Equipment Legendaris")) {
            event.setCancelled(true);
            
            // Prevent clicking on info item (slot 22)
            if (event.getRawSlot() == 22) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK) {
                    player.sendMessage(Component.text("🔨 [Pandai Besi]: ", NamedTextColor.GOLD, TextDecoration.BOLD)
                            .append(Component.text("Masukkan senjata/armor di slot ini untuk upgrade!", NamedTextColor.YELLOW)));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                return;
            }
            
            int slot = event.getRawSlot();
            
            // Check which upgrade level was clicked
            String upgradeLevel = null;
            int cost = 0;
            int multiplier = 0;
            
            if (slot == 30) { // +1 Basic
                upgradeLevel = "+1 Basic";
                cost = 5000;
                multiplier = 2;
            } else if (slot == 31) { // +2 Advanced
                upgradeLevel = "+2 Advanced";
                cost = 15000;
                multiplier = 5;
            } else if (slot == 32) { // +3 Master
                upgradeLevel = "+3 Master";
                cost = 50000;
                multiplier = 10;
            } else if (slot == 39) { // +4 Legendary
                upgradeLevel = "+4 Legendary";
                cost = 150000;
                multiplier = 25;
            } else if (slot == 40 || slot == 41) { // +5 GODLIKE
                upgradeLevel = "+5 GODLIKE";
                cost = 500000;
                multiplier = 50;
            } else {
                return;
            }
            
            // Check if player has placed an item in slot 22 (center)
            ItemStack upgradeItem = event.getInventory().getItem(22);
            if (upgradeItem == null || upgradeItem.getType() == Material.ENCHANTED_BOOK) {
                player.sendMessage(Component.text("🔨 [Pandai Besi]: ", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .append(Component.text("Masukkan senjata/armor di slot tengah untuk upgrade!", NamedTextColor.YELLOW)));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            
            // Check if player has enough money
            double playerBalance = economyManager.getDollar(uuid);
            if (playerBalance < cost) {
                player.sendMessage(Component.text("❌ Dollar tidak mencukupi! Dibutuhkan: $" + cost, NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }
            
            // Deduct money and apply upgrade
            economyManager.removeDollar(uuid, cost);
            
            // Apply legendary upgrade using PersistentDataContainer
            ItemMeta meta = upgradeItem.getItemMeta();
            if (meta != null) {
                NamespacedKey upgradeKey = new NamespacedKey(plugin, "legendary_upgrade");
                NamespacedKey multiplierKey = new NamespacedKey(plugin, "legendary_multiplier");
                
                meta.getPersistentDataContainer().set(upgradeKey, PersistentDataType.STRING, upgradeLevel);
                meta.getPersistentDataContainer().set(multiplierKey, PersistentDataType.INTEGER, multiplier);
                
                // Add glow effect
                org.bukkit.enchantments.Enchantment enchantment = org.bukkit.enchantments.Enchantment.UNBREAKING;
                meta.addEnchant(enchantment, Math.max(1, 10 - multiplier), true);
                
                // Update item name to show upgrade level
                Component originalName = meta.displayName();
                if (originalName != null) {
                    meta.displayName(Component.text("⚡ " + upgradeLevel + " ", NamedTextColor.GOLD, TextDecoration.BOLD)
                            .append(originalName.color(NamedTextColor.WHITE)));
                } else {
                    meta.displayName(Component.text("⚡ " + upgradeLevel + " Item", NamedTextColor.GOLD, TextDecoration.BOLD));
                }
                
                // Add lore about the upgrade
                List<Component> lore = meta.lore();
                if (lore == null) lore = new ArrayList<>();
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                lore.add(Component.text("🔥 UPGRADE LEGENDARIS", NamedTextColor.GOLD, TextDecoration.BOLD));
                lore.add(Component.text("Multiplier: x" + multiplier, NamedTextColor.DARK_PURPLE));
                lore.add(Component.text("Efek: Damage/Armor meningkat " + multiplier + "x lipat!", NamedTextColor.YELLOW));
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                meta.lore(lore);
                
                upgradeItem.setItemMeta(meta);
                
                // Return upgraded item to player
                player.getInventory().addItem(upgradeItem);
                event.getInventory().setItem(22, new ItemStack(Material.ENCHANTED_BOOK)); // Reset slot
            }
            
            player.sendMessage(Component.text("✅ Equipment berhasil diupgrade ke " + upgradeLevel + "!", NamedTextColor.GREEN, TextDecoration.BOLD));
            player.sendMessage(Component.text("⚡ Multiplier: x" + multiplier + " - Efek di luar nalar!", NamedTextColor.GOLD));
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 0.5f);
            player.closeInventory();
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Component title = event.getView().title();
        if (title.toString().contains("Upgrade Equipment Legendaris")) {
            // Allow dragging items to slot 22 only
            event.setCancelled(true);
        }
    }
}
