package cz.craftmania.craftkeeper.utils;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Rank;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.model.user.User;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class Utils {

    public static Rank findPlayersRankByPermission(Player player) {
        String[] preparedPermissionRankNode = Main.getKeeperManager().getPermissionRankNode().split("\\.");
        Logger.debug("--------");
        Logger.debug("'" + Arrays.toString(preparedPermissionRankNode) + "'");

        Rank rank = null;
        User user = Main.getLuckPermsAPI().getUserManager().getUser(player.getUniqueId());
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();
        Map<String, Boolean> permissions = permissionData.getPermissionMap();
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            if (entry.getKey().startsWith(preparedPermissionRankNode[0])) {
                String ending = entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1);
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
