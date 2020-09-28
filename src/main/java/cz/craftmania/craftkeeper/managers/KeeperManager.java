package cz.craftmania.craftkeeper.managers;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.utils.Logger;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KeeperManager {

    private @Getter List<KeeperPlayer> keeperPlayers = new ArrayList<>();
    private @Getter String permissionRankNode;

    public KeeperManager() {
        refreshPermissionNode();
    }

    public KeeperPlayer loadKeeperPlayer(Player player) {
        KeeperPlayer keeperPlayer = getKeeperPlayer(player);
        if (keeperPlayer == null) {
            KeeperPlayer newKeeperPlayer = new KeeperPlayer(player);
            keeperPlayers.add(newKeeperPlayer);
            return newKeeperPlayer;
        }
        keeperPlayer.refreshPlayerRank();
        return keeperPlayer;
    }

    // // //

    public boolean playerIsLoaded(Player player) {
        return getKeeperPlayer(player) != null;
    }

    public KeeperPlayer getKeeperPlayer(Player player) {
        for (KeeperPlayer keeperPlayer : keeperPlayers) {
            if (keeperPlayer.getPlayer().getUniqueId().equals(player.getUniqueId()))
                return keeperPlayer;
        }
        return null;
    }

    // Utils

    public void refreshPermissionNode() {
        String permission = Main.getInstance().getConfig().getString("permissionRankNode");
        if (permission == null) {
            Logger.danger("PermissionRankNode v configu není nastavený! Používám defaultní 'craftprison.rank.x'...");
            permission = "craftprison.rank.x";
        }
        if (permission.charAt(permission.length() - 1) != 'x') {
            Logger.danger("PermissionRankNode nemá na konci 'x'! Používám defaultní 'craftprison.rank.x'...");
            permission = "craftprison.rank.x";
        }
        permissionRankNode = permission;
    }
}
