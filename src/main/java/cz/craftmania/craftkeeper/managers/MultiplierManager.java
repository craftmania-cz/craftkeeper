package cz.craftmania.craftkeeper.managers;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.utils.Logger;
import lombok.Getter;
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
            boostingBy+= multiplier.getPercentageBoost() * 100; // 0.5 -> 50%
        }
        boostingBy+= 100;
        valueAfterEnchancing = baseValue / (double)100 * boostingBy;
        return valueAfterEnchancing;
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
                        if (multiplier.isActive()) {
                            returnValues.add(multiplier);
                        }
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

    /**
     * Vrátí multipliery, které hráč vlastní (Personal + Global). Pokud chceš získat
     * všechny multipliery, které jsou aktivní pro daného hráče (Persona + Global + Event), využij {@link #getActiveMultipliersForPlayer(Player)}
     *
     * @param player
     *
     * @return
     */
    public List<Multiplier> getMultipliersByPlayer(Player player) {
        List<Multiplier> returnValues = new ArrayList<>();
        for (Multiplier multiplier : multipliers) {
            if (multiplier.getTargetUUID().equals(player.getUniqueId()))
                returnValues.add(multiplier);
        }
        return returnValues;
    }

    // Managers

    public void addMultiplier(Multiplier multiplier) {
        //todo: Ukládání multiplierů do SQL
        multipliers.add(multiplier);
    }

    public boolean removeMultiplier(Multiplier multiplier) {
        int counter = 0;
        for (Multiplier multiplierInList : multipliers) {
            if (multiplierInList.getInternalID() == multiplier.getInternalID()) {
                multipliers.remove(counter);
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
                Logger.debug("Refreshing multipliers...");
                List<Multiplier> multipliersCopy = new ArrayList<>(multipliers);
                for (Multiplier multiplier : multipliersCopy) {
                    if (multiplier.isActive()) {
                        Logger.debug("Checking multiplier: " + multiplier.toString());
                        long remainingLength = multiplier.getRemainingLength();
                        remainingLength -= 10000; // 10000ms = 10s
                        if (remainingLength <= 0) {
                            Logger.debug(" - Result: Deleting!");
                            removeMultiplier(multiplier);
                            //todo post event skončený multiplier
                        } else {
                            Logger.debug(" - Result: Updating!");
                            multiplier.setRemainingLength(remainingLength);
                            updateMultiplier(multiplier);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 20L * 10); // Každých 10s
    }
}
