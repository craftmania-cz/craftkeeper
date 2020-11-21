package cz.craftmania.craftkeeper.managers;

import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.objects.MultiplierType;
import cz.craftmania.craftkeeper.utils.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class MultiplierManager {

    private @Getter List<Multiplier> multipliers = new ArrayList<>();

    public MultiplierManager() {
        runMultiplierTimer();
    }

    public Double enhanceSellValue(Player player, Double baseValue) {
        Double valueAfterEnchancing;
        List<Multiplier> multipliers = getActiveMultipliersForPlayer(player);
        double boostingBy = 0;
        for (Multiplier multiplier : multipliers) {
            boostingBy += multiplier.getPercentageBoost() * 100; // 0.5 -> 50%
        }
        boostingBy += 100;
        valueAfterEnchancing = baseValue / (double) 100 * boostingBy;
        return valueAfterEnchancing;
    }

    public void loadMultipliersForPlayer(Player player) {
        List<Multiplier> multipliers = Main.getSqlManager().getPlayersMultipliers(player);
        this.multipliers.addAll(multipliers);
    }

    public void saveMultipliersForPlayer(Player player) {
        List<Multiplier> multipliers = getPersonalMultipliersByPlayer(player);
        for (Multiplier multiplier : multipliers) {
            Main.getSqlManager().updateMultiplierRemainingLength(multiplier);
        }
    }

    public void unloadMultipliersFromCache(Player player) {
        multipliers.removeAll(getPersonalMultipliersByPlayer(player));
    }

    // Getters

    /**
     * Vrátí všechny multipliery, které jsou pro daného hráče (Personal + Global + Event). Pokud chceš získat
     * jen multipliery, které hráč vlasntí, využij {@link #getActiveMultipliersForPlayer(Player)}
     *
     * @param player
     *
     * @return
     */
    public List<Multiplier> getActiveMultipliersForPlayer(Player player) {
        List<Multiplier> returnValues = new ArrayList<>();
        for (Multiplier multiplier : multipliers) {
            switch (multiplier.getMultiplierType()) {
                case PERSONAL:
                    if (multiplier.getTargetUUID().equals(player.getUniqueId())) {
                        returnValues.add(multiplier);
                    }
                    break;
                case EVENT:
                case GLOBAL:
                    returnValues.add(multiplier);
                    break;
            }
        }
        return returnValues;
    }

    public List<Multiplier> getPersonalMultipliersByPlayer(Player player) {
        List<Multiplier> returnValues = new ArrayList<>();
        for (Multiplier multiplier : multipliers) {
            if (multiplier.getTargetUUID().equals(player.getUniqueId())) {
                if (multiplier.getMultiplierType() == MultiplierType.PERSONAL) {
                    returnValues.add(multiplier);
                }
            }
        }
        return returnValues;
    }

    // Managers

    public void addMultiplier(Multiplier multiplier) {
        Main.getSqlManager().createMultiplier(multiplier);
        multipliers.add(multiplier);
    }

    public boolean removeMultiplier(Multiplier multiplier) {
        int counter = 0;
        for (Multiplier multiplierInList : multipliers) {
            if (multiplierInList.getInternalID() == multiplier.getInternalID()) {
                multipliers.remove(counter);
                if (multiplier.getMultiplierType() == MultiplierType.PERSONAL) {
                    Player player = Bukkit.getPlayer(multiplier.getTargetUUID());
                    if (player != null) {
                        ChatInfo.warning(player, "Vypršel ti Multiplier s " + (multiplier.getPercentageBoost() * 100) + "% boostem!");
                    }
                }
                Main.getSqlManager().removeMultiplier(multiplier);
                return true;
            }
            counter++;
        }
        return false;
    }

    public boolean updateMultiplier(Multiplier multiplier) {
        int counter = 0;
        for (Multiplier multiplierInList : multipliers) {
            if (multiplierInList.getInternalID() == multiplier.getInternalID()) {
                multipliers.set(counter, multiplier);
                return true;
            }
            counter++;
        }
        return false;
    }

    // Timer

    public void runMultiplierTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                //Logger.debug("Refreshing multipliers...");
                List<Multiplier> multipliersCopy = new ArrayList<>(multipliers);
                for (Multiplier multiplier : multipliersCopy) {
                    if (multiplier.getMultiplierType() == MultiplierType.PERSONAL) {
                        Player player = Bukkit.getPlayer(multiplier.getTargetUUID());
                        if (player == null)
                            continue;
                    }
                    //Logger.debug("Checking multiplier: " + multiplier.toString());
                    long remainingLength = multiplier.getRemainingLength();
                    remainingLength -= 10000; // 10000ms = 10s
                    if (remainingLength <= 0) {
                        //Logger.debug(" - Result: Deleting!");
                        removeMultiplier(multiplier);
                    } else {
                        //Logger.debug(" - Result: Updating!");
                        multiplier.setRemainingLength(remainingLength);
                        updateMultiplier(multiplier);
                    }
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 20L * 10); // Každých 10s
    }
}
