package cz.craftmania.craftkeeper.menu;

import cz.craftmania.craftcore.spigot.inventory.builder.ClickableItem;
import cz.craftmania.craftcore.spigot.inventory.builder.SmartInventory;
import cz.craftmania.craftcore.spigot.inventory.builder.content.InventoryContents;
import cz.craftmania.craftcore.spigot.inventory.builder.content.InventoryProvider;
import cz.craftmania.craftcore.spigot.inventory.builder.content.Pagination;
import cz.craftmania.craftcore.spigot.inventory.builder.content.SlotIterator;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.objects.SellPrices;
import cz.craftmania.craftkeeper.utils.Utils;
import cz.wake.craftprison.objects.Rank;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SellallGUI implements InventoryProvider {

    private Pagination pagination;
    private Rank rank;

    public SellallGUI(Rank rank) {
        this.rank = rank;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        pagination = contents.pagination();
        SellPrices sellPrices = Main.getSellManager().getSellPricesByRank(rank);
        if (sellPrices == null) {
            ChatInfo.error(player, "Nastala chyba při získávání cen z Ranku " + rank.getName() + "! Prosím, nahlaš tuto chybu na discordu.");
            return;
        }
        List<ClickableItem> items = new ArrayList<>();

        for (Map.Entry<Material, Double> entry : sellPrices.getPrices().entrySet()) {
            Material material = entry.getKey();
            double price = entry.getValue();

            ItemStack item = createItem(material, "§e" + Utils.processBlockName(material.name()), Collections.singletonList("§7Cena: §e" + price + "§6$"));
            items.add(ClickableItem.empty(item));
        }

        ClickableItem[] itemsArray = new ClickableItem[items.size()];
        items.toArray(itemsArray);
        pagination.setItems(itemsArray);
        pagination.setItemsPerPage(36);

        openPage(player, contents, rank);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private void openPage(Player player, InventoryContents contents, Rank rank) {
        makeBorders(contents);

        Rank previousRank = Rank.getByWeight(rank.getWeight() - 1);
        Rank nextRank = Rank.getByWeight(rank.getWeight() + 1);

        boolean isPreviousRank = Main.getSellManager().getSellPricesByRank(previousRank) != null;
        boolean isNextRank = Main.getSellManager().getSellPricesByRank(nextRank) != null;

        ItemStack previousRankItem, nextRankItem;
        ItemStack previousPage = createItem(Material.ARROW, "§ePředchozí strana", null);
        ItemStack nextPage = createItem(Material.ARROW, "§eDalší strana", null);
        ItemStack currentRankItem = createItem(Material.ENCHANTED_BOOK, "§eRank " + rank.getName(), Arrays.asList("§7" + Main.getSellManager().getSellPricesByRank(rank).getPrices().size() + " věcí k prodeji"));
        ItemStack closeInvItem = createItem(Material.RED_BED, "§cZavřít", null);

        if (isPreviousRank)
            previousRankItem = createItem(Material.SPECTRAL_ARROW, "§ePředchozí rank", Arrays.asList("§7Rank " + previousRank.getName()));
        else
            previousRankItem = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);

        if (isNextRank)
            nextRankItem = createItem(Material.SPECTRAL_ARROW, "§eDalší rank", Arrays.asList("§7Rank " + nextRank.getName()));
        else
            nextRankItem = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);

        contents.set(0, 4, ClickableItem.empty(currentRankItem));
        if (!pagination.isFirst()) {
            contents.set(5, 3, ClickableItem.of(previousPage, e -> {
                contents.inventory().open(player, pagination.previous().getPage());
            }));
        }
        if (!pagination.isLast()) {
            contents.set(5, 5, ClickableItem.of(nextPage, e -> {
                contents.inventory().open(player, pagination.next().getPage());
            }));
        }
        contents.set(5, 2, ClickableItem.of(previousRankItem, e -> {
            if (Main.getSellManager().getSellPricesByRank(previousRank) == null)
                return;
            SmartInventory.builder().size(6, 9).title("Výkupní seznam - Rank " + previousRank.getName()).provider(new SellallGUI(previousRank)).build().open(player);
        }));
        contents.set(5, 6, ClickableItem.of(nextRankItem, e -> {
            if (Main.getSellManager().getSellPricesByRank(nextRank) == null)
                return;
            SmartInventory.builder().size(6, 9).title("Výkupní seznam - Rank " + nextRank.getName()).provider(new SellallGUI(nextRank)).build().open(player);
        }));
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
        for (int x=0; x<contents.inventory().getColumns(); x++) {
            contents.set(0, x, ClickableItem.empty(filler));
            contents.set(5, x, ClickableItem.empty(filler));
        }
    }
}
