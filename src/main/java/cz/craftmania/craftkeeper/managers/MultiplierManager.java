package cz.craftmania.craftkeeper.managers;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.objects.MultiplierType;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.MessageMaker;
import cz.craftmania.craftkeeper.utils.ProtectedAsync;
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
        try {
            int prestigeLevel = Main.getPrisonAPI().getPlayer(player).getPrestige();
            if (prestigeLevel >= 2) {
                boostingBy += (prestigeLevel - 1) * 5;

            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.danger("Chyba při získávání hráče z PrisonAPI!");
            Main.getInstance().sendSentryException(e);
        }
        boostingBy += 100;
        valueAfterEnchancing = baseValue / (double) 100 * boostingBy;
        return valueAfterEnchancing;
    }

    public void loadMultipliersForPlayer(Player player) {
        List<Multiplier> multipliers = Main.getSqlManager().getPlayersMultipliers(player);
        this.multipliers.addAll(multipliers);
    }

    public void loadGlobalMultipliers() {
        List<Multiplier> multipliers = Main.getSqlManager().getGlobalMultipliers();
        this.multipliers.addAll(multipliers);
        Logger.info("Načteno " + multipliers.size() + " GLOBAL a/nebo EVENT Multiplierů!");
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
            switch (multiplier.getType()) {
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
        try {
            for (Multiplier multiplier : multipliers) {
                if (multiplier.getTargetUUID().equals(player.getUniqueId())) {
                    if (multiplier.getType() == MultiplierType.PERSONAL) {
                        returnValues.add(multiplier);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return returnValues;
    }

    public Multiplier getMultiplierByInternalID(long internalID) {
        for (Multiplier multiplier : multipliers) {
            if (multiplier.getInternalID() == internalID) {
                return multiplier;
            }
        }
        return null;
    }

    // Managers

    public void addMultiplier(Multiplier multiplier) {
        Logger.debug("[MULTIPLIERS] Přidávání Multiplieru '" + multiplier.toString()+ "'...");
        Main.getSqlManager().createMultiplier(multiplier);
        multipliers.add(multiplier);
    }

    public boolean removeMultiplier(Multiplier multiplier) {
        int counter = 0;
        for (Multiplier multiplierInList : multipliers) {
            if (multiplierInList.getInternalID() == multiplier.getInternalID()) {
                Logger.debug("[MULTIPLIERS] Odstraňování Multiplieru '" + multiplier.toString() + "'...");
                multipliers.remove(counter);
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
        final int[] counter = {0};
        new BukkitRunnable() {
            @Override
            public void run() {
                Logger.debug("[MULTIPLIERS]§8 Probíhá update všech multiplierů...");
                List<Multiplier> multipliersCopy = new ArrayList<>(multipliers);
                for (Multiplier multiplier : multipliersCopy) {
                    if (multiplier.getType() == MultiplierType.PERSONAL) {
                        Player player = Bukkit.getPlayer(multiplier.getTargetUUID());
                        if (player == null)
                            continue;
                    }
                    long remainingLength = multiplier.getRemainingLength();
                    remainingLength -= 10000; // 10000ms = 10s
                    if (remainingLength <= 0) {
                        MessageMaker.announceEnd(multiplier);
                        removeMultiplier(multiplier);
                    } else {
                        multiplier.setRemainingLength(remainingLength);
                        updateMultiplier(multiplier);
                    }
                }
                if (counter[0] == Main.getInstance().getConfig().getInt("multipliers.save-every-minute") * 6) {
                    counter[0] = 0;
                    ProtectedAsync.runAsync(() -> {
                        updateAllMultipliersToDatabase();
                    });
                } else
                    counter[0]++;

                Logger.debug("[MULTIPLIERS]§8 Update všech multiplierů dokončen!");
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 20L * 10); // Každých 10s
    }

    private void updateAllMultipliersToDatabase() {
        Logger.debug("[MULTIPLIERS]§7 Aktulizuji všechny multipliery do DB.");
        Main.getSqlManager().updateListOfMultipliersRemainingLength(new ArrayList<>(this.multipliers));
    }
}
