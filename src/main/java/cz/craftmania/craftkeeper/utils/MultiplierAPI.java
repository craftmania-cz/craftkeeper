package cz.craftmania.craftkeeper.utils;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.objects.MultiplierType;
import org.bukkit.entity.Player;

/**
 * Slouží k přidávání Multiplierů (Ostatní pluginy, ...)
 */
public class MultiplierAPI {

    /**
     * Vytvoří nový multiplier s typem PERSONAL.
     *
     * @param player Hráč
     * @param length Délka multiplieru v MS
     * @param percentageBoost Procento boostu cen
     */
    public static Multiplier createNewPersonalMultiplier(Player player, long length, double percentageBoost) {
        Multiplier multiplier = new Multiplier(
                MultiplierType.PERSONAL,
                player.getName(),
                player.getUniqueId(),
                length,
                length,
                percentageBoost,
                false
        );
        Main.getMultiplierManager().addMultiplier(multiplier);
        //Post event #MultiplierCreatedEvent()
        return multiplier;
    }

    /**
     * Vytvoří multiplier s typem GLOBAL.
     *
     * @param length Délka multiplier v MS
     * @param percentageBoost Procento boostu cen
     */
    public static Multiplier createNewGlobalMultiplier(Player player, long length, double percentageBoost) {
        Multiplier multiplier = new Multiplier(
                MultiplierType.GLOBAL,
                player.getName(),
                player.getUniqueId(),
                length,
                length,
                percentageBoost,
                true
        );
        Main.getMultiplierManager().addMultiplier(multiplier);
        //Post event #MultiplierCreatedEvent()
        return multiplier;
    }

    /**
     * Vytvoří nový multiplier s typem EVENT.
     *
     * @param length Délka multiplier v MS
     * @param percentageBoost Procento boostu cen
     */
    public static Multiplier createNewEventMultiplier(long length, double percentageBoost) {
        Multiplier multiplier = new Multiplier(
                MultiplierType.EVENT,
                "@a",
                null,
                length,
                length,
                percentageBoost,
                true
        );
        Main.getMultiplierManager().addMultiplier(multiplier);
        return multiplier;
    }
}
