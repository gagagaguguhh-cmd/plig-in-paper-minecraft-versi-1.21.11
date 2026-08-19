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
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation();
                // Check if there is an RPG_STATS NPC nearby
                boolean hasRpgNpcNearby = false;
                boolean hasEcoNpcNearby = false;

                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 150, 150, 150)) {
                    if (entity instanceof Villager villager) {
                        PersistentDataContainer pdc = villager.getPersistentDataContainer();
                        if (pdc.has(npcTypeKey, PersistentDataType.STRING)) {
                            String type = pdc.get(npcTypeKey, PersistentDataType.STRING);
                            if (NPCManager.NPCType.RPG_STATS.name().equals(type)) {
                                hasRpgNpcNearby = true;
                                npcManager.registerNPC(villager.getUniqueId(), NPCManager.NPCType.RPG_STATS, villager.getLocation(), false);
                            } else if (NPCManager.NPCType.ECONOMIC_QUEST.name().equals(type)) {
                                hasEcoNpcNearby = true;
                                npcManager.registerNPC(villager.getUniqueId(), NPCManager.NPCType.ECONOMIC_QUEST, villager.getLocation(), false);
                            }
                        }
                    }
                }

                // If no RPG_STATS NPC nearby, spawn one near player in a safe ground location
                if (!hasRpgNpcNearby) {
                    Location spawnLoc = findSafeGroundLocation(loc.clone().add(randomOffset(), 0, randomOffset()));
                    if (spawnLoc != null) {
                        spawnSpecialNPC(spawnLoc, NPCManager.NPCType.RPG_STATS);
                    }
                }

                // If no ECONOMIC_QUEST NPC nearby, spawn one
                if (!hasEcoNpcNearby) {
                    Location spawnLoc = findSafeGroundLocation(loc.clone().add(randomOffset(), 0, randomOffset()));
                    if (spawnLoc != null) {
                        spawnSpecialNPC(spawnLoc, NPCManager.NPCType.ECONOMIC_QUEST);
                    }
                }
            }
        }, 100L, 300L); // run every 15 seconds
    }

    private double randomOffset() {
        return (Math.random() - 0.5) * 60 + (Math.random() < 0.5 ? -30 : 30);
    }

    private Location findSafeGroundLocation(Location origin) {
        origin.setY(origin.getWorld().getHighestBlockYAt(origin) + 1);
        if (origin.getBlock().getType().isAir()) {
            return origin;
        }
        return null;
    }

    public Villager spawnSpecialNPC(Location location, NPCManager.NPCType type) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setPersistent(true);
        villager.setRemoveWhenFarAway(false);

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

        npcManager.registerNPC(villager.getUniqueId(), type, location, true);
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
