package cz.craftmania.craftkeeper.listeners;

import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.ProtectedAsync;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class PlayerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Logger.debug("Načítám data hráče " + player.getName() + " (" + player.getUniqueId() + ")!");
        Main.getKeeperManager().loadKeeperPlayer(player);
        ProtectedAsync.runAsync(() -> {
            Main.getMultiplierManager().loadMultipliersForPlayer(player);
            List<Multiplier> multiplierList = Main.getMultiplierManager().getActiveMultipliersForPlayer(player);
            if (multiplierList.size() == 0) {
                return;
            }
            player.sendMessage("\n§e§lAktivní Multipliery§e:");
            double pb = 0.0;
            for (Multiplier multiplier : multiplierList) {
                player.sendMessage(" §8- §e" + multiplier.getType().translate() + " §7- §eBoost: §a" + (multiplier.getPercentageBoost() * 100) + "% §7- §eSkončí za: §a" + multiplier.getRemainingTimeReadable());
                pb += multiplier.getPercentageBoost();
            }
            if (multiplierList.size() > 1) {
                player.sendMessage("§eDohromady se prodejní tvoje ceny zvýší o §a" + (pb * 100) + "%§e!");
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        ProtectedAsync.runAsync(() -> {
            Main.getMultiplierManager().saveMultipliersForPlayer(e.getPlayer());
            Main.getMultiplierManager().unloadMultipliersFromCache(e.getPlayer());
        });
    }
}
