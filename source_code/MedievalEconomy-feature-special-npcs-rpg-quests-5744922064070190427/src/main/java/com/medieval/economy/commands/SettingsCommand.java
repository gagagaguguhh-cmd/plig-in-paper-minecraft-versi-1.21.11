package com.medieval.economy.commands;

import com.medieval.economy.managers.ScoreboardManager;
import com.medieval.economy.managers.SettingsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SettingsCommand implements CommandExecutor {

    private final ScoreboardManager scoreboardManager;
    private final SettingsManager settingsManager;

    public SettingsCommand(ScoreboardManager scoreboardManager, SettingsManager settingsManager) {
        this.scoreboardManager = scoreboardManager;
        this.settingsManager = settingsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini cuma bisa dipakai player in-game!");
            return true;
        }

        openSettingsGUI(player);
        return true;
    }

    public void openSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("⚙️ Pengaturan Server & UI", NamedTextColor.DARK_GRAY));

        boolean sbEnabled = scoreboardManager.isEnabled(player.getUniqueId());
        boolean sellConfirm = settingsManager.isSellConfirmMode(player.getUniqueId().toString());
        boolean monstersEnabled = settingsManager.isMonstersEnabled(player.getUniqueId().toString());

        // Item Toggle Scoreboard (Slot 10)
        ItemStack sbItem = new ItemStack(sbEnabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta sbMeta = sbItem.getItemMeta();
        if (sbMeta != null) {
            sbMeta.displayName(Component.text("📊 Panel Layar Kanan (Scoreboard)", NamedTextColor.YELLOW, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
            lore.add(Component.text("Status: ", NamedTextColor.GRAY).append(
                    sbEnabled ? Component.text("AKTIF ✅", NamedTextColor.GREEN, TextDecoration.BOLD)
                              : Component.text("NONAKTIF ❌", NamedTextColor.RED, TextDecoration.BOLD)
            ));
            lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
            lore.add(Component.text("👉 Klik untuk " + (sbEnabled ? "mematikan" : "mengaktifkan") + " panel!", NamedTextColor.AQUA, TextDecoration.ITALIC));
            sbMeta.lore(lore);
            sbItem.setItemMeta(sbMeta);
        }
        gui.setItem(10, sbItem);

        // Item Toggle Monster (Slot 13)
        ItemStack monsterItem = new ItemStack(monstersEnabled ? Material.ZOMBIE_HEAD : Material.SKELETON_SKULL);
        ItemMeta monsterMeta = monsterItem.getItemMeta();
        if (monsterMeta != null) {
            monsterMeta.displayName(Component.text("🧟 Spawning Monster (Creeper, Zombie, dll)", NamedTextColor.RED, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
            lore.add(Component.text("Status Monster: ", NamedTextColor.GRAY).append(
                    monstersEnabled ? Component.text("MUNCUL ✅", NamedTextColor.GREEN, TextDecoration.BOLD)
                                    : Component.text("TIDAK MUNCUL ❌", NamedTextColor.RED, TextDecoration.BOLD)
            ));
            lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
            lore.add(Component.text("👉 Klik untuk " + (monstersEnabled ? "menonaktifkan" : "mengaktifkan") + " spawn monster di dekatmu!", NamedTextColor.AQUA, TextDecoration.ITALIC));
            monsterMeta.lore(lore);
            monsterItem.setItemMeta(monsterMeta);
        }
        gui.setItem(13, monsterItem);

        // Item Toggle Sell Confirmation (Slot 16)
        ItemStack sellItem = new ItemStack(sellConfirm ? Material.CHEST : Material.HOPPER);
        ItemMeta sellMeta = sellItem.getItemMeta();
        if (sellMeta != null) {
            sellMeta.displayName(Component.text("💰 Mode Penjualan (/sell)", NamedTextColor.GOLD, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
            lore.add(Component.text("Mode: ", NamedTextColor.GRAY).append(
                    sellConfirm ? Component.text("KONFIRMASI DULU 📝", NamedTextColor.GREEN, TextDecoration.BOLD)
                                : Component.text("LANGSUNG JUAL OTOMATIS ⚡", NamedTextColor.YELLOW, TextDecoration.BOLD)
            ));
            lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
            lore.add(Component.text("👉 Klik untuk ubah mode penjualan!", NamedTextColor.AQUA, TextDecoration.ITALIC));
            sellMeta.lore(lore);
            sellItem.setItemMeta(sellMeta);
        }
        gui.setItem(16, sellItem);

        // Decorate background with grey glass panes
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
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
}
