package com.medieval.economy.commands;

import com.medieval.economy.managers.EconomyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BalanceCommand implements CommandExecutor {

    private final EconomyManager economyManager;

    public BalanceCommand(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini cuma bisa dipakai player in-game!");
            return true;
        }

        if (args.length == 0) {
            double dollar = economyManager.getDollar(player.getUniqueId());
            double gold = economyManager.getGold(player.getUniqueId());
            player.sendMessage(Component.text("💵 Dompet Kamu: ", NamedTextColor.GREEN)
                    .append(Component.text(economyManager.formatDollar(dollar), NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(" | 🏆 ", NamedTextColor.GOLD))
                    .append(Component.text(economyManager.formatGold(gold), NamedTextColor.GOLD, TextDecoration.BOLD)));
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);
        UUID targetUUID = targetPlayer != null ? targetPlayer.getUniqueId() : economyManager.getUUIDByName(targetName);

        if (targetUUID == null) {
            player.sendMessage(Component.text("❌ Player '" + targetName + "' gak ditemukan!", NamedTextColor.RED));
            return true;
        }

        double dollar = economyManager.getDollar(targetUUID);
        double gold = economyManager.getGold(targetUUID);
        String name = targetPlayer != null ? targetPlayer.getName() : economyManager.getPlayerName(targetUUID);
        player.sendMessage(Component.text("💵 Dompet ", NamedTextColor.GREEN)
                .append(Component.text(name, NamedTextColor.YELLOW))
                .append(Component.text(": ", NamedTextColor.GREEN))
                .append(Component.text(economyManager.formatDollar(dollar), NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text(" | 🏆 ", NamedTextColor.GOLD))
                .append(Component.text(economyManager.formatGold(gold), NamedTextColor.GOLD, TextDecoration.BOLD)));

        return true;
    }
}
