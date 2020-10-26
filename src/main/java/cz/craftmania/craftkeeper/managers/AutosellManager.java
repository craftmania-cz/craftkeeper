package cz.craftmania.craftkeeper.managers;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.listeners.blockListeners.*;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.utils.Logger;
import net.md_5.bungee.api.chat.ItemTag;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AutosellManager {

    public AutosellManager() {
    }

    // Handler

    public void handleBlockBreakEvent(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
            if (keeperPlayer != null) {
                if (keeperPlayer.isInAutoSellMode()) {
                    boolean isInDisabledWorld = Main.getInstance().getConfig().getStringList("autosell.disabled-worlds").contains(player.getWorld().getName());
                    if (!isInDisabledWorld) {
                        if (player.getGameMode().equals(GameMode.CREATIVE)) {
                            boolean autosellAllowedInCreative = Main.getInstance().getConfig().getBoolean("autosell.allow-in-creative");
                            if (!autosellAllowedInCreative)
                                return;
                        }

                        Block block = event.getBlock();
                        List<ItemStack> blockDrops = new ArrayList<>(block.getDrops(player.getInventory().getItemInMainHand()));

                        Logger.debugBlockBreak("-- -- -- --");
                        Logger.debugBlockBreak("Hráč '" + player.getName() + "' zničil block '" + block.getType().toString() + "'");
                        Logger.debugBlockBreak("Block dropy jsou:");
                        for (ItemStack itemStack : blockDrops) {
                            Logger.debugBlockBreak(" - Name: '" + itemStack.getType().toString() + "'; Amount: '" + itemStack.getAmount() + "'");
                        }
                        Logger.debugBlockBreak("Exp to drop: " + event.getExpToDrop());

                        double price = 0.0;
                        for (ItemStack drop : blockDrops) {
                            price += Main.getSellManager().getPriceOfItemstackByRank(drop, keeperPlayer.getPlayerRank());
                        }

                        Logger.debugBlockBreak("Before: " + keeperPlayer.getToPayFromAutosell());
                        keeperPlayer.addToPay(price);
                        Main.getKeeperManager().updateKeeperPlayer(keeperPlayer);
                        Logger.debugBlockBreak("After: " + Main.getKeeperManager().getKeeperPlayer(player).getToPayFromAutosell());

                        event.setCancelled(true);
                        block.setType(Material.AIR);

                        if (Main.getInstance().getConfig().getBoolean("autosell.exp-to-player")) {
                            player.giveExp(event.getExpToDrop());
                        }

                        if (Main.getInstance().getConfig().getBoolean("autosell.do-damage-to-tool")) {
                            ItemStack tool = player.getInventory().getItemInMainHand();
                            if (EnchantmentTarget.TOOL.includes(tool)) {
                                if (tool.containsEnchantment(Enchantment.DURABILITY)) {
                                    int enchantLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);
                                    if (doDamage(enchantLevel)) {
                                        tool.setDurability((short)(tool.getDurability() + 1));
                                    }
                                } else {
                                    tool.setDurability((short)(tool.getDurability() + 1));
                                }
                                if (tool.getDurability() >= tool.getType().getMaxDurability()) {
                                    player.getInventory().getItemInMainHand().setAmount(0);
                                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 10f, 1f);
                                }
                                Logger.debug("Player is holding a tool.");
                            }
                            player.getInventory().setItemInMainHand(tool);
                        }
                        Logger.debugBlockBreak("Done");
                    }
                }
            }
        }
    }

    // Block events

    public void registerBlockBreakEvent() {
        PluginManager pm = Main.getInstance().getServer().getPluginManager();

        FileConfiguration config = Main.getInstance().getConfig();
        String blockBreakEventLevel = config.getString("block-break-event-level");

        if (blockBreakEventLevel == null) {
            Logger.danger("Pozor! 'block-break-event-level' v configu není nastaven! Používám defaultní nastavení: NORMAL");
            blockBreakEventLevel = "NORMAL";
        }

        switch (blockBreakEventLevel.toLowerCase()) {
            case "monitor": {
                pm.registerEvents(new BlockListenerMonitor(), Main.getInstance());
                break;
            }
            case "highest": {
                pm.registerEvents(new BlockListenerHighest(), Main.getInstance());
                break;
            }
            case "high": {
                pm.registerEvents(new BlockListenerHigh(), Main.getInstance());
                break;
            }
            case "normal": {
                pm.registerEvents(new BlockListenerNormal(), Main.getInstance());
                break;
            }
            case "low": {
                pm.registerEvents(new BlockListenerLow(), Main.getInstance());
                break;
            }
            case "lowest": {
                pm.registerEvents(new BlockListenerLowest(), Main.getInstance());
                break;
            }
            default:
                Logger.danger("Pozor! Byl špatně nastaven 'block-break-event-level'! Momentální hodnota: " + blockBreakEventLevel + "; Používám defaultní nastavení: NORMAL");
                blockBreakEventLevel = "normal";
                pm.registerEvents(new BlockListenerNormal(), Main.getInstance());
        }
        Logger.info("Nastaven listener na block break eventu na úroveň '" + blockBreakEventLevel + "'!");
    }

    public boolean doDamage(int var1) {
        int var2 = new Random().nextInt(var1);
        return var2 == 0;
    }
}