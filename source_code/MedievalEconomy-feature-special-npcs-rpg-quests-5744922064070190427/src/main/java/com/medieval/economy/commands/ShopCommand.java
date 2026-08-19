package com.medieval.economy.commands;

import com.medieval.economy.managers.EconomyManager;
import com.medieval.economy.managers.ShopManager;
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

public class ShopCommand implements CommandExecutor {

    private final ShopManager shopManager;
    private final EconomyManager economyManager;

    public ShopCommand(ShopManager shopManager, EconomyManager economyManager) {
        this.shopManager = shopManager;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini cuma bisa dipakai player in-game!");
            return true;
        }

        openShopMainMenu(player);
        return true;
    }

    public void openShopMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, Component.text("TOKO SERVER", NamedTextColor.GREEN, TextDecoration.BOLD));

        int[] slots = {11, 13, 15, 20, 24};
        ShopManager.ShopCategory[] categories = ShopManager.ShopCategory.values();

        for (int i = 0; i < categories.length && i < slots.length; i++) {
            ShopManager.ShopCategory cat = categories[i];
            ItemStack item = new ItemStack(cat.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(cat.getDisplayName(), NamedTextColor.YELLOW, TextDecoration.BOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text(cat.getDescription(), NamedTextColor.GRAY));
                lore.add(Component.text(" ", NamedTextColor.GRAY));
                lore.add(Component.text("👉 Klik untuk melihat barang!", NamedTextColor.AQUA, TextDecoration.ITALIC));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(slots[i], item);
        }

        ItemStack border = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.text(" "));
            border.setItemMeta(borderMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, border);
            }
        }

        player.openInventory(gui);
    }

    public void openCategoryMenu(Player player, ShopManager.ShopCategory category, int page) {
        List<ShopManager.ShopItem> items = shopManager.getItemsByCategory(category);
        int itemsPerPage = 28;
        int maxPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
        int currentPage = Math.max(1, Math.min(page, maxPages));

        Inventory gui = Bukkit.createInventory(null, 54, Component.text("Toko: " + category.getDisplayName() + " (Hal. " + currentPage + "/" + maxPages + ")", NamedTextColor.DARK_GREEN));

        // Header Navigation Bar (Slots 0-8)
        ShopManager.ShopCategory[] categories = ShopManager.ShopCategory.values();
        int[] categorySlots = {1, 2, 3, 4, 5};
        for (int i = 0; i < categories.length && i < categorySlots.length; i++) {
            ShopManager.ShopCategory cat = categories[i];
            boolean isCurrent = (cat == category);
            ItemStack tab = new ItemStack(cat.getIcon());
            ItemMeta meta = tab.getItemMeta();
            if (meta != null) {
                if (isCurrent) {
                    meta.displayName(Component.text(cat.getDisplayName(), NamedTextColor.GREEN, TextDecoration.BOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("[Kategori Aktif]", NamedTextColor.AQUA, TextDecoration.ITALIC));
                    meta.lore(lore);
                } else {
                    meta.displayName(Component.text(cat.getDisplayName(), NamedTextColor.GRAY));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("👉 Klik untuk pindah kategori", NamedTextColor.YELLOW, TextDecoration.ITALIC));
                    meta.lore(lore);
                }
                tab.setItemMeta(meta);
            }
            gui.setItem(categorySlots[i], tab);
        }

        // Header border line (Slot 0, 6, 7, 8)
        ItemStack headerBorder = new ItemStack(Material.DARK_OAK_HANGING_SIGN);
        ItemMeta hbMeta = headerBorder.getItemMeta();
        if (hbMeta != null) {
            hbMeta.displayName(Component.text(" "));
            headerBorder.setItemMeta(hbMeta);
        }
        gui.setItem(0, headerBorder);
        gui.setItem(6, headerBorder);
        gui.setItem(7, headerBorder);
        gui.setItem(8, headerBorder);

        // Display Items (Slots 10-16, 19-25, 28-34, 37-43 -> 28 slots)
        int[] itemSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int startIndex = (currentPage - 1) * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex >= items.size()) break;

            ShopManager.ShopItem shopItem = items.get(itemIndex);
            ItemStack item = shopItem.createItemStack(1);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(shopItem.displayName(), NamedTextColor.YELLOW, TextDecoration.BOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Harga Satuan: ", NamedTextColor.GRAY)
                        .append(Component.text(economyManager.formatDollar(shopItem.buyPrice()), NamedTextColor.GREEN, TextDecoration.BOLD)));
                lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
                lore.add(Component.text("👉 Klik untuk mengatur jumlah pembelian!", NamedTextColor.AQUA, TextDecoration.ITALIC));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(itemSlots[i], item);
        }

        // Navigation Footer (Slots 45 - 53)
        // Tombol Hal. Sebelum (Slot 48)
        if (currentPage > 1) {
            ItemStack prev = new ItemStack(Material.PAPER);
            ItemMeta prevMeta = prev.getItemMeta();
            if (prevMeta != null) {
                prevMeta.displayName(Component.text("◀ Halaman Sebelum", NamedTextColor.YELLOW, TextDecoration.BOLD));
                prev.setItemMeta(prevMeta);
            }
            gui.setItem(48, prev);
        }

        // Tombol Kembali ke Menu Utama Toko (Slot 49)
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(Component.text("Kembali ke Utama", NamedTextColor.RED, TextDecoration.BOLD));
            back.setItemMeta(backMeta);
        }
        gui.setItem(49, back);

        // Tombol Hal. Berikutnya (Slot 50)
        if (currentPage < maxPages) {
            ItemStack next = new ItemStack(Material.PAPER);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.displayName(Component.text("Halaman Berikutnya ▶", NamedTextColor.YELLOW, TextDecoration.BOLD));
                next.setItemMeta(nextMeta);
            }
            gui.setItem(50, next);
        }

        // Fill remaining empty slots with dark stained glass
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

    public void openQuantityMenu(Player player, ShopManager.ShopItem shopItem, int quantity) {
        int qty = Math.max(1, Math.min(64, quantity));
        double totalPrice = shopItem.buyPrice() * qty;

        Inventory gui = Bukkit.createInventory(null, 36, Component.text("Beli: " + shopItem.displayName(), NamedTextColor.DARK_GREEN));

        // Display Item Center (Slot 13)
        ItemStack itemDisplay = shopItem.createItemStack(qty);
        ItemMeta displayMeta = itemDisplay.getItemMeta();
        if (displayMeta != null) {
            displayMeta.displayName(Component.text(shopItem.displayName(), NamedTextColor.GOLD, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
            lore.add(Component.text("Jumlah Dipilih: ", NamedTextColor.GRAY)
                    .append(Component.text(qty + "x", NamedTextColor.YELLOW, TextDecoration.BOLD)));
            lore.add(Component.text("Total Harga: ", NamedTextColor.GRAY)
                    .append(Component.text(economyManager.formatDollar(totalPrice), NamedTextColor.GREEN, TextDecoration.BOLD)));
            lore.add(Component.text("-----------------------", NamedTextColor.GRAY));
            displayMeta.lore(lore);
            itemDisplay.setItemMeta(displayMeta);
        }
        gui.setItem(13, itemDisplay);

        // Decrement Buttons (-64, -16, -1) on Slots 10, 11, 12
        gui.setItem(10, createModifierButton(Material.RED_STAINED_GLASS_PANE, "-64", NamedTextColor.RED));
        gui.setItem(11, createModifierButton(Material.RED_STAINED_GLASS_PANE, "-16", NamedTextColor.RED));
        gui.setItem(12, createModifierButton(Material.RED_STAINED_GLASS_PANE, "-1", NamedTextColor.RED));

        // Increment Buttons (+1, +16, +64) on Slots 14, 15, 16
        gui.setItem(14, createModifierButton(Material.LIME_STAINED_GLASS_PANE, "+1", NamedTextColor.GREEN));
        gui.setItem(15, createModifierButton(Material.LIME_STAINED_GLASS_PANE, "+16", NamedTextColor.GREEN));
        gui.setItem(16, createModifierButton(Material.LIME_STAINED_GLASS_PANE, "+64", NamedTextColor.GREEN));

        // Confirm Purchase (Slot 29)
        ItemStack confirm = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(Component.text("✅ KONFIRMASI BELI (" + qty + "x)", NamedTextColor.GREEN, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Total Bayar: ", NamedTextColor.GRAY)
                    .append(Component.text(economyManager.formatDollar(totalPrice), NamedTextColor.GREEN, TextDecoration.BOLD)));
            confirmMeta.lore(lore);
            confirm.setItemMeta(confirmMeta);
        }
        gui.setItem(29, confirm);

        // Cancel / Back (Slot 33)
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(Component.text("❌ BATAL / KEMBALI", NamedTextColor.RED, TextDecoration.BOLD));
            cancel.setItemMeta(cancelMeta);
        }
        gui.setItem(33, cancel);

        // Fill remaining empty slots
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
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

    private ItemStack createModifierButton(Material mat, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, color, TextDecoration.BOLD));
            item.setItemMeta(meta);
        }
        return item;
    }
}
