package com.medieval.economy.listeners;

import com.medieval.economy.managers.SettingsManager;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MonsterToggleListener implements Listener {

    private final SettingsManager settingsManager;

    public MonsterToggleListener(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Only target hostile monsters (Zombie, Skeleton, Creeper, Spider, etc.)
        if (event.getEntity() instanceof Monster) {
            // Check if any nearby player within 32 blocks has disabled monsters
            for (Player player : event.getLocation().getNearbyPlayers(32.0)) {
                if (!settingsManager.isMonstersEnabled(player.getUniqueId().toString())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Monster && event.getTarget() instanceof Player player) {
            if (!settingsManager.isMonstersEnabled(player.getUniqueId().toString())) {
                event.setCancelled(true);
            }
        }
    }
}
