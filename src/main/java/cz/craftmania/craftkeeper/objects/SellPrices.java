package cz.craftmania.craftkeeper.objects;

import cz.craftmania.craftkeeper.utils.Logger;
import lombok.Getter;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Mělo by být ASYNC.
 */
public class SellPrices {

    private @Getter Rank rank;
    private @Getter Map<Material, Double> prices = new HashMap<>();

    public SellPrices(String rankName, Map<String, Double> pricesUnresolved) {
        this.rank = Rank.getByName(rankName);
        if (rank == null) {
            Logger.danger("[SELLPRICES]: Rank " + rankName + " neexistuje! V sellprices.yml nejspíše existuje rank, který je vyšší, než v pluginu.");
            return;
        }
        resolvePrices(rank, pricesUnresolved);
    }

    private void resolvePrices(Rank rank, Map<String, Double> pricesUnresolved) {
        Logger.debug("Resolvuji hodnoty pro Rank " + rank.getName() + "!");
        for (Map.Entry<String, Double> mapEntry : pricesUnresolved.entrySet()) {
            String blockName = mapEntry.getKey();
            Double blockValue = mapEntry.getValue();

            Material material = Material.getMaterial(blockName);
            if (material == null) {
                Logger.danger("Při resolvování hodnot pro Rank " + rank.getName() + " došlo k chybě - Materiál '" + blockName + "' s hodnotou '" + blockValue + "' neexistuje! Materiál bude přeskočen.");
                continue;
            }
            prices.put(material, blockValue);
        }
        Logger.success("Pro Rank " + rank.getName() + " bylo resolvováno " + prices.size() + " Materiálů s hodnoty!");
    }
}
