package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestManager {

    public static class QuestTemplate {
        private final String id;
        private final String name;
        private final Material material;
        private final int requiredAmount;
        private final double rewardDollar;
        private final double rewardGold;
        private final long rewardExp;

        public QuestTemplate(String id, String name, Material material, int requiredAmount, double rewardDollar, double rewardGold, long rewardExp) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.requiredAmount = requiredAmount;
            this.rewardDollar = rewardDollar;
            this.rewardGold = rewardGold;
            this.rewardExp = rewardExp;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Material getMaterial() { return material; }
        public int getRequiredAmount() { return requiredAmount; }
        public double getRewardDollar() { return rewardDollar; }
        public double getRewardGold() { return rewardGold; }
        public long getRewardExp() { return rewardExp; }
    }

    public static class PlayerQuest {
        private final String questId;
        private final long acceptedTime;

        public PlayerQuest(String questId, long acceptedTime) {
            this.questId = questId;
            this.acceptedTime = acceptedTime;
        }

        public String getQuestId() { return questId; }
        public long getAcceptedTime() { return acceptedTime; }
    }

    private final MedievalEconomyPlugin plugin;
    private final File questsFile;
    private final File playerQuestsFile;
    private FileConfiguration questsConfig;
    private FileConfiguration playerQuestsConfig;

    private final List<QuestTemplate> questTemplates = new ArrayList<>();
    // UUID -> Active PlayerQuest
    private final Map<UUID, PlayerQuest> activePlayerQuests = new HashMap<>();

    public QuestManager(MedievalEconomyPlugin plugin) {
        this.plugin = plugin;
        this.questsFile = new File(plugin.getDataFolder(), "quests.yml");
        this.playerQuestsFile = new File(plugin.getDataFolder(), "player_quests.yml");
        loadQuests();
        loadPlayerQuests();
    }

    public void loadQuests() {
        questTemplates.clear();
        if (!questsFile.exists()) {
            plugin.saveResource("quests.yml", false);
        }
        questsConfig = YamlConfiguration.loadConfiguration(questsFile);
        ConfigurationSection section = questsConfig.getConfigurationSection("quests");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".name", "Misi Desa");
                Material mat = Material.matchMaterial(section.getString(key + ".item", "WHEAT"));
                if (mat == null) mat = Material.WHEAT;
                int amount = section.getInt(key + ".amount", 10);
                double dollar = section.getDouble(key + ".reward_dollar", 50.0);
                double gold = section.getDouble(key + ".reward_gold", 1.0);
                long exp = section.getLong(key + ".reward_exp", 25L);

                questTemplates.add(new QuestTemplate(key, name, mat, amount, dollar, gold, exp));
            }
        }

        if (questTemplates.isEmpty()) {
            // Default fallbacks
            questTemplates.add(new QuestTemplate("wheat_gather", "Panen Gandum Desa", Material.WHEAT, 16, 100.0, 1.0, 50L));
            questTemplates.add(new QuestTemplate("carrot_gather", "Pasokan Wortel Segar", Material.CARROT, 16, 120.0, 1.0, 60L));
            questTemplates.add(new QuestTemplate("wood_gather", "Pengumpulan Kayu Oak", Material.OAK_LOG, 10, 150.0, 2.0, 75L));
            questTemplates.add(new QuestTemplate("iron_gather", "Persediaan Biji Besi", Material.RAW_IRON, 5, 250.0, 3.0, 100L));
        }
    }

    public void loadPlayerQuests() {
        if (!playerQuestsFile.exists()) {
            playerQuestsFile.getParentFile().mkdirs();
            try {
                playerQuestsFile.createNewFile();
            } catch (IOException ignored) {}
        }
        playerQuestsConfig = YamlConfiguration.loadConfiguration(playerQuestsFile);
        for (String key : playerQuestsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String questId = playerQuestsConfig.getString(key + ".quest_id");
                long time = playerQuestsConfig.getLong(key + ".accepted_time");
                if (questId != null) {
                    activePlayerQuests.put(uuid, new PlayerQuest(questId, time));
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public void savePlayerQuests() {
        if (playerQuestsConfig == null) return;
        for (String key : playerQuestsConfig.getKeys(false)) {
            playerQuestsConfig.set(key, null);
        }
        for (Map.Entry<UUID, PlayerQuest> entry : activePlayerQuests.entrySet()) {
            String path = entry.getKey().toString();
            playerQuestsConfig.set(path + ".quest_id", entry.getValue().getQuestId());
            playerQuestsConfig.set(path + ".accepted_time", entry.getValue().getAcceptedTime());
        }
        try {
            playerQuestsConfig.save(playerQuestsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan player_quests.yml: " + e.getMessage());
        }
    }

    public QuestTemplate getRandomQuest() {
        if (questTemplates.isEmpty()) return null;
        Random rand = new Random();
        return questTemplates.get(rand.nextInt(questTemplates.size()));
    }

    public QuestTemplate getQuestTemplate(String id) {
        for (QuestTemplate qt : questTemplates) {
            if (qt.getId().equalsIgnoreCase(id)) return qt;
        }
        return null;
    }

    public PlayerQuest getActiveQuest(UUID uuid) {
        return activePlayerQuests.get(uuid);
    }

    public void setActiveQuest(UUID uuid, String questId) {
        if (questId == null) {
            activePlayerQuests.remove(uuid);
        } else {
            activePlayerQuests.put(uuid, new PlayerQuest(questId, System.currentTimeMillis()));
        }
        savePlayerQuests();
    }
}
