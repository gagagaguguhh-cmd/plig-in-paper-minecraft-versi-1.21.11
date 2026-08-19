package com.medieval.economy.managers;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {

    public enum ShopCategory {
        FOOD("🍎 Makanan & Kebutuhan Farm", Material.GOLDEN_CARROT, "Berbagai hasil tani, makanan, dan pupuk"),
        BUILDING("🏰 Blok Bangunan Medieval", Material.STONE_BRICKS, "Material kokoh untuk membangun kerajaan"),
        MINING("⛏ Ore & Hasil Tambang", Material.RAW_IRON, "Barang tambang dan hasil bumi mulia"),
        EQUIPMENT("⚔️ Gear & Peralatan", Material.DIAMOND_SWORD, "Perlengkapan tempur, armor, dan jimat ksatria"),
        MAGIC("🧪 Potion & Sihir", Material.BREWING_STAND, "Ramuan gaib dan bahan sihir");

        private final String displayName;
        private final Material icon;
        private final String description;

        ShopCategory(String displayName, Material icon, String description) {
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }

        public String getDescription() {
            return description;
        }
    }

    public record ShopItem(
            Material material,
            int amount,
            double buyPrice,
            String displayName,
            ShopCategory category,
            Map<Enchantment, Integer> enchantments
    ) {
        public ItemStack createItemStack(int qty) {
            ItemStack stack = new ItemStack(material, qty);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                if (enchantments != null && !enchantments.isEmpty()) {
                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }
                }
                stack.setItemMeta(meta);
            }
            return stack;
        }
    }

    private final List<ShopItem> shopItems = new ArrayList<>();

    public ShopManager() {
        setupDefaultShop();
    }

    private void setupDefaultShop() {
        // 1. Makanan & Kebutuhan Farm
        shopItems.add(new ShopItem(Material.BREAD, 16, 25.0, "Roti Hangat", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.COOKED_BEEF, 16, 40.0, "Daging Sapi Panggang", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.COOKED_PORKCHOP, 16, 40.0, "Daging Babi Panggang", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.GOLDEN_APPLE, 1, 150.0, "Apel Emas", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.GOLDEN_CARROT, 8, 80.0, "Wortel Emas", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.BONE_MEAL, 16, 20.0, "Pupuk Tani (Bone Meal)", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.WHEAT_SEEDS, 32, 10.0, "Bibit Gandum", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.CARROT, 16, 15.0, "Wortel Segar", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.POTATO, 16, 15.0, "Kentang Segar", ShopCategory.FOOD, null));
        shopItems.add(new ShopItem(Material.SWEET_BERRIES, 16, 15.0, "Beri Manis", ShopCategory.FOOD, null));

        // 2. Blok Bangunan Medieval
        shopItems.add(new ShopItem(Material.OAK_LOG, 32, 32.0, "Kayu Ek Segar", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.SPRUCE_LOG, 32, 32.0, "Kayu Spruce", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.DARK_OAK_LOG, 32, 35.0, "Kayu Ek Gelap", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.STONE_BRICKS, 64, 32.0, "Bata Batu Kerajaan", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.MOSSY_STONE_BRICKS, 64, 40.0, "Bata Batu Berlumut", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.COBBLESTONE, 64, 15.0, "Batu Kasar", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.DEEPSLATE_BRICKS, 64, 45.0, "Bata Batu Dalam", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.GLASS, 32, 25.0, "Kaca Bening", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.LANTERN, 8, 30.0, "Lentera Kerajaan", ShopCategory.BUILDING, null));
        shopItems.add(new ShopItem(Material.CHAIN, 16, 25.0, "Rantai Besi", ShopCategory.BUILDING, null));

        // 3. Ore & Pertambangan
        shopItems.add(new ShopItem(Material.COAL, 16, 25.0, "Batubara Hitam", ShopCategory.MINING, null));
        shopItems.add(new ShopItem(Material.RAW_IRON, 16, 120.0, "Biji Besi Mentah", ShopCategory.MINING, null));
        shopItems.add(new ShopItem(Material.IRON_INGOT, 8, 80.0, "Batangan Besi", ShopCategory.MINING, null));
        shopItems.add(new ShopItem(Material.RAW_GOLD, 8, 120.0, "Biji Emas Mentah", ShopCategory.MINING, null));
        shopItems.add(new ShopItem(Material.GOLD_INGOT, 4, 80.0, "Batangan Emas Murni", ShopCategory.MINING, null));
        shopItems.add(new ShopItem(Material.DIAMOND, 1, 120.0, "Permata Berlian", ShopCategory.MINING, null));
        shopItems.add(new ShopItem(Material.EMERALD, 2, 80.0, "Permata Zamrud", ShopCategory.MINING, null));
        shopItems.add(new ShopItem(Material.LAPIS_LAZULI, 16, 30.0, "Batu Lapis Lazuli", ShopCategory.MINING, null));
        shopItems.add(new ShopItem(Material.REDSTONE, 32, 40.0, "Bubuk Redstone", ShopCategory.MINING, null));

        // 4. Gear & Peralatan (Enchanted Medium, Totem, No Elytra)
        Map<Enchantment, Integer> swordEnchants = new HashMap<>();
        swordEnchants.put(Enchantment.SHARPNESS, 3);
        swordEnchants.put(Enchantment.UNBREAKING, 2);
        shopItems.add(new ShopItem(Material.DIAMOND_SWORD, 1, 500.0, "Pedang Berlian Ksatria (Sharp III)", ShopCategory.EQUIPMENT, swordEnchants));

        Map<Enchantment, Integer> axeEnchants = new HashMap<>();
        axeEnchants.put(Enchantment.SHARPNESS, 3);
        axeEnchants.put(Enchantment.EFFICIENCY, 3);
        axeEnchants.put(Enchantment.UNBREAKING, 2);
        shopItems.add(new ShopItem(Material.DIAMOND_AXE, 1, 550.0, "Kapak Berlian Ksatria (Sharp III / Eff III)", ShopCategory.EQUIPMENT, axeEnchants));

        Map<Enchantment, Integer> pickaxeEnchants = new HashMap<>();
        pickaxeEnchants.put(Enchantment.EFFICIENCY, 3);
        pickaxeEnchants.put(Enchantment.UNBREAKING, 2);
        shopItems.add(new ShopItem(Material.DIAMOND_PICKAXE, 1, 500.0, "Beliung Berlian Penambang (Eff III)", ShopCategory.EQUIPMENT, pickaxeEnchants));

        Map<Enchantment, Integer> armorEnchants = new HashMap<>();
        armorEnchants.put(Enchantment.PROTECTION, 2);
        armorEnchants.put(Enchantment.UNBREAKING, 2);

        shopItems.add(new ShopItem(Material.DIAMOND_HELMET, 1, 450.0, "Helm Berlian (Prot II)", ShopCategory.EQUIPMENT, armorEnchants));
        shopItems.add(new ShopItem(Material.DIAMOND_CHESTPLATE, 1, 750.0, "Baju Zirah Berlian (Prot II)", ShopCategory.EQUIPMENT, armorEnchants));
        shopItems.add(new ShopItem(Material.DIAMOND_LEGGINGS, 1, 650.0, "Celana Zirah Berlian (Prot II)", ShopCategory.EQUIPMENT, armorEnchants));
        shopItems.add(new ShopItem(Material.DIAMOND_BOOTS, 1, 450.0, "Sepatu Zirah Berlian (Prot II)", ShopCategory.EQUIPMENT, armorEnchants));

        Map<Enchantment, Integer> bowEnchants = new HashMap<>();
        bowEnchants.put(Enchantment.POWER, 3);
        bowEnchants.put(Enchantment.UNBREAKING, 2);
        shopItems.add(new ShopItem(Material.BOW, 1, 300.0, "Busur Panah Pemburu (Power III)", ShopCategory.EQUIPMENT, bowEnchants));

        Map<Enchantment, Integer> shieldEnchants = new HashMap<>();
        shieldEnchants.put(Enchantment.UNBREAKING, 2);
        shopItems.add(new ShopItem(Material.SHIELD, 1, 200.0, "Perisai Kerajaan (Unbreaking II)", ShopCategory.EQUIPMENT, shieldEnchants));

        shopItems.add(new ShopItem(Material.TOTEM_OF_UNDYING, 1, 1000.0, "Totem Perlindungan (Totem of Undying)", ShopCategory.EQUIPMENT, null));
        shopItems.add(new ShopItem(Material.ARROW, 64, 40.0, "Anak Panah Tajam", ShopCategory.EQUIPMENT, null));

        // 5. Potion & Sihir
        shopItems.add(new ShopItem(Material.EXPERIENCE_BOTTLE, 16, 120.0, "Botol Pengalaman Sihir", ShopCategory.MAGIC, null));
        shopItems.add(new ShopItem(Material.BREWING_STAND, 1, 100.0, "Alat Pembuat Ramuan", ShopCategory.MAGIC, null));
        shopItems.add(new ShopItem(Material.BLAZE_POWDER, 8, 80.0, "Bubuk Api Blaze", ShopCategory.MAGIC, null));
        shopItems.add(new ShopItem(Material.NETHER_WART, 8, 50.0, "Tanaman Nether Wart", ShopCategory.MAGIC, null));
        shopItems.add(new ShopItem(Material.GLOWSTONE_DUST, 16, 40.0, "Bubuk Batu Bersinar", ShopCategory.MAGIC, null));
        shopItems.add(new ShopItem(Material.ENDER_PEARL, 4, 50.0, "Mutiara Ender", ShopCategory.MAGIC, null));
    }

    public List<ShopItem> getItemsByCategory(ShopCategory category) {
        return shopItems.stream().filter(item -> item.category() == category).toList();
    }
}
