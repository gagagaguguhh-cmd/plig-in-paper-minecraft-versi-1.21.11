package com.medieval.economy.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandBlockerListener implements Listener {

    // Daftar command resmi plugin MedievalEconomy yang diizinkan untuk player biasa
    private final Set<String> allowedCommands = new HashSet<>(Arrays.asList(
            "balance", "bal", "uang", "dompet",
            "pay", "transfer", "tf",
            "akun", "profile", "akunku",
            "shop", "toko",
            "sell", "jual",
            "auctions", "ah", "lelang",
            "order", "orders", "misi",
            "settings", "setting", "setelan"
    ));

    // Filter daftar command saat player mengetik '/' (Tab-Completion)
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return; // Admin/OP tetap bisa akses semua command

        event.getCommands().removeIf(command -> !allowedCommands.contains(command.toLowerCase()));
    }

    // Blokir eksekusi command jika player mengetik command di luar daftar diizinkan
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return; // Admin/OP tetap bisa menjalankan semua command

        String message = event.getMessage().substring(1).trim();
        if (message.isEmpty()) return;

        String mainCommand = message.split(" ")[0].toLowerCase();
        // Hapus namespace jika ada (contoh: minecraft:me -> me)
        if (mainCommand.contains(":")) {
            mainCommand = mainCommand.substring(mainCommand.indexOf(":") + 1);
        }

        if (!allowedCommands.contains(mainCommand)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("❌ Perintah ini tidak diizinkan di server ini!", NamedTextColor.RED));
        }
    }
}
