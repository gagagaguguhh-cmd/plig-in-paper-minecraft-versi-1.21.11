package com.medieval.economy.managers;

import org.bukkit.Material;

import java.util.EnumMap;
import java.util.Map;

public class SellManager {

    private final Map<Material, Double> dollarPrices = new EnumMap<>(Material.class);

    public SellManager() {
        setupAllMaterialPrices();
    }

    private void register(Material mat, double price) {
        if (mat != null && !mat.isAir()) {
            dollarPrices.put(mat, price);
        }
    }

    private void setupAllMaterialPrices() {
        // --- ORE & MINERALS ---
        register(Material.NETHERITE_INGOT, 500.0);
        register(Material.NETHERITE_SCRAP, 150.0);
        register(Material.ANCIENT_DEBRIS, 150.0);
        register(Material.DIAMOND, 50.0);
        register(Material.DIAMOND_ORE, 40.0);
        register(Material.DEEPSLATE_DIAMOND_ORE, 40.0);
        register(Material.EMERALD, 30.0);
        register(Material.EMERALD_ORE, 25.0);
        register(Material.DEEPSLATE_EMERALD_ORE, 25.0);
        register(Material.GOLD_INGOT, 10.0);
        register(Material.RAW_GOLD, 8.0);
        register(Material.GOLD_ORE, 8.0);
        register(Material.DEEPSLATE_GOLD_ORE, 8.0);
        register(Material.NETHER_GOLD_ORE, 5.0);
        register(Material.IRON_INGOT, 5.0);
        register(Material.RAW_IRON, 4.0);
        register(Material.IRON_ORE, 4.0);
        register(Material.DEEPSLATE_IRON_ORE, 4.0);
        register(Material.COPPER_INGOT, 1.5);
        register(Material.RAW_COPPER, 1.0);
        register(Material.COPPER_ORE, 1.0);
        register(Material.DEEPSLATE_COPPER_ORE, 1.0);
        register(Material.COAL, 1.0);
        register(Material.CHARCOAL, 0.8);
        register(Material.COAL_ORE, 1.0);
        register(Material.DEEPSLATE_COAL_ORE, 1.0);
        register(Material.LAPIS_LAZULI, 1.0);
        register(Material.LAPIS_ORE, 1.0);
        register(Material.DEEPSLATE_LAPIS_ORE, 1.0);
        register(Material.REDSTONE, 0.8);
        register(Material.REDSTONE_ORE, 0.8);
        register(Material.DEEPSLATE_REDSTONE_ORE, 0.8);
        register(Material.QUARTZ, 2.0);
        register(Material.NETHER_QUARTZ_ORE, 2.0);
        register(Material.AMETHYST_SHARD, 2.0);

        // --- FARMING & CROPS ---
        register(Material.WHEAT, 0.5);
        register(Material.CARROT, 0.5);
        register(Material.POTATO, 0.5);
        register(Material.BEETROOT, 0.5);
        register(Material.MELON_SLICE, 0.2);
        register(Material.PUMPKIN, 1.0);
        register(Material.SWEET_BERRIES, 0.4);
        register(Material.GLOW_BERRIES, 0.5);
        register(Material.APPLE, 0.8);
        register(Material.SUGAR_CANE, 0.5);
        register(Material.BAMBOO, 0.2);
        register(Material.CACTUS, 0.3);
        register(Material.COCOA_BEANS, 0.5);
        register(Material.NETHER_WART, 1.0);
        register(Material.WHEAT_SEEDS, 0.1);
        register(Material.PUMPKIN_SEEDS, 0.1);
        register(Material.MELON_SEEDS, 0.1);
        register(Material.BEETROOT_SEEDS, 0.1);
        register(Material.TORCHFLOWER_SEEDS, 0.5);
        register(Material.PITCHER_POD, 0.5);
        register(Material.HAY_BLOCK, 4.0);
        register(Material.DRIED_KELP, 0.2);

        // --- FOOD & MEAT ---
        register(Material.BEEF, 1.0);
        register(Material.COOKED_BEEF, 1.5);
        register(Material.PORKCHOP, 1.0);
        register(Material.COOKED_PORKCHOP, 1.5);
        register(Material.CHICKEN, 0.8);
        register(Material.COOKED_CHICKEN, 1.2);
        register(Material.MUTTON, 0.8);
        register(Material.COOKED_MUTTON, 1.2);
        register(Material.RABBIT, 1.0);
        register(Material.COOKED_RABBIT, 1.5);
        register(Material.COD, 0.8);
        register(Material.COOKED_COD, 1.2);
        register(Material.SALMON, 1.0);
        register(Material.COOKED_SALMON, 1.5);
        register(Material.TROPICAL_FISH, 1.5);
        register(Material.PUFFERFISH, 2.0);
        register(Material.BREAD, 1.0);
        register(Material.BAKED_POTATO, 0.8);
        register(Material.HONEY_BOTTLE, 2.0);
        register(Material.MUSHROOM_STEW, 1.5);

        // --- WOOD & LOGS ---
        register(Material.OAK_LOG, 0.5);
        register(Material.SPRUCE_LOG, 0.5);
        register(Material.BIRCH_LOG, 0.5);
        register(Material.JUNGLE_LOG, 0.5);
        register(Material.ACACIA_LOG, 0.5);
        register(Material.DARK_OAK_LOG, 0.5);
        register(Material.MANGROVE_LOG, 0.5);
        register(Material.CHERRY_LOG, 0.5);
        register(Material.CRIMSON_STEM, 0.6);
        register(Material.WARPED_STEM, 0.6);
        register(Material.STRIPPED_OAK_LOG, 0.5);
        register(Material.STRIPPED_SPRUCE_LOG, 0.5);
        register(Material.STRIPPED_BIRCH_LOG, 0.5);
        register(Material.STRIPPED_JUNGLE_LOG, 0.5);
        register(Material.STRIPPED_ACACIA_LOG, 0.5);
        register(Material.STRIPPED_DARK_OAK_LOG, 0.5);
        register(Material.STRIPPED_MANGROVE_LOG, 0.5);
        register(Material.STRIPPED_CHERRY_LOG, 0.5);
        register(Material.STRIPPED_CRIMSON_STEM, 0.6);
        register(Material.STRIPPED_WARPED_STEM, 0.6);

        // --- STONE & BUILDING BLOCKS ---
        register(Material.COBBLESTONE, 0.05);
        register(Material.STONE, 0.1);
        register(Material.SMOOTH_STONE, 0.15);
        register(Material.STONE_BRICKS, 0.15);
        register(Material.MOSSY_STONE_BRICKS, 0.2);
        register(Material.DEEPSLATE, 0.05);
        register(Material.COBBLED_DEEPSLATE, 0.05);
        register(Material.POLISHED_DEEPSLATE, 0.1);
        register(Material.DEEPSLATE_BRICKS, 0.15);
        register(Material.DIRT, 0.02);
        register(Material.SAND, 0.05);
        register(Material.RED_SAND, 0.05);
        register(Material.GRAVEL, 0.05);
        register(Material.CLAY_BALL, 0.1);
        register(Material.CLAY, 0.4);
        register(Material.GRANITE, 0.05);
        register(Material.DIORITE, 0.05);
        register(Material.ANDESITE, 0.05);
        register(Material.TUFF, 0.05);
        register(Material.CALCITE, 0.1);
        register(Material.BASALT, 0.1);
        register(Material.BLACKSTONE, 0.08);
        register(Material.NETHERRACK, 0.02);
        register(Material.SOUL_SAND, 0.1);
        register(Material.SOUL_SOIL, 0.1);
        register(Material.END_STONE, 0.1);

        // --- MOB DROPS ---
        register(Material.ROTTEN_FLESH, 0.3);
        register(Material.BONE, 0.5);
        register(Material.STRING, 0.5);
        register(Material.SPIDER_EYE, 0.8);
        register(Material.GUNPOWDER, 2.0);
        register(Material.SLIME_BALL, 2.0);
        register(Material.ENDER_PEARL, 5.0);
        register(Material.BLAZE_ROD, 5.0);
        register(Material.FEATHER, 0.3);
        register(Material.LEATHER, 0.8);
        register(Material.PHANTOM_MEMBRANE, 3.0);
        register(Material.SHULKER_SHELL, 25.0);
        register(Material.GHAST_TEAR, 10.0);
        register(Material.MAGMA_CREAM, 1.5);
        register(Material.INK_SAC, 0.5);
        register(Material.GLOW_INK_SAC, 1.0);
        register(Material.PRISMARINE_SHARD, 1.0);
        register(Material.PRISMARINE_CRYSTALS, 1.5);
        register(Material.NAUTILUS_SHELL, 15.0);
    }

    public double getPrice(Material material) {
        return dollarPrices.getOrDefault(material, 0.0);
    }

    public boolean isSellable(Material material) {
        return dollarPrices.containsKey(material) && dollarPrices.get(material) > 0;
    }
}
