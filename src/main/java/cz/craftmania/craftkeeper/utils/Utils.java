package cz.craftmania.craftkeeper.utils;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Rank;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class Utils {

    public static Rank findPlayersRankByPermission(Player player) {
        String[] preparedPermissionRankNode = Main.getKeeperManager().getPermissionRankNode().split("\\.");

        Rank rank = null;
        for (PermissionAttachmentInfo pio : player.getEffectivePermissions()) {
            String permissionNode = pio.getPermission();
            if (permissionNode.startsWith(preparedPermissionRankNode[0])) {
                String ending = permissionNode.substring(permissionNode.lastIndexOf('.') + 1);
                rank = Rank.getByName(ending);
            }
        }
        return rank;
    }
}
