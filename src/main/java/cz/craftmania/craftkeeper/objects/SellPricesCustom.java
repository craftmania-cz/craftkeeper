package cz.craftmania.craftkeeper.objects;

import cz.craftmania.craftkeeper.utils.Logger;
import lombok.Getter;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class SellPricesCustom {

    private @Getter String mineName;
    private @Getter Map<Material, Double> prices = new HashMap<>();

    public SellPricesCustom(String mineName, Map<String, Double> pricesUnresolved) {
        this.mineName = mineName;
        resolvePrices(mineName, pricesUnresolved);
    }

    private void resolvePrices(String mineName, Map<String, Double> pricesUnresolved) {
        Logger.debug("Resolvuji hodnoty pro mine " + mineName + "!");
        for (Map.Entry<String, Double> mapEntry : pricesUnresolved.entrySet()) {
            String blockName = mapEntry.getKey();
            Double blockValue = mapEntry.getValue();

            Material material = Material.getMaterial(blockName);
            if (material == null) {
                Logger.danger("Při resolvování hodnot pro mine " + mineName + " došlo k chybě - Materiál '" + blockName + "' s hodnotou '" + blockValue + "' neexistuje! Materiál bude přeskočen.");
                continue;
            }
            prices.put(material, blockValue);
        }
        Logger.success("Pro mine " + mineName + " bylo resolvováno " + prices.size() + " Materiálů s hodnoty!");
    }
}
