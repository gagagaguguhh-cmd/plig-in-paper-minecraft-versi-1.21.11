package com.medieval.economy;

import com.medieval.economy.commands.*;
import com.medieval.economy.listeners.*;
import com.medieval.economy.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class MedievalEconomyPlugin extends JavaPlugin {

    private EconomyManager economyManager;
    private ShopManager shopManager;
    private SellManager sellManager;
    private AuctionManager auctionManager;
    private OrderManager orderManager;
    private ScoreboardManager scoreboardManager;
    private SettingsManager settingsManager;
    private RTPManager rtpManager;
    private RPGManager rpgManager;
    private NPCManager npcManager;
    private QuestManager questManager;

    @Override
    public void onEnable() {
        // Initialize managers
        this.economyManager = new EconomyManager(this);
        this.shopManager = new ShopManager();
        this.sellManager = new SellManager();
        this.auctionManager = new AuctionManager(this);
        this.orderManager = new OrderManager(this);
        this.scoreboardManager = new ScoreboardManager(this, economyManager);
        this.settingsManager = new SettingsManager(this);
        this.rtpManager = new RTPManager(this);
        this.rpgManager = new RPGManager(this);
        this.npcManager = new NPCManager(this);
        this.questManager = new QuestManager(this);

        // Register commands
        getCommand("balance").setExecutor(new BalanceCommand(economyManager));
        getCommand("pay").setExecutor(new PayCommand(economyManager, scoreboardManager));
        getCommand("akun").setExecutor(new AkunCommand(economyManager, rpgManager));
        getCommand("shop").setExecutor(new ShopCommand(shopManager, economyManager));
        getCommand("sell").setExecutor(new SellCommand());
        getCommand("auctions").setExecutor(new AuctionsCommand(auctionManager, economyManager));
        getCommand("order").setExecutor(new OrderCommand(orderManager, economyManager));
        getCommand("settings").setExecutor(new SettingsCommand(scoreboardManager, settingsManager));
        getCommand("rtp").setExecutor(new RTPCommand(this, rtpManager));

        // Register event listeners
        SpecialNPCListener specialNPCListener = new SpecialNPCListener(this, npcManager);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(economyManager, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(economyManager, shopManager, sellManager, auctionManager, orderManager, scoreboardManager, settingsManager), this);
        getServer().getPluginManager().registerEvents(new MonsterToggleListener(settingsManager), this);
        getServer().getPluginManager().registerEvents(new RTPDimensionUnlockListener(rtpManager), this);
        getServer().getPluginManager().registerEvents(new CommandBlockerListener(), this);
        getServer().getPluginManager().registerEvents(specialNPCListener, this);
        getServer().getPluginManager().registerEvents(new SpecialNPCInteractListener(this, npcManager, rpgManager, questManager, economyManager, specialNPCListener.getNpcTypeKey()), this);
        getServer().getPluginManager().registerEvents(new RPGTrackingListener(this, rpgManager, npcManager), this);

        getLogger().info("MedievalEconomy plugin 1.0.0 (Command Blocker & UI & RTP & Special NPCs & RPG Quests) berhasil diaktifkan!");
    }

    @Override
    public void onDisable() {
        if (auctionManager != null) {
            auctionManager.saveAuctions();
        }
        if (orderManager != null) {
            orderManager.saveOrders();
        }
        if (economyManager != null) {
            economyManager.saveData();
        }
        if (settingsManager != null) {
            settingsManager.saveSettings();
        }
        if (rtpManager != null) {
            rtpManager.saveData();
        }
        if (rpgManager != null) {
            rpgManager.saveData();
        }
        if (npcManager != null) {
            npcManager.saveData();
        }
        if (questManager != null) {
            questManager.savePlayerQuests();
        }
        getLogger().info("MedievalEconomy plugin telah dinonaktifkan.");
    }
}
