package cz.craftmania.craftkeeper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import cz.craftmania.craftcore.spigot.inventory.builder.SmartInventory;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.events.PlayerSellallInventoryOpenEvent;
import cz.craftmania.craftkeeper.menu.SellallCustomGUI;
import cz.craftmania.craftkeeper.menu.SellallGUI;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.objects.SellPricesCustom;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.wake.craftprison.objects.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("craftkeeper|ck|keeper")
@Description("Umožní ti otevřít tržbní informace o daném dolu")
public class KeeperCommand extends BaseCommand {

    @HelpCommand
    public void helpCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§e§lKeeper commands:");
        help.showHelp();
    }

    @Subcommand("mine")
    public void showInventoryByPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
            if (keeperPlayer == null) {
                ChatInfo.error(player, "Nastala chyba při získávání tvých dat. Prosím, odpoj se a připoj. Pokud tento problém bude přetrvávat, napiš nám na Disocrd -> #bugy_a_problemy");
                return;
            }
            keeperPlayer.refreshPlayerRank();
            Rank rank = keeperPlayer.getPlayerRank();

            if (rank == null) {
                ChatInfo.error(player, "Chyba při získávání tvého ranku. Zkus se odpojit a připojit!");
                return;
            }

            SmartInventory.builder().size(6, 9).title("Výkupní seznam - Rank " + rank.getName()).provider(new SellallGUI(rank)).build().open(player);
            Bukkit.getPluginManager().callEvent(new PlayerSellallInventoryOpenEvent(keeperPlayer));
        } else
            Logger.danger("Konzole si neotevře inventář.");
    }

    @Subcommand("mine")
    @Syntax("[rank]")
    public void showInventoryByRankName(CommandSender sender, String rankName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
            if (keeperPlayer == null) {
                ChatInfo.error(player, "Nastala chyba při získávání tvých dat. Prosím, odpoj se a připoj. Pokud tento problém bude přetrvávat, napiš nám na Disocrd -> #bugy_a_problemy");
                return;
            }
            Rank rank = Rank.getByName(rankName);
            if (rank == null) {
                ChatInfo.error(player, "Zadal jsi neplatné jméno ranku!");
                return;
            }
            SmartInventory.builder().size(6, 9).title("Výkupní seznam - Rank " + rank.getName()).provider(new SellallGUI(rank)).build().open(player);
            Bukkit.getPluginManager().callEvent(new PlayerSellallInventoryOpenEvent(keeperPlayer));

        } else
            Logger.danger("Konzole si inventář neotevře.");
    }

    @Subcommand("mine custom")
    @CommandAlias("keepercustom")
    @Syntax("[mine]")
    public void showInventoryByMineName(CommandSender sender, String mineName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
            keeperPlayer.refreshPlayerRank();

            SellPricesCustom sellPricesCustom = Main.getSellManager().getSellPricesByMineName(mineName);
            if (sellPricesCustom == null) {
                ChatInfo.error(player, "Zadal jsi neplatné jméno dolu!");
                return;
            }

            SmartInventory.builder().size(6, 9).title("Výkupní seznam - Mine " + sellPricesCustom.getMineName()).provider(new SellallCustomGUI(sellPricesCustom)).build().open(player);
            Bukkit.getPluginManager().callEvent(new PlayerSellallInventoryOpenEvent(keeperPlayer));
        } else
            Logger.danger("Konzole si inventář neotevře.");
    }
}
