package com.medieval.economy.managers;

import com.medieval.economy.MedievalEconomyPlugin;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final MedievalEconomyPlugin plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, Boolean> scoreboardEnabled = new HashMap<>();

    public ScoreboardManager(MedievalEconomyPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllPlaytimes, 20L, 20L);
    }

    public boolean isEnabled(UUID uuid) {
        return scoreboardEnabled.getOrDefault(uuid, true);
    }

    public void setEnabled(UUID uuid, boolean enabled) {
        scoreboardEnabled.put(uuid, enabled);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            if (enabled) {
                setupScoreboard(player);
            } else {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }

    public void setupScoreboard(Player player) {
        if (!isEnabled(player.getUniqueId())) {
            return;
        }

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Component title = Component.text("TUEMPEK", NamedTextColor.YELLOW, TextDecoration.BOLD);

        Objective obj = board.registerNewObjective("medieval_sb", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Hapus angka skor merah di ujung kanan (Paper 1.21.1 API)
        try {
            obj.numberFormat(NumberFormat.blank());
        } catch (Throwable ignored) {}

        player.setScoreboard(board);
        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        if (!isEnabled(player.getUniqueId())) return;

        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("medieval_sb");
        if (obj == null) {
            setupScoreboard(player);
            return;
        }

        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        UUID uuid = player.getUniqueId();
        double dollar = economyManager.getDollar(uuid);
        double gold = economyManager.getGold(uuid);
        String playtime = formatPlaytime(player);
        int ping = player.getPing();

        String pingColor = ping < 80 ? "§a" : (ping < 150 ? "§e" : "§c");

        obj.getScore("§fPemain: §e" + player.getName()).setScore(5);
        obj.getScore("§fDollar: §a" + economyManager.formatDollar(dollar)).setScore(4);
        obj.getScore("§fGold: §6" + economyManager.formatGold(gold)).setScore(3);
        obj.getScore("§fMain: §b" + playtime).setScore(2);
        obj.getScore("§fPing: " + pingColor + ping + "ms").setScore(1);
    }

    private void updateAllPlaytimes() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isEnabled(player.getUniqueId())) {
                updateScoreboard(player);
            }
        }
    }

    private String formatPlaytime(Player player) {
        int ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long totalSeconds = ticks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        if (hours > 0) {
            return hours + "j " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
}
