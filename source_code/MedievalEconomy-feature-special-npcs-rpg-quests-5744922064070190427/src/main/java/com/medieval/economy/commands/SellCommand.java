package com.medieval.economy.commands;

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

public class SellCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini cuma bisa dipakai player in-game!");
            return true;
        }

        openSellGUI(player);
        return true;
    }

    public void openSellGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, Component.text("💰 TARUH BARANG UNTUK DIJUAL", NamedTextColor.DARK_GREEN));

        // Tombol Masukkan Semua Barang (Slot 29)
        ItemStack addAll = new ItemStack(Material.CHEST_MINECART);
        ItemMeta addAllMeta = addAll.getItemMeta();
        if (addAllMeta != null) {
            addAllMeta.displayName(Component.text("📥 MASUKKAN SEMUA BARANG", NamedTextColor.AQUA, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Otomatis memindahkan semua barang yang", NamedTextColor.YELLOW));
            lore.add(Component.text("bisa dijual dari tas kamu ke GUI ini!", NamedTextColor.YELLOW));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            addAllMeta.lore(lore);
            addAll.setItemMeta(addAllMeta);
        }
        gui.setItem(29, addAll);

        // Tombol Konfirmasi Jual (Slot 31)
        ItemStack confirm = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(Component.text("✅ PROSES PENJUALAN", NamedTextColor.GREEN, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Klik untuk memproses dan menjual", NamedTextColor.YELLOW));
            lore.add(Component.text("semua barang yang ada di GUI ini!", NamedTextColor.YELLOW));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            confirmMeta.lore(lore);
            confirm.setItemMeta(confirmMeta);
        }
        gui.setItem(31, confirm);

        // Tombol Kembalikan Semua Barang (Slot 33)
        ItemStack removeAll = new ItemStack(Material.HOPPER);
        ItemMeta removeAllMeta = removeAll.getItemMeta();
        if (removeAllMeta != null) {
            removeAllMeta.displayName(Component.text("📤 KOSONGKAN / KEMBALIKAN", NamedTextColor.RED, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Kembalikan semua item dari GUI", NamedTextColor.YELLOW));
            lore.add(Component.text("ke dalam tas inventory kamu!", NamedTextColor.YELLOW));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            removeAllMeta.lore(lore);
            removeAll.setItemMeta(removeAllMeta);
        }
        gui.setItem(33, removeAll);

        // Bingkai bawah GUI
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.text(" "));
            border.setItemMeta(borderMeta);
        }
        for (int i = 27; i < 36; i++) {
            if (i != 29 && i != 31 && i != 33) {
                gui.setItem(i, border);
            }
        }

        player.openInventory(gui);
    }
}
