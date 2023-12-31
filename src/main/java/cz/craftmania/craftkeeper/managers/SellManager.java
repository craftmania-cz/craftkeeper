package cz.craftmania.craftkeeper.managers;

import at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.events.PlayerSellallEvent;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.objects.SellPrices;
import cz.craftmania.craftkeeper.objects.SellPricesCustom;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.Utils;
import cz.wake.craftprison.objects.Rank;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellManager {

    private @Getter List<SellPrices> sellPricesList = new ArrayList<>();
    private @Getter List<SellPricesCustom> sellPricesCustomsList = new ArrayList<>();

    public SellManager() {
        reloadPrices();
    }

    public Double getPriceOfItemstackByRank(Player player, ItemStack itemStack, Rank rank) {
        SellPrices sellPrices = Main.getSellManager().getSellPricesByRank(rank);
        Double sellPricesBase = sellPrices.getPrices().get(itemStack.getType());
        return Main.getMultiplierManager().enhanceSellValue(player, sellPricesBase);
    }

    public Double getPriceOfItemstackByMineName(Player player, ItemStack itemStack, String mineName) {
        SellPricesCustom sellPrices = Main.getSellManager().getSellPricesByMineName(mineName);
        Double sellPricesBase = sellPrices.getPrices().get(itemStack.getType());
        return Main.getMultiplierManager().enhanceSellValue(player, sellPricesBase);
    }


    public void sellEverythingByRank(KeeperPlayer keeperPlayer, Rank rank) {
        if (keeperPlayer.getPlayerRank().getWeight() < rank.getWeight()) {
            Logger.danger("Pozor! Hráčovi " + keeperPlayer.getPlayer().getName() + " se prodávají věci pomocí vyššího ranku (" + rank.getName() + ") než má on sám (" + keeperPlayer.getPlayerRank().getName() + ")!");
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = keeperPlayer.getPlayer();
                PlayerInventory playerInventory = player.getInventory();
                SellPrices sellPrices = Main.getSellManager().getSellPricesByRank(rank);
                double moneyToAdd = 0.0;
                for (ItemStack itemInInvetory : playerInventory.getContents()) {
                    if (itemInInvetory == null)
                        continue;
                    if (sellPrices.getPrices().containsKey(itemInInvetory.getType())) {
                        Double price = sellPrices.getPrices().get(itemInInvetory.getType());
                        moneyToAdd += itemInInvetory.getAmount() * price;
                        itemInInvetory.setAmount(0);
                    }
                }

                int sellBackpack = Main.getSqlManager().getSettings(player, "prison_sell_backpack");
                if (sellBackpack == 1) {
                    Inventory backpackInventory = null;
                    Backpack bp = Main.getMinepacksPlugin().getBackpackCachedOnly(player);
                    if (bp != null) {
                        backpackInventory = bp.getInventory();
                        for (ItemStack itemInBackpack : backpackInventory.getContents()) {
                            if (itemInBackpack == null)
                                continue;
                            if (sellPrices.getPrices().containsKey(itemInBackpack.getType())) {
                                Double price = sellPrices.getPrices().get(itemInBackpack.getType());
                                moneyToAdd += itemInBackpack.getAmount() * price;
                                itemInBackpack.setAmount(0);
                            }
                        }
                    }
                }

                if (moneyToAdd == 0.0) {
                    ChatInfo.warning(player, "Nemáš v inventáři žádný materiál, který by odpovídal tomuto shopu, takže jsi nic neprodal!");
                    return;
                }

                double moneyToAddWithoutEnhance = moneyToAdd;
                moneyToAdd = Main.getMultiplierManager().enhanceSellValue(player, moneyToAdd);

                String message = "§aProdal jsi materiál a bylo ti přidáno §e" + Utils.formatMoney(Math.round(moneyToAddWithoutEnhance)) + "§6$";
                if ((int) moneyToAdd != (int) moneyToAddWithoutEnhance)
                    message += "§a (§e+ " + Utils.formatMoney(Math.round(moneyToAdd - moneyToAddWithoutEnhance - 1)) + "§6$§a)!";
                else
                    message += "§a!";
                Main.getVaultEconomy().depositPlayer(player, moneyToAdd);
                ChatInfo.success(player, message);
                Double finalMoneyToAdd = moneyToAdd;
                Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        Bukkit.getPluginManager().callEvent(new PlayerSellallEvent(keeperPlayer, finalMoneyToAdd));
                    }
                });
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public void sellEverythingByMineName(KeeperPlayer keeperPlayer, String mineName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = keeperPlayer.getPlayer();
                PlayerInventory playerInventory = player.getInventory();
                SellPricesCustom sellPrices = Main.getSellManager().getSellPricesByMineName(mineName);

                if (sellPrices == null) {
                    ChatInfo.warning(player, "Zadal jsi neplatné jméno dolu!");
                    return;
                }

                double moneyToAdd = 0.0;
                for (ItemStack itemInInvetory : playerInventory.getContents()) {
                    if (itemInInvetory == null)
                        continue;
                    if (sellPrices.getPrices().containsKey(itemInInvetory.getType())) {
                        Double price = sellPrices.getPrices().get(itemInInvetory.getType());
                        moneyToAdd += itemInInvetory.getAmount() * price;
                        itemInInvetory.setAmount(0);
                    }
                }

                int sellBackpack = Main.getSqlManager().getSettings(player, "prison_sell_backpack");
                if (sellBackpack == 1) {
                    Inventory backpackInventory = null;
                    Backpack bp = Main.getMinepacksPlugin().getBackpackCachedOnly(player);
                    if (bp != null) {
                        backpackInventory = bp.getInventory();
                        for (ItemStack itemInBackpack : backpackInventory.getContents()) {
                            if (itemInBackpack == null)
                                continue;
                            if (sellPrices.getPrices().containsKey(itemInBackpack.getType())) {
                                Double price = sellPrices.getPrices().get(itemInBackpack.getType());
                                moneyToAdd += itemInBackpack.getAmount() * price;
                                itemInBackpack.setAmount(0);
                            }
                        }
                    }
                }

                if (moneyToAdd == 0.0) {
                    ChatInfo.warning(player, "Nemáš v inventáři žádný materiál, který by odpovídal tomuto shopu, takže jsi nic neprodal!");
                    return;
                }

                double moneyToAddWithoutEnhance = moneyToAdd;
                moneyToAdd = Main.getMultiplierManager().enhanceSellValue(player, moneyToAdd);

                String message = "§aProdal jsi materiál a bylo ti přidáno §e" + Utils.formatMoney(Math.round(moneyToAddWithoutEnhance)) + "§6$";
                if ((int) moneyToAdd != (int) moneyToAddWithoutEnhance)
                    message += "§a (§e+ " + Utils.formatMoney(Math.round(moneyToAdd - moneyToAddWithoutEnhance - 1)) + "§6$§a)!";
                else
                    message += "§a!";
                Main.getVaultEconomy().depositPlayer(player, moneyToAdd);
                ChatInfo.success(player, message);
                Double finalMoneyToAdd = moneyToAdd;
                Main.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        Bukkit.getPluginManager().callEvent(new PlayerSellallEvent(keeperPlayer, finalMoneyToAdd));
                    }
                });
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public SellPrices getSellPricesByRank(Rank rank) {
        for (SellPrices sellPrices : sellPricesList) {
            if (sellPrices.getRank().getWeight() == rank.getWeight())
                return sellPrices;
        }
        return null;
    }

    public SellPricesCustom getSellPricesByMineName(String mineName) {
        for (SellPricesCustom sellPricesCustom : sellPricesCustomsList) {
            if (sellPricesCustom.getMineName().equalsIgnoreCase(mineName))
                return sellPricesCustom;
        }
        return null;
    }

    public void reloadPrices() {
        Logger.info("Začínám s načítáním prodávajících hodnot materiálů...");
        long start = System.currentTimeMillis();
        sellPricesList.clear();
        sellPricesCustomsList.clear();

        Main.getConfigAPI().loadConfigs();
        reloadSellPrices();
        reloadCustomMinesPrices();

        Logger.success("Dokončeno načítání prodávajících hodnot materiálů! (Trvalo " + (System.currentTimeMillis() - start) + "ms)");
    }

    private void reloadCustomMinesPrices() {
        ConfigurationSection config = Main.getConfigAPI().getConfig("sellprices").getConfig().getConfigurationSection("customPrices");
        if (config == null) {
            Logger.debug("V sellprices.yml neexistuje 'customPrices'! Hodnoty nebudou načteny.");
            return;
        }

        List<String> mines = new ArrayList<>(config.getKeys(false));
        Logger.debug("Zjišťuji data " + mines.size() + " custom dolů...");
        for (String mine : mines) {
            ConfigurationSection section = config.getConfigurationSection(mine);
            if (section == null) {
                Logger.danger("Tato chyba by nikdy neměla nastat. MineName: " + mine + "; section == null (viz. SellPricesManager)");
                continue;
            }
            List<String> materials = new ArrayList<>(section.getKeys(false));
            Map<String, Double> materialsWithPrices = new HashMap<>();
            for (String material : materials) {
                Double price = section.getDouble(material);
                materialsWithPrices.put(material, price);
            }
            sellPricesCustomsList.add(new SellPricesCustom(mine, materialsWithPrices));
        }
    }

    private void reloadSellPrices() {
        ConfigurationSection config = Main.getConfigAPI().getConfig("sellprices").getConfig().getConfigurationSection("sellPrices");
        if (config == null) {
            Logger.debug("V sellprices.yml neexistuje 'sellPrices'! Hodnoty nebudou načteny.");
            return;
        }
        Logger.debug("Zjišťuji data do " + Rank.getLast().getWeight() + ". ranku...");
        List<String> availableRanksStrings = new ArrayList<>(config.getKeys(false));
        List<String> availableRanks = new ArrayList<>();
        for (String rankName : availableRanksStrings) {
            try {
                availableRanks.add(rankName);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.danger("V sellPrices.json se je nějaký rank označený stringem, místo číslem!");
            }
        }
        //int maxSavedRank = getMaxInt(availableRanks);
        for (String rankName : availableRanks) {
            ConfigurationSection section = config.getConfigurationSection(rankName);
            if (section == null) {
                Logger.danger("Nastala chyba, která by nikdy neměla nastat. RankName: " + rankName + "; section == null (viz. SellPricesManager)");
                continue;
            }
            List<String> materials = new ArrayList<>(section.getKeys(false));
            Map<String, Double> materialsWithPrices = new HashMap<>();
            for (String material : materials) {
                Double price = section.getDouble(material);
                materialsWithPrices.put(material, price);
            }
            sellPricesList.add(new SellPrices(rankName, materialsWithPrices));
        }
    }
}
