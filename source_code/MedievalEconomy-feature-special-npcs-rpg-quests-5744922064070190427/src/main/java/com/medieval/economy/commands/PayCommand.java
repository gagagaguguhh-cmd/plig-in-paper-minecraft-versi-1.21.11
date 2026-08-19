package com.medieval.economy.commands;

import com.medieval.economy.managers.EconomyManager;
import com.medieval.economy.managers.ScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayCommand implements CommandExecutor {

    private final EconomyManager economyManager;
    private final ScoreboardManager scoreboardManager;

    public PayCommand(EconomyManager economyManager, ScoreboardManager scoreboardManager) {
        this.economyManager = economyManager;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini cuma bisa dipakai player in-game!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("⚠️ Gunakan format: /pay <player> <jumlah>", NamedTextColor.YELLOW));
            return true;
        }

        String targetName = args[0];
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(Component.text("😂 Mana bisa transfer uang ke diri sendiri, kocak!", NamedTextColor.RED));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("❌ Jumlah Dollar harus berupa angka positif!", NamedTextColor.RED));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(Component.text("❌ Jumlah Dollar harus lebih besar dari 0!", NamedTextColor.RED));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        UUID targetUUID = targetPlayer != null ? targetPlayer.getUniqueId() : economyManager.getUUIDByName(targetName);

        if (targetUUID == null) {
            player.sendMessage(Component.text("❌ Player '" + targetName + "' gak ditemukan!", NamedTextColor.RED));
            return true;
        }

        if (!economyManager.hasDollar(player.getUniqueId(), amount)) {
            player.sendMessage(Component.text("❌ Uang kamu gak cukup buat transfer ", NamedTextColor.RED)
                    .append(Component.text(economyManager.formatDollar(amount), NamedTextColor.YELLOW)));
            return true;
        }

        economyManager.withdrawDollar(player.getUniqueId(), amount);
        economyManager.addDollar(targetUUID, amount);

        scoreboardManager.updateScoreboard(player);

        String name = targetPlayer != null ? targetPlayer.getName() : economyManager.getPlayerName(targetUUID);

        player.sendMessage(Component.text("✅ Berhasil kirim ", NamedTextColor.GREEN)
                .append(Component.text(economyManager.formatDollar(amount), NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" ke ", NamedTextColor.GREEN))
                .append(Component.text(name, NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.GREEN)));

        if (targetPlayer != null && targetPlayer.isOnline()) {
            scoreboardManager.updateScoreboard(targetPlayer);
            targetPlayer.sendMessage(Component.text("🎉 Kamu dapat transfer ", NamedTextColor.GREEN)
                    .append(Component.text(economyManager.formatDollar(amount), NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text(" dari ", NamedTextColor.GREEN))
                    .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                    .append(Component.text("!", NamedTextColor.GREEN)));
        }

        return true;
    }
}
