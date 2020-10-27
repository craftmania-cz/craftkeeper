package cz.craftmania.craftkeeper.objects;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.Utils;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class KeeperPlayer {

    private @Getter Player player;
    private @Getter Rank playerRank;
    private @Getter boolean inAutoSellMode = false;
    private @Getter double toPayFromAutosell = 0.0;
    private @Getter long autosellCooldownTo = 0;
    private @Getter long autosellDurationTo = 0;
    private boolean canHaveAutosell = true;

    public KeeperPlayer(Player player) {
        this.player = player;
        refreshPlayerRank();
    }

    public void addToPay(double amount) {
        toPayFromAutosell += amount;
    }

    public long getRemainingDurationInMS() {
        if (autosellDurationTo == 0)
            return 0;
        else
            return (System.currentTimeMillis() - autosellDurationTo) * -1;
    }

    public long getRemainingCooldownInMS() {
        if (autosellCooldownTo == 0)
            return 0;
        else
            return (System.currentTimeMillis() - autosellCooldownTo) * -1;
    }

    public boolean canHaveAutosell() {
        return canHaveAutosell;
    }

    public void disableAutosellMode() {
        FileConfiguration config = Main.getInstance().getConfig();

        inAutoSellMode = false;
        autosellDurationTo = 0;

        boolean cooldownEnabled = config.getBoolean("autosell.cooldown.enabled");
        if (cooldownEnabled) {
            canHaveAutosell = false;
            long cooldownDuration = config.getLong("autosell.cooldown.time");
            autosellCooldownTo = System.currentTimeMillis() + cooldownDuration;

            new BukkitRunnable() { // Hráčovi povolí autosell <- Vypršel cooldown duration
                @Override
                public void run() {
                    Logger.debugBlockBreak("Hráčovi '" + player.getName() + "' vypršel cooldown!");
                    //TODO: Event na to udělat
                    autosellCooldownTo = 0;
                    canHaveAutosell = true;
                }
            }.runTaskLaterAsynchronously(Main.getInstance(), (20 * cooldownDuration) / 1000); // ms -> ticks
        }
    }

    public void enableAutosellMode() {
        inAutoSellMode = true;
        canHaveAutosell = false;

        FileConfiguration config = Main.getInstance().getConfig();

        boolean infiniteDuration = config.getBoolean("autosell.duration.infinite");
        if (infiniteDuration) {
            autosellCooldownTo = -1;
        } else {
            long duration = Main.getInstance().getConfig().getLong("autosell.duration.time");
            autosellDurationTo = System.currentTimeMillis() + duration;

            new BukkitRunnable() { // Hráčovi dojde duration při autosellu <- Zapne se cooldown duration
                @Override
                public void run() {
                    Logger.debugBlockBreak("Hráčovi '" + player.getName() + "' vypršel duration!");
                    //TODO: Event na to
                    autosellDurationTo = 0;
                    inAutoSellMode = false;
                    canHaveAutosell = false;
                    updateVaultBalance();

                    boolean cooldownEnabled = config.getBoolean("autosell.cooldown.enabled");
                    if (cooldownEnabled) {
                        canHaveAutosell = false;
                        long cooldownDuration = config.getLong("autosell.cooldown.time");
                        autosellCooldownTo = System.currentTimeMillis() + cooldownDuration;

                        new BukkitRunnable() { // Hráčovi povolí autosell <- Vypršel cooldown duration
                            @Override
                            public void run() {
                                Logger.debugBlockBreak("Hráčovi '" + player.getName() + "' vypršel cooldown!");
                                //TODO: Event na to udělat
                                autosellCooldownTo = 0;
                                canHaveAutosell = true;
                            }
                        }.runTaskLaterAsynchronously(Main.getInstance(), (20 * cooldownDuration) / 1000); // ms -> ticks
                    }
                }
            }.runTaskLaterAsynchronously(Main.getInstance(), (20 * duration) / 1000); // ms -> ticks
        }
    }

    public void updateVaultBalance() {
        if (toPayFromAutosell == 0.0)
            return;
        Main.getVaultEconomy().depositPlayer(player, toPayFromAutosell);
        toPayFromAutosell = 0.0;
    }

    public void refreshPlayerRank() {
        playerRank = Utils.findPlayersRankByPermission(player);
        if (playerRank == null) {
            Logger.danger("Při načítání player ranku pro hráče " + player.getName() + " (" + player.getUniqueId() + ") došlo k chybě! Defaultní rank bude 1...");
            playerRank = Rank.getByWeight(1);
        }
    }
}
