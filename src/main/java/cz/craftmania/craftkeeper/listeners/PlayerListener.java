package cz.craftmania.craftkeeper.listeners;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.utils.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Logger.debug("Načítám data hráče " + e.getPlayer() + " (" + e.getPlayer().getUniqueId() + ")!");
        Main.getKeeperManager().loadKeeperPlayer(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        
    }
}
