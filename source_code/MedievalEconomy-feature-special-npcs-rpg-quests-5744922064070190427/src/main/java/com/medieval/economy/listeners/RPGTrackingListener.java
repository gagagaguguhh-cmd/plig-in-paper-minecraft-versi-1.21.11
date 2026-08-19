package com.medieval.economy.listeners;

import com.medieval.economy.MedievalEconomyPlugin;
import com.medieval.economy.managers.NPCManager;
import com.medieval.economy.managers.RPGManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class RPGTrackingListener implements Listener {

    private final MedievalEconomyPlugin plugin;
    private final RPGManager rpgManager;
    private final NPCManager npcManager;

    public RPGTrackingListener(MedievalEconomyPlugin plugin, RPGManager rpgManager, NPCManager npcManager) {
        this.plugin = plugin;
        this.rpgManager = rpgManager;
        this.npcManager = npcManager;

        startRealtimeTrackerTask();
    }

    private void startRealtimeTrackerTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                if (!rpgManager.isInitiated(uuid)) {
                    NPCManager.SpecialNPC rpgNpc = npcManager.getNearestRPGStatsNPC(player.getLocation());
                    if (rpgNpc != null && rpgNpc.getHomeLocation() != null) {
                        Location loc = rpgNpc.getHomeLocation();
                        int distance = (int) player.getLocation().distance(loc);
                        Component actionbar = Component.text("📍 Temui Tetua RPG di Desa: ", NamedTextColor.GOLD, TextDecoration.BOLD)
                                .append(Component.text("X: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ(), NamedTextColor.YELLOW))
                                .append(Component.text(" (Jarak: " + distance + "m)", NamedTextColor.AQUA));
                        player.sendActionBar(actionbar);
                    } else {
                        Component actionbar = Component.text("📍 Cari Desa Terdekat untuk Menemui Tetua RPG Desa!", NamedTextColor.GOLD, TextDecoration.BOLD);
                        player.sendActionBar(actionbar);
                    }
                }
            }
        }, 20L, 20L); // Repeat every second
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!rpgManager.isInitiated(player.getUniqueId())) {
            player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
            player.sendMessage(Component.text("📜 SELAMAT DATANG DI MEDIEVAL ECONOMY!", NamedTextColor.GOLD, TextDecoration.BOLD));
            player.sendMessage(Component.text("Kamu belum menginisiasi petualangan RPG-mu.", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Silakan ikuti penunjuk arah realtime di ActionBar-mu untuk menemui ", NamedTextColor.GRAY)
                    .append(Component.text("Tetua RPG Desa", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                    .append(Component.text("!", NamedTextColor.GRAY)));
            player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String cmd = event.getMessage().toLowerCase();

        // Lock /akun command if uninitiated
        if ((cmd.startsWith("/akun") || cmd.startsWith("/profile")) && !rpgManager.isInitiated(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("❌ Fitur Profil Akun terkunci!", NamedTextColor.RED, TextDecoration.BOLD));
            player.sendMessage(Component.text("Kamu harus menemui ", NamedTextColor.GRAY)
                    .append(Component.text("Tetua RPG Desa", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
                    .append(Component.text(" terlebih dahulu di koordinat yang tertera di ActionBar!", NamedTextColor.GRAY)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
}
