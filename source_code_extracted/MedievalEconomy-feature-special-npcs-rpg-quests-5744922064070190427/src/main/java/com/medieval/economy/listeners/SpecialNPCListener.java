package com.medieval.economy.listeners;

import com.medieval.economy.MedievalEconomyPlugin;
import com.medieval.economy.managers.NPCManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class SpecialNPCListener implements Listener {

    private final MedievalEconomyPlugin plugin;
    private final NPCManager npcManager;
    private final NamespacedKey npcTypeKey;
    
    // KOORDINAT DESA TETAP - NPC HANYA SPAWN DI SINI
    private static final double VILLAGE_X = 687.0;
    private static final double VILLAGE_Y = 68.0;
    private static final double VILLAGE_Z = 278.0;
    private static final String VILLAGE_WORLD = "world";

    public SpecialNPCListener(MedievalEconomyPlugin plugin, NPCManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
        this.npcTypeKey = new NamespacedKey(plugin, "special_npc_type");

        startNPCSpawnerTask();
    }

    public NamespacedKey getNpcTypeKey() {
        return npcTypeKey;
    }

    private void startNPCSpawnerTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            World world = Bukkit.getWorld(VILLAGE_WORLD);
            if (world == null) {
                plugin.getLogger().warning("World '" + VILLAGE_WORLD + "' tidak ditemukan! NPC tidak bisa spawn.");
                return;
            }
            
            Location villageLocation = new Location(world, VILLAGE_X, VILLAGE_Y, VILLAGE_Z);
            
            // Cek apakah NPC sudah ada di desa
            boolean rpgNpcExists = false;
            boolean ecoNpcExists = false;
            
            for (Entity entity : world.getNearbyEntities(villageLocation, 10, 10, 10)) {
                if (entity instanceof Villager villager) {
                    PersistentDataContainer pdc = villager.getPersistentDataContainer();
                    if (pdc.has(npcTypeKey, PersistentDataType.STRING)) {
                        String type = pdc.get(npcTypeKey, PersistentDataType.STRING);
                        if (NPCManager.NPCType.RPG_STATS.name().equals(type)) {
                            rpgNpcExists = true;
                            // Update lokasi NPC di manager
                            npcManager.registerNPC(villager.getUniqueId(), NPCManager.NPCType.RPG_STATS, villageLocation, false);
                        } else if (NPCManager.NPCType.ECONOMIC_QUEST.name().equals(type)) {
                            ecoNpcExists = true;
                            npcManager.registerNPC(villager.getUniqueId(), NPCManager.NPCType.ECONOMIC_QUEST, villageLocation, false);
                        }
                    }
                }
            }
            
            // Spawn NPC di koordinat desa jika belum ada
            if (!rpgNpcExists) {
                Location spawnLoc = findSafeGroundLocation(villageLocation.clone());
                if (spawnLoc != null) {
                    spawnSpecialNPC(spawnLoc, NPCManager.NPCType.RPG_STATS);
                    plugin.getLogger().info("🧙 Tetua RPG Desa berhasil di-spawn di koordinat desa: " + 
                        spawnLoc.getBlockX() + ", " + spawnLoc.getBlockY() + ", " + spawnLoc.getBlockZ());
                }
            }

            if (!ecoNpcExists) {
                Location spawnLoc = findSafeGroundLocation(villageLocation.clone());
                if (spawnLoc != null) {
                    spawnSpecialNPC(spawnLoc, NPCManager.NPCType.ECONOMIC_QUEST);
                    plugin.getLogger().info("🌾 Pedagang Misi Desa berhasil di-spawn di koordinat desa: " + 
                        spawnLoc.getBlockX() + ", " + spawnLoc.getBlockY() + ", " + spawnLoc.getBlockZ());
                }
            }
            
            // Cleanup: Hapus NPC yang spawn di lokasi salah (bukan di desa)
            cleanupWrongNPCs(world);
            
        }, 100L, 600L); // run setiap 30 detik (lebih lama agar tidak spam)
    }
    
    private void cleanupWrongNPCs(World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Villager villager) {
                PersistentDataContainer pdc = villager.getPersistentDataContainer();
                if (pdc.has(npcTypeKey, PersistentDataType.STRING)) {
                    Location npcLoc = villager.getLocation();
                    double distanceToVillage = npcLoc.distance(new Location(world, VILLAGE_X, VILLAGE_Y, VILLAGE_Z));
                    
                    // Jika NPC lebih dari 50 blok dari desa, hapus (kecuali sedang dalam proses spawn)
                    if (distanceToVillage > 50) {
                        UUID npcUuid = villager.getUniqueId();
                        if (npcManager.isSpecialNPC(npcUuid)) {
                            plugin.getLogger().warning("⚠️ Menghapus NPC yang salah spawn di: " + 
                                npcLoc.getBlockX() + ", " + npcLoc.getBlockY() + ", " + npcLoc.getBlockZ() + 
                                " (Jarak dari desa: " + (int)distanceToVillage + "m)");
                            villager.remove();
                            npcManager.unregisterNPC(npcUuid);
                        }
                    }
                }
            }
        }
    }

    private Location findSafeGroundLocation(Location origin) {
        // Cari Y level tertinggi di koordinat X,Z desa
        int highestY = origin.getWorld().getHighestBlockYAt(origin);
        origin.setY(highestY + 1);
        
        // Pastikan lokasi aman (blok udara, bukan di dalam tanah/air)
        if (origin.getBlock().getType().isAir()) {
            return origin;
        }
        
        // Jika tidak aman, coba naik beberapa blok
        for (int i = 1; i <= 5; i++) {
            Location testLoc = origin.clone();
            testLoc.setY(highestY + i);
            if (testLoc.getBlock().getType().isAir() && 
                testLoc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                return testLoc;
            }
        }
        
        return origin; // Fallback ke lokasi asli
    }

    public Villager spawnSpecialNPC(Location location, NPCManager.NPCType type) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setPersistent(true);
        villager.setRemoveWhenFarAway(false);
        villager.setAI(false); // Nonaktifkan AI agar NPC tidak berjalan kemana-mana

        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        pdc.set(npcTypeKey, PersistentDataType.STRING, type.name());

        if (type == NPCManager.NPCType.RPG_STATS) {
            villager.customName(Component.text("🧙 Tetua RPG Desa", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
            villager.setProfession(Villager.Profession.CLERIC);
            villager.setGlowing(true);
        } else {
            villager.customName(Component.text("🌾 Pedagang Misi Desa", NamedTextColor.GOLD, TextDecoration.BOLD));
            villager.setProfession(Villager.Profession.FARMER);
            villager.setGlowing(true);
        }
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true); // NPC tidak bisa dibunuh

        // Simpan lokasi desa sebagai home location, bukan lokasi spawn aktual
        Location villageLocation = new Location(location.getWorld(), VILLAGE_X, VILLAGE_Y, VILLAGE_Z);
        npcManager.registerNPC(villager.getUniqueId(), type, villageLocation, true);
        
        return villager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;

        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        if (!pdc.has(npcTypeKey, PersistentDataType.STRING)) return;

        String typeStr = pdc.get(npcTypeKey, PersistentDataType.STRING);
        if (event.getDamager() instanceof Player attacker) {
            // Retaliation mechanic!
            String npcName = typeStr.equals(NPCManager.NPCType.RPG_STATS.name()) ? "Tetua RPG Desa" : "Pedagang Misi Desa";

            attacker.sendMessage(Component.text("⚔️ [" + npcName + "]: ", NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("Jangan serang aku! Rasakan perlawananku!", NamedTextColor.YELLOW)));

            // Play sound
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);

            // Knockback player away
            Vector direction = attacker.getLocation().toVector().subtract(villager.getLocation().toVector()).normalize();
            direction.setY(0.4);
            attacker.setVelocity(direction.multiply(1.2));

            // Deal retaliation damage
            attacker.damage(4.0, villager);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            UUID uuid = villager.getUniqueId();
            if (npcManager.isSpecialNPC(uuid)) {
                npcManager.unregisterNPC(uuid);
            }
        }
    }
}
