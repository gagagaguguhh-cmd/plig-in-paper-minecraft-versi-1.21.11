package com.medieval.economy.commands;

import com.medieval.economy.MedievalEconomyPlugin;
import com.medieval.economy.managers.RTPManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RTPCommand implements CommandExecutor {

    private final MedievalEconomyPlugin plugin;
    private final RTPManager rtpManager;
    private final Set<UUID> pendingTeleports = new HashSet<>();

    public RTPCommand(MedievalEconomyPlugin plugin, RTPManager rtpManager) {
        this.plugin = plugin;
        this.rtpManager = rtpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini cuma bisa dipakai player in-game!");
            return true;
        }

        World world = player.getWorld();

        if (!rtpManager.isDimensionUnlocked(world)) {
            player.sendMessage(Component.text("❌ Teleport acak di dimensi ini belum terbuka!", NamedTextColor.RED));
            player.sendMessage(Component.text("Dimensi ini baru bisa di-RTP setelah ada player yang berhasil pergi ke sini!", NamedTextColor.GRAY));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return true;
        }

        if (pendingTeleports.contains(player.getUniqueId())) {
            player.sendMessage(Component.text("⏳ Kamu sedang dalam proses teleportasi!", NamedTextColor.YELLOW));
            return true;
        }

        player.sendMessage(Component.text("🔍 Mencari lokasi aman dalam radius 5.000 blok...", NamedTextColor.YELLOW));
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);

        Location targetLoc = rtpManager.findSafeLocation(world, 5000);

        if (targetLoc == null) {
            player.sendMessage(Component.text("❌ Gagal menemukan lokasi aman. Coba lagi beberapa saat lagi!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return true;
        }

        pendingTeleports.add(player.getUniqueId());
        Location startLoc = player.getLocation().clone();

        player.sendMessage(Component.text("⌛ Teleportasi akan dilakukan dalam ", NamedTextColor.GREEN)
                .append(Component.text("3 detik", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(". Jangan bergerak!", NamedTextColor.GREEN)));

        new BukkitRunnable() {
            int countdown = 3;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    pendingTeleports.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                // Cek apakah player bergerak
                Location current = player.getLocation();
                if (startLoc.getWorld() != current.getWorld() ||
                    startLoc.getBlockX() != current.getBlockX() ||
                    startLoc.getBlockY() != current.getBlockY() ||
                    startLoc.getBlockZ() != current.getBlockZ()) {

                    player.sendMessage(Component.text("❌ Teleportasi dibatalkan karena kamu bergerak!", NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    pendingTeleports.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    player.sendMessage(Component.text("⏱ Teleportasi dalam ", NamedTextColor.GRAY)
                            .append(Component.text(countdown + "...", NamedTextColor.YELLOW, TextDecoration.BOLD)));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.2f);
                    countdown--;
                } else {
                    pendingTeleports.remove(player.getUniqueId());
                    player.teleportAsync(targetLoc);
                    player.sendMessage(Component.text("✨ Berhasil teleportasi secara acak ke koordinat: ", NamedTextColor.GREEN)
                            .append(Component.text(targetLoc.getBlockX() + ", " + targetLoc.getBlockY() + ", " + targetLoc.getBlockZ(), NamedTextColor.YELLOW, TextDecoration.BOLD)));
                    player.playSound(targetLoc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
                    player.playSound(targetLoc, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 1.0f);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }
}
