package com.medieval.economy.listeners;

import com.medieval.economy.managers.RTPManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class RTPDimensionUnlockListener implements Listener {

    private final RTPManager rtpManager;

    public RTPDimensionUnlockListener(RTPManager rtpManager) {
        this.rtpManager = rtpManager;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        World world = event.getPlayer().getWorld();

        if (world.getEnvironment() == World.Environment.NETHER) {
            rtpManager.unlockNether();
        } else if (world.getEnvironment() == World.Environment.THE_END) {
            rtpManager.unlockEnd();
        }
    }
}
