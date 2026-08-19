package com.medieval.economy.listeners;

import com.medieval.economy.commands.SettingsCommand;
import com.medieval.economy.commands.ShopCommand;
import com.medieval.economy.managers.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final EconomyManager economyManager;
    private final ShopManager shopManager;
    private final SellManager sellManager;
    private final AuctionManager auctionManager;
    private final OrderManager orderManager;
    private final ScoreboardManager scoreboardManager;
    private final SettingsManager settingsManager;

    private final Map<UUID, List<ItemStack>> pendingSellItems = new HashMap<>();

    public InventoryClickListener(EconomyManager economyManager, ShopManager shopManager, SellManager sellManager, AuctionManager auctionManager, OrderManager orderManager, ScoreboardManager scoreboardManager, SettingsManager settingsManager) {
        this.economyManager = economyManager;
        this.shopManager = shopManager;
        this.sellManager = sellManager;
        this.auctionManager = auctionManager;
        this.orderManager = orderManager;
        this.scoreboardManager = scoreboardManager;
        this.settingsManager = settingsManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title() == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // 1. Profil & Ringkasan Akun
        if (title.contains("Profil & Ringkasan Akun")) {
            event.setCancelled(true);
            return;
        }

        // 2. Pengaturan Server & UI (/settings)
        if (title.contains("Pengaturan Server & UI")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            if (slot == 10) {
                boolean current = scoreboardManager.isEnabled(player.getUniqueId());
                boolean next = !current;
                scoreboardManager.setEnabled(player.getUniqueId(), next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                new SettingsCommand(scoreboardManager, settingsManager).openSettingsGUI(player);
            } else if (slot == 13) {
                boolean current = settingsManager.isMonstersEnabled(player.getUniqueId().toString());
                boolean next = !current;
                settingsManager.setMonstersEnabled(player.getUniqueId().toString(), next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                new SettingsCommand(scoreboardManager, settingsManager).openSettingsGUI(player);
            } else if (slot == 16) {
                boolean current = settingsManager.isSellConfirmMode(player.getUniqueId().toString());
                boolean next = !current;
                settingsManager.setSellConfirmMode(player.getUniqueId().toString(), next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                new SettingsCommand(scoreboardManager, settingsManager).openSettingsGUI(player);
            }
            return;
        }

        // 3. Toko Server - Kategori Utama
        if (title.equals("TOKO SERVER")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            ShopManager.ShopCategory selectedCategory = null;

            if (slot == 11) selectedCategory = ShopManager.ShopCategory.FOOD;
            else if (slot == 13) selectedCategory = ShopManager.ShopCategory.BUILDING;
            else if (slot == 15) selectedCategory = ShopManager.ShopCategory.MINING;
            else if (slot == 20) selectedCategory = ShopManager.ShopCategory.EQUIPMENT;
            else if (slot == 24) selectedCategory = ShopManager.ShopCategory.MAGIC;

            if (selectedCategory != null) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
                new ShopCommand(shopManager, economyManager).openCategoryMenu(player, selectedCategory, 1);
            }
            return;
        }

        // 4. Sub-Menu Toko Kategori (Toko: <Kategori> (Hal. X/Y))
        if (title.startsWith("Toko: ")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            // Header bar category switching (Slots 1-5)
            int[] categorySlots = {1, 2, 3, 4, 5};
            ShopManager.ShopCategory[] categories = ShopManager.ShopCategory.values();
            for (int i = 0; i < categorySlots.length && i < categories.length; i++) {
                if (slot == categorySlots[i]) {
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
                    new ShopCommand(shopManager, economyManager).openCategoryMenu(player, categories[i], 1);
                    return;
                }
            }

            int currentPage = 1;
            try {
                String pageStr = title.substring(title.indexOf("Hal.") + 4, title.indexOf("/"));
                currentPage = Integer.parseInt(pageStr.trim());
            } catch (Exception ignored) {}

            ShopManager.ShopCategory matchedCategory = null;
            for (ShopManager.ShopCategory cat : ShopManager.ShopCategory.values()) {
                if (title.contains(cat.getDisplayName())) {
                    matchedCategory = cat;
                    break;
                }
            }

            // Footer Navigation
            if (slot == 48 && currentPage > 1 && matchedCategory != null) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 0.9f);
                new ShopCommand(shopManager, economyManager).openCategoryMenu(player, matchedCategory, currentPage - 1);
                return;
            }
            if (slot == 49) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 0.8f);
                new ShopCommand(shopManager, economyManager).openShopMainMenu(player);
                return;
            }
            if (slot == 50 && matchedCategory != null) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.1f);
                new ShopCommand(shopManager, economyManager).openCategoryMenu(player, matchedCategory, currentPage + 1);
                return;
            }

            // Click Item to Open Quantity Menu
            if (matchedCategory != null) {
                List<ShopManager.ShopItem> categoryItems = shopManager.getItemsByCategory(matchedCategory);
                int itemsPerPage = 28;
                int[] itemSlots = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34,
                    37, 38, 39, 40, 41, 42, 43
                };

                int startIndex = (currentPage - 1) * itemsPerPage;
                for (int i = 0; i < itemSlots.length; i++) {
                    if (slot == itemSlots[i]) {
                        int itemIndex = startIndex + i;
                        if (itemIndex < categoryItems.size()) {
                            ShopManager.ShopItem shopItem = categoryItems.get(itemIndex);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                            new ShopCommand(shopManager, economyManager).openQuantityMenu(player, shopItem, 1);
                        }
                        return;
                    }
                }
            }
            return;
        }

        // 5. GUI Pilihan Jumlah Pembelian Kustom (/shop)
        if (title.startsWith("Beli: ")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            String itemName = title.substring(6);
            ShopManager.ShopItem targetItem = null;

            for (ShopManager.ShopCategory cat : ShopManager.ShopCategory.values()) {
                for (ShopManager.ShopItem item : shopManager.getItemsByCategory(cat)) {
                    if (item.displayName().equalsIgnoreCase(itemName)) {
                        targetItem = item;
                        break;
                    }
                }
                if (targetItem != null) break;
            }

            if (targetItem == null) return;

            // Extract current quantity from item in slot 13
            ItemStack centerItem = event.getInventory().getItem(13);
            int currentQty = 1;
            if (centerItem != null) {
                currentQty = centerItem.getAmount();
            }

            // Modifiers (-64, -16, -1, +1, +16, +64)
            int delta = 0;
            if (slot == 10) delta = -64;
            else if (slot == 11) delta = -16;
            else if (slot == 12) delta = -1;
            else if (slot == 14) delta = 1;
            else if (slot == 15) delta = 16;
            else if (slot == 16) delta = 64;

            if (delta != 0) {
                int newQty = Math.max(1, Math.min(64, currentQty + delta));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
                new ShopCommand(shopManager, economyManager).openQuantityMenu(player, targetItem, newQty);
                return;
            }

            // Cancel / Back (Slot 33)
            if (slot == 33) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
                new ShopCommand(shopManager, economyManager).openCategoryMenu(player, targetItem.category(), 1);
                return;
            }

            // Confirm Purchase (Slot 29)
            if (slot == 29) {
                double totalPrice = targetItem.buyPrice() * currentQty;

                if (!economyManager.hasDollar(player.getUniqueId(), totalPrice)) {
                    player.sendMessage(Component.text("❌ Uang tidak cukup! Butuh ", NamedTextColor.RED)
                            .append(Component.text(economyManager.formatDollar(totalPrice), NamedTextColor.GREEN)));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                ItemStack buyItem = targetItem.createItemStack(currentQty);
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(buyItem);
                if (!overflow.isEmpty()) {
                    player.sendMessage(Component.text("❌ Inventory kamu penuh!", NamedTextColor.RED));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                economyManager.withdrawDollar(player.getUniqueId(), totalPrice);
                economyManager.addItemsBought(player.getUniqueId(), currentQty);
                scoreboardManager.updateScoreboard(player);

                player.sendMessage(Component.text("Berhasil membeli ", NamedTextColor.GREEN)
                        .append(Component.text(currentQty + "x " + targetItem.displayName(), NamedTextColor.YELLOW))
                        .append(Component.text(" seharga ", NamedTextColor.GREEN))
                        .append(Component.text(economyManager.formatDollar(totalPrice), NamedTextColor.GREEN, TextDecoration.BOLD)));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
                player.closeInventory();
            }
            return;
        }

        // 6. Konfirmasi Penjualan Barang GUI (/sell)
        if (title.contains("Konfirmasi Penjualan")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            UUID uuid = player.getUniqueId();

            if (slot == 11) {
                List<ItemStack> itemsToSell = pendingSellItems.remove(uuid);
                if (itemsToSell != null && !itemsToSell.isEmpty()) {
                    double totalMoney = 0.0;
                    int totalCount = 0;

                    for (ItemStack item : itemsToSell) {
                        if (sellManager.isSellable(item.getType())) {
                            double unitPrice = sellManager.getPrice(item.getType());
                            totalMoney += unitPrice * item.getAmount();
                            totalCount += item.getAmount();
                        }
                    }

                    if (totalCount > 0) {
                        economyManager.addDollar(uuid, totalMoney);
                        economyManager.addItemsSold(uuid, totalCount);
                        scoreboardManager.updateScoreboard(player);

                        player.sendMessage(Component.text("Berhasil menjual ", NamedTextColor.GREEN)
                                .append(Component.text(totalCount + " item", NamedTextColor.YELLOW))
                                .append(Component.text(" senilai ", NamedTextColor.GREEN))
                                .append(Component.text(economyManager.formatDollar(totalMoney), NamedTextColor.GREEN, TextDecoration.BOLD)));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
                    }
                }
                player.closeInventory();
            } else if (slot == 15) {
                List<ItemStack> itemsToSell = pendingSellItems.remove(uuid);
                if (itemsToSell != null) {
                    for (ItemStack item : itemsToSell) {
                        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                        for (ItemStack drop : overflow.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }
                    }
                }
                player.sendMessage(Component.text("Penjualan dibatalkan.", NamedTextColor.YELLOW));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.closeInventory();
            }
            return;
        }

        // 7. Taruh Barang Untuk Dijual (/sell)
        if (title.contains("TARUH BARANG UNTUK DIJUAL")) {
            int slot = event.getRawSlot();
            if (slot >= 27 && slot < 36) {
                event.setCancelled(true);

                Inventory topInv = event.getView().getTopInventory();

                if (slot == 29) {
                    int addedCount = 0;
                    ItemStack[] pInv = player.getInventory().getContents();
                    for (int i = 0; i < pInv.length; i++) {
                        ItemStack pItem = pInv[i];
                        if (pItem != null && !pItem.getType().isAir()) {
                            if (sellManager.isSellable(pItem.getType())) {
                                HashMap<Integer, ItemStack> overflow = topInv.addItem(pItem.clone());
                                if (overflow.isEmpty()) {
                                    player.getInventory().setItem(i, null);
                                    addedCount += pItem.getAmount();
                                } else {
                                    int remaining = overflow.values().iterator().next().getAmount();
                                    int moved = pItem.getAmount() - remaining;
                                    pItem.setAmount(remaining);
                                    addedCount += moved;
                                    break;
                                }
                            }
                        }
                    }
                    if (addedCount > 0) {
                        player.sendMessage(Component.text("Berhasil memindahkan " + addedCount + " item.", NamedTextColor.GREEN));
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.2f);
                    } else {
                        player.sendMessage(Component.text("Tidak ada item yang bisa dijual.", NamedTextColor.YELLOW));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                } else if (slot == 31) {
                    processSellGUI(player, topInv);
                } else if (slot == 33) {
                    int returnedCount = 0;
                    for (int i = 0; i < 27; i++) {
                        ItemStack item = topInv.getItem(i);
                        if (item != null && !item.getType().isAir()) {
                            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                            if (overflow.isEmpty()) {
                                topInv.setItem(i, null);
                                returnedCount += item.getAmount();
                            } else {
                                int remaining = overflow.values().iterator().next().getAmount();
                                int moved = item.getAmount() - remaining;
                                item.setAmount(remaining);
                                returnedCount += moved;
                                break;
                            }
                        }
                    }
                    if (returnedCount > 0) {
                        player.sendMessage(Component.text("Mengembalikan " + returnedCount + " item.", NamedTextColor.YELLOW));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                    }
                }
            }
            return;
        }

        // 8. Papan Pesanan / Order (/order)
        if (title.contains("Papan Pesanan / Order")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            int currentPage = 1;
            try {
                String pageStr = title.substring(title.indexOf("Hal.") + 4);
                currentPage = Integer.parseInt(pageStr);
            } catch (Exception ignored) {}

            List<OrderManager.OrderListing> orders = orderManager.getOrders();
            int itemsPerPage = 36;
            int maxPages = Math.max(1, (int) Math.ceil((double) orders.size() / itemsPerPage));

            if (slot == 48 && currentPage > 1) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 0.9f);
                new com.medieval.economy.commands.OrderCommand(orderManager, economyManager).openOrderGUI(player, currentPage - 1);
                return;
            }
            if (slot == 50 && currentPage < maxPages) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.1f);
                new com.medieval.economy.commands.OrderCommand(orderManager, economyManager).openOrderGUI(player, currentPage + 1);
                return;
            }

            if (slot >= 0 && slot < 36) {
                int actualIndex = ((currentPage - 1) * itemsPerPage) + slot;
                if (actualIndex < orders.size()) {
                    OrderManager.OrderListing order = orders.get(actualIndex);

                    if (order.requesterUUID().equals(player.getUniqueId())) {
                        economyManager.addDollar(player.getUniqueId(), order.rewardDollar());
                        orderManager.removeOrder(order.id());
                        scoreboardManager.updateScoreboard(player);

                        player.sendMessage(Component.text("Order dibatalkan. Uang ", NamedTextColor.GREEN)
                                .append(Component.text(economyManager.formatDollar(order.rewardDollar()), NamedTextColor.GREEN))
                                .append(Component.text(" telah dikembalikan.", NamedTextColor.GREEN)));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                        player.closeInventory();
                        return;
                    }

                    ItemStack required = order.requestedItem();
                    if (!player.getInventory().containsAtLeast(required, required.getAmount())) {
                        player.sendMessage(Component.text("❌ Kamu tidak punya item ", NamedTextColor.RED)
                                .append(Component.text(required.getAmount() + "x " + required.getType().name(), NamedTextColor.YELLOW)));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    player.getInventory().removeItem(required);
                    economyManager.addDollar(player.getUniqueId(), order.rewardDollar());
                    economyManager.addItemsSold(player.getUniqueId(), required.getAmount());

                    orderManager.removeOrder(order.id());
                    scoreboardManager.updateScoreboard(player);

                    player.sendMessage(Component.text("Berhasil menyelesaikan pesanan dari ", NamedTextColor.GREEN)
                            .append(Component.text(order.requesterName(), NamedTextColor.YELLOW))
                            .append(Component.text("! Imbalan: ", NamedTextColor.GREEN))
                            .append(Component.text(economyManager.formatDollar(order.rewardDollar()), NamedTextColor.GREEN, TextDecoration.BOLD)));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);

                    Player requester = Bukkit.getPlayer(order.requesterUUID());
                    if (requester != null && requester.isOnline()) {
                        HashMap<Integer, ItemStack> overflow = requester.getInventory().addItem(required);
                        for (ItemStack drop : overflow.values()) {
                            requester.getWorld().dropItemNaturally(requester.getLocation(), drop);
                        }
                        requester.sendMessage(Component.text("Pesanan ", NamedTextColor.GREEN)
                                .append(Component.text(required.getAmount() + "x " + required.getType().name(), NamedTextColor.YELLOW))
                                .append(Component.text(" telah diselesaikan oleh ", NamedTextColor.GREEN))
                                .append(Component.text(player.getName(), NamedTextColor.YELLOW)));
                        requester.playSound(requester.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                    }

                    player.closeInventory();
                }
            }
            return;
        }

        // 9. Pasar Lelang (/auctions)
        if (title.contains("Lelang [")) {
            event.setCancelled(true);
            int slot = event.getSlot();

            AuctionManager.AuctionCategory cat = AuctionManager.AuctionCategory.ALL;
            int currentPage = 1;
            try {
                String catName = title.substring(title.indexOf("[") + 1, title.indexOf("]"));
                cat = AuctionManager.AuctionCategory.valueOf(catName);
                String pageStr = title.substring(title.indexOf("Hal.") + 4);
                currentPage = Integer.parseInt(pageStr);
            } catch (Exception ignored) {}

            List<AuctionManager.AuctionListing> categoryListings = auctionManager.getListingsByCategory(cat);
            int itemsPerPage = 36;
            int maxPages = Math.max(1, (int) Math.ceil((double) categoryListings.size() / itemsPerPage));

            if (slot >= 36 && slot <= 40) {
                int catIndex = slot - 36;
                AuctionManager.AuctionCategory[] cats = AuctionManager.AuctionCategory.values();
                if (catIndex < cats.length) {
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.2f);
                    new com.medieval.economy.commands.AuctionsCommand(auctionManager, economyManager).openAuctionGUI(player, cats[catIndex], 1);
                }
                return;
            }

            if (slot == 48 && currentPage > 1) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 0.9f);
                new com.medieval.economy.commands.AuctionsCommand(auctionManager, economyManager).openAuctionGUI(player, cat, currentPage - 1);
                return;
            }
            if (slot == 50 && currentPage < maxPages) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.1f);
                new com.medieval.economy.commands.AuctionsCommand(auctionManager, economyManager).openAuctionGUI(player, cat, currentPage + 1);
                return;
            }

            if (slot >= 0 && slot < 36) {
                int actualIndex = ((currentPage - 1) * itemsPerPage) + slot;
                if (actualIndex < categoryListings.size()) {
                    AuctionManager.AuctionListing listing = categoryListings.get(actualIndex);

                    if (listing.sellerUUID().equals(player.getUniqueId())) {
                        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(listing.item());
                        if (!overflow.isEmpty()) {
                            player.sendMessage(Component.text("❌ Inventory kamu penuh!", NamedTextColor.RED));
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            return;
                        }

                        auctionManager.removeListing(listing.id());
                        player.sendMessage(Component.text("Lelang dibatalkan. Item telah dikembalikan.", NamedTextColor.GREEN));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                        player.closeInventory();
                        return;
                    }

                    if (!economyManager.hasDollar(player.getUniqueId(), listing.price())) {
                        player.sendMessage(Component.text("❌ Uang tidak cukup! Butuh ", NamedTextColor.RED)
                                .append(Component.text(economyManager.formatDollar(listing.price()), NamedTextColor.GREEN)));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(listing.item());
                    if (!overflow.isEmpty()) {
                        player.sendMessage(Component.text("❌ Inventory kamu penuh!", NamedTextColor.RED));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }

                    economyManager.withdrawDollar(player.getUniqueId(), listing.price());
                    economyManager.addDollar(listing.sellerUUID(), listing.price());
                    economyManager.addItemsBought(player.getUniqueId(), listing.item().getAmount());
                    economyManager.addItemsSold(listing.sellerUUID(), listing.item().getAmount());

                    scoreboardManager.updateScoreboard(player);
                    auctionManager.removeListing(listing.id());

                    player.sendMessage(Component.text("Berhasil membeli item lelang seharga ", NamedTextColor.GREEN)
                            .append(Component.text(economyManager.formatDollar(listing.price()), NamedTextColor.GREEN, TextDecoration.BOLD))
                            .append(Component.text(" dari ", NamedTextColor.GREEN))
                            .append(Component.text(listing.sellerName(), NamedTextColor.YELLOW)));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);

                    Player seller = Bukkit.getPlayer(listing.sellerUUID());
                    if (seller != null && seller.isOnline()) {
                        scoreboardManager.updateScoreboard(seller);
                        seller.sendMessage(Component.text("Item lelang kamu dibeli oleh ", NamedTextColor.GREEN)
                                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                                .append(Component.text("! Menerima ", NamedTextColor.GREEN))
                                .append(Component.text(economyManager.formatDollar(listing.price()), NamedTextColor.GREEN, TextDecoration.BOLD)));
                        seller.playSound(seller.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                    }

                    player.closeInventory();
                }
            }
        }
    }

    private void processSellGUI(Player player, Inventory topInv) {
        List<ItemStack> sellableItems = new ArrayList<>();
        double totalMoney = 0.0;
        int totalItemsCount = 0;

        for (int i = 0; i < 27; i++) {
            ItemStack item = topInv.getItem(i);
            if (item != null && !item.getType().isAir()) {
                if (sellManager.isSellable(item.getType())) {
                    sellableItems.add(item.clone());
                    double unitPrice = sellManager.getPrice(item.getType());
                    totalMoney += unitPrice * item.getAmount();
                    totalItemsCount += item.getAmount();
                    topInv.setItem(i, null);
                }
            }
        }

        if (totalItemsCount == 0) {
            player.sendMessage(Component.text("Taruh item yang mau dijual terlebih dahulu.", NamedTextColor.YELLOW));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        boolean needConfirm = settingsManager.isSellConfirmMode(player.getUniqueId().toString());
        if (needConfirm) {
            pendingSellItems.put(player.getUniqueId(), sellableItems);
            openConfirmSellGUI(player, totalMoney, totalItemsCount);
        } else {
            economyManager.addDollar(player.getUniqueId(), totalMoney);
            economyManager.addItemsSold(player.getUniqueId(), totalItemsCount);
            scoreboardManager.updateScoreboard(player);

            player.sendMessage(Component.text("Berhasil menjual ", NamedTextColor.GREEN)
                    .append(Component.text(totalItemsCount + " item", NamedTextColor.YELLOW))
                    .append(Component.text(" senilai ", NamedTextColor.GREEN))
                    .append(Component.text(economyManager.formatDollar(totalMoney), NamedTextColor.GREEN, TextDecoration.BOLD)));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
            player.closeInventory();
        }
    }

    private void openConfirmSellGUI(Player player, double totalMoney, int itemCount) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("Konfirmasi Penjualan", NamedTextColor.DARK_GRAY));

        ItemStack agree = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta agreeMeta = agree.getItemMeta();
        if (agreeMeta != null) {
            agreeMeta.displayName(Component.text("SETUJU JUAL", NamedTextColor.GREEN, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Total Item: ", NamedTextColor.GRAY).append(Component.text(itemCount + " item", NamedTextColor.WHITE)));
            lore.add(Component.text("Estimasi: ", NamedTextColor.GRAY).append(Component.text(economyManager.formatDollar(totalMoney), NamedTextColor.GREEN, TextDecoration.BOLD)));
            agreeMeta.lore(lore);
            agree.setItemMeta(agreeMeta);
        }
        gui.setItem(11, agree);

        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(Component.text("BATALKAN", NamedTextColor.RED, TextDecoration.BOLD));
            cancel.setItemMeta(cancelMeta);
        }
        gui.setItem(15, cancel);

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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().title() == null) return;
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (title.contains("TARUH BARANG UNTUK DIJUAL")) {
            Player player = (Player) event.getPlayer();
            Inventory inv = event.getInventory();

            for (int i = 0; i < 27; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && !item.getType().isAir()) {
                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
                    for (ItemStack drop : overflow.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
            }
        }
    }
}
