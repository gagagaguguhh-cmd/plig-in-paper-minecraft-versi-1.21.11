package com.medieval.economy.listeners;

import com.medieval.economy.managers.EconomyManager;
import com.medieval.economy.managers.ScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final EconomyManager economyManager;
    private final ScoreboardManager scoreboardManager;

    public PlayerJoinListener(EconomyManager economyManager, ScoreboardManager scoreboardManager) {
        this.economyManager = economyManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        economyManager.registerPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        scoreboardManager.setupScoreboard(event.getPlayer());
    }
}
