package cz.craftmania.craftkeeper.utils;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Rank;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.text.NumberFormat;
import java.util.Locale;

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

    public static String getFormattedNumber(double number) {
        return NumberFormat.getInstance(Locale.US).format(number);
    }

    public static String processBlockName(String name) {
        if (name.length() == 0)
            return name;
        name = name.toLowerCase();
        name = String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
        name = name.replaceAll("_", " ");
        return WordUtils.capitalizeFully(name);
    }
}
