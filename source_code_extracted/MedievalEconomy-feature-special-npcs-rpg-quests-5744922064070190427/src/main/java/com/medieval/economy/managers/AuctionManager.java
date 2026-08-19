package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionManager {

    public enum AuctionCategory {
        ALL("🌐 Semua Item", Material.NETHER_STAR),
        WEAPONS_ARMOR("⚔️ Armor & Senjata", Material.IRON_SWORD),
        ORES_RESOURCES("⛏ Ore & Resource", Material.DIAMOND),
        FOOD_FARMING("🌾 Makanan & Tani", Material.BREAD),
        MISC("📦 Item Lainnya", Material.CHEST);

        private final String displayName;
        private final Material icon;

        AuctionCategory(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }
    }

    public record AuctionListing(String id, UUID sellerUUID, String sellerName, ItemStack item, double price, long listedTime) {}

    private final MedievalEconomyPlugin plugin;
    private File auctionFile;
    private FileConfiguration auctionConfig;
    private final List<AuctionListing> listings = new ArrayList<>();

    public AuctionManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        loadAuctions();
    }

    public void loadAuctions() {
        auctionFile = new File(plugin.getDataFolder(), "auctions.yml");
        if (!auctionFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                auctionFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat file auctions.yml: " + e.getMessage());
            }
        }
        auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);

        listings.clear();
        for (String id : auctionConfig.getKeys(false)) {
            try {
                UUID sellerUUID = UUID.fromString(auctionConfig.getString(id + ".seller_uuid", ""));
                String sellerName = auctionConfig.getString(id + ".seller_name", "Anonim");
                ItemStack item = auctionConfig.getItemStack(id + ".item");
                double price = auctionConfig.getDouble(id + ".price");
                long time = auctionConfig.getLong(id + ".time");

                if (item != null) {
                    listings.add(new AuctionListing(id, sellerUUID, sellerName, item, price, time));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Gagal membaca listing lelang ID: " + id);
            }
        }
    }

    public void saveAuctions() {
        for (String key : auctionConfig.getKeys(false)) {
            auctionConfig.set(key, null);
        }

        for (AuctionListing listing : listings) {
            String id = listing.id();
            auctionConfig.set(id + ".seller_uuid", listing.sellerUUID().toString());
            auctionConfig.set(id + ".seller_name", listing.sellerName());
            auctionConfig.set(id + ".item", listing.item());
            auctionConfig.set(id + ".price", listing.price());
            auctionConfig.set(id + ".time", listing.listedTime());
        }

        try {
            auctionConfig.save(auctionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan file auctions.yml: " + e.getMessage());
        }
    }

    public AuctionListing createListing(UUID sellerUUID, String sellerName, ItemStack item, double price) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        AuctionListing listing = new AuctionListing(id, sellerUUID, sellerName, item, price, System.currentTimeMillis());
        listings.add(listing);
        saveAuctions();
        return listing;
    }

    public boolean removeListing(String id) {
        boolean removed = listings.removeIf(l -> l.id().equals(id));
        if (removed) {
            saveAuctions();
        }
        return removed;
    }

    public List<AuctionListing> getListings() {
        return new ArrayList<>(listings);
    }

    public List<AuctionListing> getListingsByCategory(AuctionCategory category) {
        if (category == AuctionCategory.ALL) {
            return getListings();
        }
        return listings.stream().filter(l -> getItemCategory(l.item().getType()) == category).toList();
    }

    public AuctionCategory getItemCategory(Material mat) {
        String name = mat.name();
        if (name.contains("SWORD") || name.contains("HELMET") || name.contains("CHESTPLATE") ||
            name.contains("LEGGINGS") || name.contains("BOOTS") || name.contains("BOW") ||
            name.contains("SHIELD") || name.contains("AXE") || name.contains("CROSSBOW") || name.contains("TRIDENT")) {
            return AuctionCategory.WEAPONS_ARMOR;
        } else if (name.contains("INGOT") || name.contains("RAW") || name.contains("DIAMOND") ||
                   name.contains("EMERALD") || name.contains("COAL") || name.contains("LAPIS") ||
                   name.contains("REDSTONE") || name.contains("COPPER") || name.contains("QUARTZ")) {
            return AuctionCategory.ORES_RESOURCES;
        } else if (name.contains("BREAD") || name.contains("BEEF") || name.contains("PORK") ||
                   name.contains("CHICKEN") || name.contains("APPLE") || name.contains("WHEAT") ||
                   name.contains("CARROT") || name.contains("POTATO") || name.contains("SEED") || name.contains("BERRY")) {
            return AuctionCategory.FOOD_FARMING;
        } else {
            return AuctionCategory.MISC;
        }
    }
}
