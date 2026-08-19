package com.medieval.economy.listeners;

import com.medieval.economy.MedievalEconomyPlugin;
import com.medieval.economy.managers.EconomyManager;
import com.medieval.economy.managers.NPCManager;
import com.medieval.economy.managers.QuestManager;
import com.medieval.economy.managers.RPGManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpecialNPCInteractListener implements Listener {

    private final MedievalEconomyPlugin plugin;
    private final NPCManager npcManager;
    private final RPGManager rpgManager;
    private final QuestManager questManager;
    private final EconomyManager economyManager;
    private final MonsterKekuatanManager monsterKekuatanManager;
    private final NamespacedKey npcTypeKey;
    private final NamespacedKey questIdKey;

    public SpecialNPCInteractListener(MedievalEconomyPlugin plugin, NPCManager npcManager, RPGManager rpgManager, QuestManager questManager, EconomyManager economyManager, MonsterKekuatanManager monsterKekuatanManager, NamespacedKey npcTypeKey) {
        this.plugin = plugin;
        this.npcManager = npcManager;
        this.rpgManager = rpgManager;
        this.questManager = questManager;
        this.economyManager = economyManager;
        this.monsterKekuatanManager = monsterKekuatanManager;
        this.npcTypeKey = npcTypeKey;
        this.questIdKey = new NamespacedKey(plugin, "offered_quest_id");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        if (!pdc.has(npcTypeKey, PersistentDataType.STRING)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        String typeStr = pdc.get(npcTypeKey, PersistentDataType.STRING);
        NPCManager.NPCType type = NPCManager.NPCType.valueOf(typeStr);

        if (type == NPCManager.NPCType.RPG_STATS) {
            handleRPGStatsNPCInteract(player);
        } else if (type == NPCManager.NPCType.ECONOMIC_QUEST) {
            handleEconomicQuestNPCInteract(player);
        } else if (type == NPCManager.NPCType.MONSTER_RAID) {
            handleMonsterRaidNPCInteract(player);
        }
    }

    private void handleMonsterRaidNPCInteract(Player player) {
        UUID uuid = player.getUniqueId();
        if (!rpgManager.isInitiated(uuid)) {
            player.sendMessage(Component.text("👹 [Pemberi Misi Monster]: ", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                    .append(Component.text("Kamu harus menemui Tetua RPG Desa terlebih dahulu sebelum menerima misi monster!", NamedTextColor.RED)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        openMonsterRaidGUI(player);
    }

    public void openMonsterRaidGUI(Player player) {
        UUID uuid = player.getUniqueId();
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("👹 Misi Raid Monster", NamedTextColor.DARK_RED));

        int kekuatan = monsterKekuatanManager.getKekuatan(uuid);
        int totalKilled = monsterKekuatanManager.getTotalMonsterKilled(uuid);
        boolean canRaid = monsterKekuatanManager.canStartRaid(uuid);
        long cooldownRemaining = monsterKekuatanManager.getRaidCooldownRemaining(uuid);

        ItemStack raidItem = new ItemStack(canRaid ? Material.DIAMOND_SWORD : Material.BARRIER);
        ItemMeta raidMeta = raidItem.getItemMeta();
        if (raidMeta != null) {
            raidMeta.displayName(Component.text((canRaid ? "⚔️" : "🔒") + " Mulai Raid Monster", NamedTextColor.RED, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            lore.add(Component.text("💪 Kekuatanmu: ", NamedTextColor.GRAY).append(Component.text(kekuatan, NamedTextColor.AQUA, TextDecoration.BOLD)));
            lore.add(Component.text("☠️ Total Monster Dibunuh: ", NamedTextColor.GRAY).append(Component.text(totalKilled, NamedTextColor.DARK_RED)));
            lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
            if (canRaid) {
                lore.add(Component.text("📍 Koordinat Raid:", NamedTextColor.YELLOW));
                Location raidLoc = generateRaidLocation(player.getLocation());
                lore.add(Component.text("   X: " + raidLoc.getBlockX(), NamedTextColor.GRAY));
                lore.add(Component.text("   Y: " + raidLoc.getBlockY(), NamedTextColor.GRAY));
                lore.add(Component.text("   Z: " + raidLoc.getBlockZ(), NamedTextColor.GRAY));
                lore.add(Component.text("   Dunia: " + raidLoc.getWorld().getName(), NamedTextColor.GRAY));
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                lore.add(Component.text("👉 KLIK UNTUK MENERIMA MISI RAID!", NamedTextColor.GREEN, TextDecoration.BOLD));
            } else {
                long minutes = cooldownRemaining / 60000;
                long seconds = (cooldownRemaining % 60000) / 1000;
                lore.add(Component.text("❌ Cooldown: " + minutes + "m " + seconds + "s", NamedTextColor.RED));
                lore.add(Component.text("Tunggu sebelum memulai raid baru!", NamedTextColor.GRAY));
            }
            raidMeta.lore(lore);
            raidItem.setItemMeta(raidMeta);
        }
        gui.setItem(13, raidItem);

        // Background filler
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

    private Location generateRaidLocation(Location playerLoc) {
        World world = playerLoc.getWorld();
        Random rand = new Random();
        // Generate random coordinates within 500-1500 blocks from player
        int offsetX = (rand.nextInt(1000) + 500) * (rand.nextBoolean() ? 1 : -1);
        int offsetZ = (rand.nextInt(1000) + 500) * (rand.nextBoolean() ? 1 : -1);
        int x = playerLoc.getBlockX() + offsetX;
        int z = playerLoc.getBlockZ() + offsetZ;
        // Find highest block at that location
        int y = world.getHighestBlockYAt(x, z);
        return new Location(world, x, y, z);
    }

    private void handleRPGStatsNPCInteract(Player player) {
        UUID uuid = player.getUniqueId();
        if (!rpgManager.isInitiated(uuid)) {
            rpgManager.setInitiated(uuid, true);
            player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
            player.sendMessage(Component.text("🧙 [Tetua RPG Desa]: ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                    .append(Component.text("Selamat! Kamu telah menginisiasi petualangan RPG-mu!", NamedTextColor.GREEN)));
            player.sendMessage(Component.text("Sekarang kamu bisa mengakses menu ", NamedTextColor.GRAY)
                    .append(Component.text("/akun", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .append(Component.text(" dan menerima Misi Ekonomi di Desa!", NamedTextColor.GRAY)));
            player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        } else {
            player.sendMessage(Component.text("🧙 [Tetua RPG Desa]: ", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                    .append(Component.text("Salam, Petualang! Terus tingkatkan Level RPG dan selesaikan Misi Desa!", NamedTextColor.YELLOW)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
        }
    }

    private void handleEconomicQuestNPCInteract(Player player) {
        UUID uuid = player.getUniqueId();
        if (!rpgManager.isInitiated(uuid)) {
            player.sendMessage(Component.text("🌾 [Pedagang Desa]: ", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text("Kamu harus menemui Tetua RPG Desa terlebih dahulu sebelum mengambil misi denganku!", NamedTextColor.RED)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        openQuestGUI(player);
    }

    public void openQuestGUI(Player player) {
        UUID uuid = player.getUniqueId();
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("🌾 Misi Ekonomi Desa", NamedTextColor.DARK_GRAY));

        QuestManager.PlayerQuest activePQ = questManager.getActiveQuest(uuid);
        if (activePQ == null) {
            // Offer new quest
            QuestManager.QuestTemplate newQuest = questManager.getRandomQuest();
            if (newQuest == null) {
                player.sendMessage(Component.text("Tidak ada misi yang tersedia saat ini.", NamedTextColor.RED));
                return;
            }

            ItemStack questItem = new ItemStack(newQuest.getMaterial());
            ItemMeta meta = questItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("📜 Misi Baru: " + newQuest.getName(), NamedTextColor.GOLD, TextDecoration.BOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                lore.add(Component.text("📦 Bawa: ", NamedTextColor.GRAY).append(Component.text(newQuest.getRequiredAmount() + "x " + newQuest.getMaterial().name(), NamedTextColor.YELLOW)));
                lore.add(Component.text("🎁 Hadiah:", NamedTextColor.GRAY));
                lore.add(Component.text("   • Dollar: ", NamedTextColor.GRAY).append(Component.text("$" + newQuest.getRewardDollar(), NamedTextColor.GREEN)));
                lore.add(Component.text("   • Gold: ", NamedTextColor.GRAY).append(Component.text("+" + newQuest.getRewardGold(), NamedTextColor.GOLD)));
                lore.add(Component.text("   • RPG EXP: ", NamedTextColor.GRAY).append(Component.text("+" + newQuest.getRewardExp() + " EXP", NamedTextColor.LIGHT_PURPLE)));
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                lore.add(Component.text("👉 KLIK UNTUK MENERIMA MISI INI!", NamedTextColor.GREEN, TextDecoration.BOLD));
                meta.lore(lore);

                meta.getPersistentDataContainer().set(questIdKey, PersistentDataType.STRING, newQuest.getId());
                questItem.setItemMeta(meta);
            }
            gui.setItem(13, questItem);
        } else {
            // Show active quest status & submission option
            QuestManager.QuestTemplate qt = questManager.getQuestTemplate(activePQ.getQuestId());
            if (qt == null) {
                questManager.setActiveQuest(uuid, null);
                openQuestGUI(player);
                return;
            }

            int playerItemCount = countItemsInInventory(player, qt.getMaterial());
            boolean canComplete = playerItemCount >= qt.getRequiredAmount();

            ItemStack questItem = new ItemStack(qt.getMaterial());
            ItemMeta meta = questItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("📜 Misi Aktif: " + qt.getName(), NamedTextColor.GOLD, TextDecoration.BOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                lore.add(Component.text("📦 Diperlukan: ", NamedTextColor.GRAY).append(Component.text(qt.getRequiredAmount() + "x " + qt.getMaterial().name(), NamedTextColor.YELLOW)));
                lore.add(Component.text("🎒 Dimiliki saat ini: ", NamedTextColor.GRAY).append(Component.text(playerItemCount + "x", canComplete ? NamedTextColor.GREEN : NamedTextColor.RED)));
                lore.add(Component.text("🎁 Hadiah:", NamedTextColor.GRAY));
                lore.add(Component.text("   • Dollar: ", NamedTextColor.GRAY).append(Component.text("$" + qt.getRewardDollar(), NamedTextColor.GREEN)));
                lore.add(Component.text("   • Gold: ", NamedTextColor.GRAY).append(Component.text("+" + qt.getRewardGold(), NamedTextColor.GOLD)));
                lore.add(Component.text("   • RPG EXP: ", NamedTextColor.GRAY).append(Component.text("+" + qt.getRewardExp() + " EXP", NamedTextColor.LIGHT_PURPLE)));
                lore.add(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                if (canComplete) {
                    lore.add(Component.text("👉 KLIK UNTUK SERAHKAN BARANG & KLAIM HADIAH!", NamedTextColor.GREEN, TextDecoration.BOLD));
                } else {
                    lore.add(Component.text("❌ Barang belum mencukupi di inventori!", NamedTextColor.RED));
                }
                meta.lore(lore);
                questItem.setItemMeta(meta);
            }
            gui.setItem(13, questItem);
        }

        // Background filler
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

    private int countItemsInInventory(Player player, Material material) {
        int count = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && is.getType() == material) {
                count += is.getAmount();
            }
        }
        return count;
    }

    private void removeItemsFromInventory(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack is = contents[i];
            if (is != null && is.getType() == material) {
                if (is.getAmount() <= remaining) {
                    remaining -= is.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    is.setAmount(is.getAmount() - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = event.getView().title();
        if (title.toString().contains("Misi Ekonomi Desa")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot != 13) return;

            UUID uuid = player.getUniqueId();
            QuestManager.PlayerQuest activePQ = questManager.getActiveQuest(uuid);

            if (activePQ == null) {
                // Accept new quest from slot 13 using PDC
                ItemStack current = event.getCurrentItem();
                if (current != null && current.hasItemMeta()) {
                    ItemMeta meta = current.getItemMeta();
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    if (pdc.has(questIdKey, PersistentDataType.STRING)) {
                        String questId = pdc.get(questIdKey, PersistentDataType.STRING);
                        QuestManager.QuestTemplate qt = questManager.getQuestTemplate(questId);
                        if (qt != null) {
                            questManager.setActiveQuest(uuid, qt.getId());
                            player.sendMessage(Component.text("✅ Misi diterima: " + qt.getName() + "!", NamedTextColor.GREEN, TextDecoration.BOLD));
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                        }
                    }
                    openQuestGUI(player);
                }
            } else {
                // Complete quest
                QuestManager.QuestTemplate qt = questManager.getQuestTemplate(activePQ.getQuestId());
                if (qt != null) {
                    int count = countItemsInInventory(player, qt.getMaterial());
                    if (count >= qt.getRequiredAmount()) {
                        removeItemsFromInventory(player, qt.getMaterial(), qt.getRequiredAmount());
                        questManager.setActiveQuest(uuid, null);

                        economyManager.addDollar(uuid, qt.getRewardDollar());
                        economyManager.addGold(uuid, qt.getRewardGold());
                        boolean levelUp = rpgManager.addExp(uuid, qt.getRewardExp());

                        player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
                        player.sendMessage(Component.text("🎉 MISI SELESAI: " + qt.getName(), NamedTextColor.GOLD, TextDecoration.BOLD));
                        player.sendMessage(Component.text("🎁 Hadiah Diterima:", NamedTextColor.YELLOW));
                        player.sendMessage(Component.text("   • Dollar: ", NamedTextColor.GRAY).append(Component.text("+$" + qt.getRewardDollar(), NamedTextColor.GREEN)));
                        player.sendMessage(Component.text("   • Gold: ", NamedTextColor.GRAY).append(Component.text("+" + qt.getRewardGold(), NamedTextColor.GOLD)));
                        player.sendMessage(Component.text("   • RPG EXP: ", NamedTextColor.GRAY).append(Component.text("+" + qt.getRewardExp() + " EXP", NamedTextColor.LIGHT_PURPLE)));

                        if (levelUp) {
                            player.sendMessage(Component.text("⭐ LEVEL UP! Level RPG kamu sekarang: " + rpgManager.getLevel(uuid), NamedTextColor.AQUA, TextDecoration.BOLD));
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                        }
                        player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));

                        player.closeInventory();
                    } else {
                        player.sendMessage(Component.text("❌ Kamu belum memiliki cukup " + qt.getMaterial().name() + " (" + count + "/" + qt.getRequiredAmount() + ")", NamedTextColor.RED));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                }
            }
        } else if (title.toString().contains("Misi Raid Monster")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot != 13) return;

            UUID uuid = player.getUniqueId();
            boolean canRaid = monsterKekuatanManager.canStartRaid(uuid);

            if (canRaid) {
                Location raidLoc = generateRaidLocation(player.getLocation());
                monsterKekuatanManager.setLastRaidTime(uuid, System.currentTimeMillis());

                player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
                player.sendMessage(Component.text("⚔️ MISI RAID MONSTER DITERIMA!", NamedTextColor.RED, TextDecoration.BOLD));
                player.sendMessage(Component.text("📍 Koordinat Target:", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("   X: " + raidLoc.getBlockX(), NamedTextColor.GRAY));
                player.sendMessage(Component.text("   Y: " + raidLoc.getBlockY(), NamedTextColor.GRAY));
                player.sendMessage(Component.text("   Z: " + raidLoc.getBlockZ(), NamedTextColor.GRAY));
                player.sendMessage(Component.text("   Dunia: " + raidLoc.getWorld().getName(), NamedTextColor.GRAY));
                player.sendMessage(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
                player.sendMessage(Component.text("💪 Bunuh monster di lokasi tersebut untuk meningkatkan Kekuatanmu!", NamedTextColor.AQUA));
                player.sendMessage(Component.text("⏰ Cooldown: 5 menit setelah memulai.", NamedTextColor.GRAY));
                player.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.DARK_GRAY));
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);

                player.closeInventory();
            } else {
                player.sendMessage(Component.text("❌ Kamu masih dalam cooldown raid!", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }
}
