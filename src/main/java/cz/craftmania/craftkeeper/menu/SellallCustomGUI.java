package cz.craftmania.craftkeeper.menu;

import cz.craftmania.craftcore.spigot.inventory.builder.ClickableItem;
import cz.craftmania.craftcore.spigot.inventory.builder.SmartInventory;
import cz.craftmania.craftcore.spigot.inventory.builder.content.InventoryContents;
import cz.craftmania.craftcore.spigot.inventory.builder.content.InventoryProvider;
import cz.craftmania.craftcore.spigot.inventory.builder.content.Pagination;
import cz.craftmania.craftcore.spigot.inventory.builder.content.SlotIterator;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.SellPrices;
import cz.craftmania.craftkeeper.objects.SellPricesCustom;
import cz.craftmania.craftkeeper.utils.Utils;
import cz.wake.craftprison.objects.Rank;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SellallCustomGUI implements InventoryProvider {

    private Pagination pagination;
    private SellPricesCustom sellPrices;

    public SellallCustomGUI(SellPricesCustom sellPrices) {
        this.sellPrices = sellPrices;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        pagination = contents.pagination();
        if (sellPrices == null) {
            return;
        }
        boolean hasPlayerMultipliers = Main.getMultiplierManager().getActiveMultipliersForPlayer(player).size() != 0;
        List<ClickableItem> items = new ArrayList<>();

        for (Map.Entry<Material, Double> entry : sellPrices.getPrices().entrySet()) {
            Material material = entry.getKey();
            double price = entry.getValue();
            double enhancedPrice = price;

            List<String> description = new ArrayList<>();
            description.add("§7Cena: §e" + Utils.formatMoney(price) + "§6$");

            if (hasPlayerMultipliers) {
                enhancedPrice = Main.getMultiplierManager().enhanceSellValue(player, enhancedPrice);
                description.add("§7Cena s MP: §e" + Utils.formatMoney(Math.round(enhancedPrice)) + "§6$");
            }

            ItemStack item = createItem(material, "§e" + Utils.processBlockName(material.name()), description);
            items.add(ClickableItem.empty(item));
        }

        ClickableItem[] itemsArray = new ClickableItem[items.size()];
        items.toArray(itemsArray);
        pagination.setItems(itemsArray);
        pagination.setItemsPerPage(36);

        openPage(player, contents);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private void openPage(Player player, InventoryContents contents) {
        makeBorders(contents);

        ItemStack currentMineItem = createItem(Material.ENCHANTED_BOOK, "§eMine " + sellPrices.getMineName(), Arrays.asList("§7" + sellPrices.getPrices().size() + " věcí k prodeji"));
        ItemStack closeInvItem = createItem(Material.RED_BED, "§cZavřít", null);

        contents.set(0, 4, ClickableItem.empty(currentMineItem));
        contents.set(5, 4, ClickableItem.of(closeInvItem, e -> {
            contents.inventory().close(player);
        }));

        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));
    }

    // Utils
    private ItemStack createItem(Material material, String itemName, List<String> itemLore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(itemName);
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public void makeBorders(InventoryContents contents) {
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int x = 0; x < contents.inventory().getColumns(); x++) {
            contents.set(0, x, ClickableItem.empty(filler));
            contents.set(5, x, ClickableItem.empty(filler));
        }
    }
}
