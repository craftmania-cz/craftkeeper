package cz.craftmania.craftkeeper.extension;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;

public class KeeperExtension extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "craftkeeper";
    }

    @Override
    public String getAuthor() {
        return "CraftMania.cz";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        switch (identifier) {
            case "money": {
                try {
                    KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
                    double money = Main.getVaultEconomy().getBalance(player) + keeperPlayer.getToPayFromAutosell();
                    return Utils.getFormattedNumber(money);
                } catch (Exception exception) {
                    return "exception";
                }
            }
            case "cached_money": {
                try {
                    KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
                    return Utils.getFormattedNumber(keeperPlayer.getToPayFromAutosell());
                } catch (Exception ignored) {
                    return "exception";
                }
            }
            case "autosell_cooldown": {
                try {
                    KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
                    return String.valueOf(keeperPlayer.getRemainingCooldownInMS() / 1000);
                } catch (Exception ignored) {
                    return "exception";
                }
            }
            case "autosell_duration": {
                try {
                    KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
                    return String.valueOf(keeperPlayer.getRemainingDurationInMS() / 1000);
                } catch (Exception ignored) {
                    return "exception";
                }
            }
            case "total_multiplier_boost": {
                try {
                    List<Multiplier> activeMultipliers = Main.getMultiplierManager().getActiveMultipliersForPlayer(player);
                    double pb = 0.0;
                    for (Multiplier multiplier : activeMultipliers) {
                        pb += multiplier.getPercentageBoost();
                    }
                    try {
                        int prestigeLevel = Main.getPrisonAPI().getPlayer(player).getPrestige();
                        if (prestigeLevel >= 2) {
                            pb += (prestigeLevel - 1) * 5;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.danger("Chyba při získávání hráče z PrisonAPI!");
                        Main.getInstance().sendSentryException(e);
                    }
                    return (pb * 100) + "";
                } catch (Exception ignored) {
                    return "exception";
                }
            }

        }
        return "";
    }
}
