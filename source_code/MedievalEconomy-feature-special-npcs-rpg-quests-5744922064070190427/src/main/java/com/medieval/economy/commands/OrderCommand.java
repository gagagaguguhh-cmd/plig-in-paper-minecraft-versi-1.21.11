package com.medieval.economy.commands;

import com.medieval.economy.managers.EconomyManager;
import com.medieval.economy.managers.OrderManager;
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

public class OrderCommand implements CommandExecutor {

    private final OrderManager orderManager;
    private final EconomyManager economyManager;

    public OrderCommand(OrderManager orderManager, EconomyManager economyManager) {
        this.orderManager = orderManager;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini cuma bisa dipakai player in-game!");
            return true;
        }

        // /order create <imbalan_dollar>
        if (args.length > 0 && args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage(Component.text("⚠️ Gunakan format: /order create <imbalan_dollar>", NamedTextColor.YELLOW));
                return true;
            }

            double reward;
            try {
                reward = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("❌ Imbalan Dollar harus berupa angka positif!", NamedTextColor.RED));
                return true;
            }

            if (reward <= 0) {
                player.sendMessage(Component.text("❌ Imbalan Dollar harus lebih besar dari 0!", NamedTextColor.RED));
                return true;
            }

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType().isAir()) {
                player.sendMessage(Component.text("❌ Pegang barang yang mau kamu minta/order di tangan utama!", NamedTextColor.RED));
                return true;
            }

            if (!economyManager.hasDollar(player.getUniqueId(), reward)) {
                player.sendMessage(Component.text("❌ Dollar kamu gak cukup buat bayar imbalan ", NamedTextColor.RED)
                        .append(Component.text(economyManager.formatDollar(reward), NamedTextColor.YELLOW)));
                return true;
            }

            ItemStack requested = itemInHand.clone();

            economyManager.withdrawDollar(player.getUniqueId(), reward);

            OrderManager.OrderListing order = orderManager.createOrder(player.getUniqueId(), player.getName(), requested, reward);

            player.sendMessage(Component.text("📋 Berhasil membuat Pesanan/Order ", NamedTextColor.GREEN)
                    .append(Component.text(requested.getAmount() + "x " + requested.getType().name(), NamedTextColor.YELLOW))
                    .append(Component.text(" dengan imbalan ", NamedTextColor.GREEN))
                    .append(Component.text(economyManager.formatDollar(reward), NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text("! (ID: " + order.id() + ")", NamedTextColor.GRAY)));

            return true;
        }

        openOrderGUI(player, 1);
        return true;
    }

    public void openOrderGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, Component.text("📋 Papan Pesanan / Order Hal." + page, NamedTextColor.DARK_AQUA));

        List<OrderManager.OrderListing> orders = orderManager.getOrders();
        int itemsPerPage = 36;
        int maxPages = Math.max(1, (int) Math.ceil((double) orders.size() / itemsPerPage));
        int currentPage = Math.min(Math.max(1, page), maxPages);

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, orders.size());

        for (int i = startIndex; i < endIndex; i++) {
            OrderManager.OrderListing order = orders.get(i);
            ItemStack displayItem = order.requestedItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(Component.text(" ", NamedTextColor.WHITE));
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                lore.add(Component.text("👤 Pemesan: ", NamedTextColor.GRAY).append(Component.text(order.requesterName(), NamedTextColor.YELLOW)));
                lore.add(Component.text("💵 Imbalan: ", NamedTextColor.GRAY).append(Component.text(economyManager.formatDollar(order.rewardDollar()), NamedTextColor.GOLD, TextDecoration.BOLD)));
                lore.add(Component.text("🆔 ID Order: ", NamedTextColor.DARK_GRAY).append(Component.text(order.id(), NamedTextColor.GRAY)));
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                if (order.requesterUUID().equals(player.getUniqueId())) {
                    lore.add(Component.text("👉 Klik untuk MEMBATALKAN order & mengambil Dollar!", NamedTextColor.RED, TextDecoration.ITALIC));
                } else {
                    lore.add(Component.text("👉 Klik untuk MENYELESAIKAN pesanan ini!", NamedTextColor.GREEN, TextDecoration.ITALIC));
                }
                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.setItem(i - startIndex, displayItem);
        }

        // Navigasi
        if (currentPage > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            if (prevMeta != null) {
                prevMeta.displayName(Component.text("⬅️ Halaman Sebelumnya (" + (currentPage - 1) + ")", NamedTextColor.YELLOW, TextDecoration.BOLD));
                prev.setItemMeta(prevMeta);
            }
            gui.setItem(48, prev);
        }

        ItemStack infoPage = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoPage.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Component.text("📄 Halaman " + currentPage + " / " + maxPages, NamedTextColor.GOLD, TextDecoration.BOLD));
            infoPage.setItemMeta(infoMeta);
        }
        gui.setItem(49, infoPage);

        if (currentPage < maxPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.displayName(Component.text("➡️ Halaman Selanjutnya (" + (currentPage + 1) + ")", NamedTextColor.YELLOW, TextDecoration.BOLD));
                next.setItemMeta(nextMeta);
            }
            gui.setItem(50, next);
        }

        ItemStack filler = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Component.text(" "));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 36; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }
}
