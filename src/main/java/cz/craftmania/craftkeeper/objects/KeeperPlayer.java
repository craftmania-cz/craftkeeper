package cz.craftmania.craftkeeper.objects;

import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.Utils;
import lombok.Getter;
import org.bukkit.entity.Player;

public class KeeperPlayer {

    private @Getter Player player;
    private @Getter Rank playerRank;

    public KeeperPlayer(Player player) {
        this.player = player;
        refreshPlayerRank();
    }

    public void refreshPlayerRank() {
        playerRank = Utils.findPlayersRankByPermission(player);
        if (playerRank == null) {
            Logger.danger("Při načítání player ranku pro hráče " + player.getName() + " (" + player.getUniqueId() + ") došlo k chybě! Defaultní rank bude 1...");
            playerRank = Rank.getByWeight(1);
        }
    }
}
