package cz.craftmania.craftkeeper.managers;

import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.events.PlayerAutosellGotPaidEvent;
import cz.craftmania.craftkeeper.events.PlayerSellallEvent;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.objects.Rank;
import cz.craftmania.craftkeeper.objects.SellPrices;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.ProtectedAsync;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellManager {

    private @Getter List<SellPrices> sellPricesList = new ArrayList<>();

    public SellManager() {
        reloadPrices();
    }

    public Double getPriceOfItemstackByRank(ItemStack itemStack, Rank rank) {
        SellPrices sellPrices = Main.getSellManager().getSellPricesByRank(rank);
        return sellPrices.getPrices().get(itemStack.getType());
    }

    public void sellEverythingByRank(KeeperPlayer keeperPlayer, Rank rank) {
        if (keeperPlayer.getPlayerRank().getWeight() < rank.getWeight()) {
            Logger.danger("Pozor! Hráčovi " + keeperPlayer.getPlayer().getName() + " se prodávají věci pomocí vyššího ranku (" + rank.getName() + ") než má on sám (" + keeperPlayer.getPlayerRank().getName() + ")!");
        }
        ProtectedAsync.runAsync(() -> {
            Player player = keeperPlayer.getPlayer();
            PlayerInventory playerInventory = player.getInventory();
            SellPrices sellPrices = Main.getSellManager().getSellPricesByRank(rank);
            Double moneyToAdd = 0.0;
            for (ItemStack itemInInvetory : playerInventory.getContents()) {
                if (itemInInvetory == null)
                    continue;
                if (sellPrices.getPrices().containsKey(itemInInvetory.getType())) {
                    Double price = sellPrices.getPrices().get(itemInInvetory.getType());
                    moneyToAdd += itemInInvetory.getAmount() * price;
                    playerInventory.remove(itemInInvetory);
                }
            }
            if (moneyToAdd == 0) {
                ChatInfo.warning(player, "Nemáš v inventáři žádný materiál, který by odpovídal tomuto shopu, takže jsi nic neprodal!");
                return;
            }
            Main.getVaultEconomy().depositPlayer(player, moneyToAdd);
            Double finalMoneyToAdd = moneyToAdd;
            Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(new PlayerSellallEvent(keeperPlayer, finalMoneyToAdd));
                }
            });
        });
    }

    public SellPrices getSellPricesByRank(Rank rank) {
        for (SellPrices sellPrices : sellPricesList) {
            if (sellPrices.getRank().getWeight() == rank.getWeight())
                return sellPrices;
        }
        return null;
    }

    public void reloadPrices() {
        Logger.info("Začínám s načítáním prodávajících hodnot materiálů...");
        long start = System.currentTimeMillis();
        sellPricesList.clear();

        Main.getConfigAPI().loadConfigs();
        ConfigurationSection config = Main.getConfigAPI().getConfig("sellprices").getConfig().getConfigurationSection("sellPrices");
        if (config == null) {
            Logger.debug("V sellprices.yml neexistuje 'sellPrices'! Hodnoty nebudou načteny.");
            return;
        }
        Logger.debug("Zjišťuji data do " + Rank.getLast().getWeight() + ". ranku...");
        List<String> availableRanksStrings = new ArrayList<>(config.getKeys(false));
        List<Integer> availableRanks = new ArrayList<>();
        for (String rankNumber : availableRanksStrings) {
            try {
                availableRanks.add(Integer.parseInt(rankNumber));
            } catch (Exception e) {
                e.printStackTrace();
                Logger.danger("V sellPrices.json se je nějaký rank označený stringem, místo číslem!");
            }
        }
        //int maxSavedRank = getMaxInt(availableRanks);
        for (Integer rankNumber : availableRanks) {
            ConfigurationSection section = config.getConfigurationSection(rankNumber.toString());
            if (section == null) {
                Logger.danger("Nastala chyba, která by nikdy neměla nastat. RankNumber: " + rankNumber + "; section == null (viz. SellPricesManager)");
                continue;
            }
            List<String> materials = new ArrayList<>(section.getKeys(false));
            Map<String, Double> materialsWithPrices = new HashMap<>();
            for (String material : materials) {
                Double price = section.getDouble(material);
                materialsWithPrices.put(material, price);
            }
            sellPricesList.add(new SellPrices(rankNumber, materialsWithPrices));
        }
        Logger.success("Dokončeno načítání prodávajících hodnot materiálů! (Trvalo " + (System.currentTimeMillis() - start) + "ms)");
    }

    // Utils
    private int getMaxInt(List<Integer> list) {
        int maxNum = 0;
        for (Integer number : list) {
            if (maxNum < number)
                maxNum = number;
        }
        return maxNum;
    }
}
