package cz.craftmania.craftkeeper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import cz.craftmania.craftcore.spigot.inventory.builder.SmartInventory;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.events.PlayerSellallInventoryOpenEvent;
import cz.craftmania.craftkeeper.menu.SellallGUI;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.objects.Rank;
import cz.craftmania.craftkeeper.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("sellall")
@Description("Umožní ti prodávat bloky")
public class SellallCommand extends BaseCommand {

    @HelpCommand
    public void helpCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§e§lSellall commands:");
        help.showHelp();
    }

    @Default
    public void sellAll(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
            if (keeperPlayer == null) {
                ChatInfo.error(player, "Nastala chyba při získávání tvých dat. Prosím, odpoj se a připoj. Pokud tento problém bude přetrvávat, napiš nám na Disocrd -> #bugy_a_problemy");
                return;
            }
            keeperPlayer.refreshPlayerRank();
            Main.getSellManager().sellEverythingByRank(keeperPlayer, keeperPlayer.getPlayerRank());
        } else
            Logger.danger("Konzole nic neprodá.");
    }

    @Default
    @CommandCompletion("[Rank]")
    @Syntax("[Rank]")
    public void sellAllByRank(CommandSender sender, String rankName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
            if (keeperPlayer == null) {
                ChatInfo.error(player, "Nastala chyba při získávání tvých dat. Prosím, odpoj se a připoj. Pokud tento problém bude přetrvávat, napiš nám na Disocrd -> #bugy_a_problemy");
                return;
            }
            keeperPlayer.refreshPlayerRank();
            Rank rank = Rank.getByName(rankName);
            if (rank == null) {
                ChatInfo.error(player, "Zadal jsi neplatné jméno ranku!");
                return;
            }
            if (keeperPlayer.getPlayerRank().getWeight() < rank.getWeight()) {
                ChatInfo.error(player, "Nemáš dostatečný rank, aby jsi prodával s tímto rankem.");
                return;
            }
            Main.getSellManager().sellEverythingByRank(keeperPlayer, rank);
        } else
            Logger.danger("Konzole nic neprodá.");
    }

    @Subcommand("info")
    public void openSellallInventory(CommandSender sender) {
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

    @Subcommand("info")
    @CommandCompletion("[Rank]")
    @Syntax("[Rank]")
    public void openSellallInventoryByRank(CommandSender sender, String rankName) {
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
            Logger.danger("Konzole si neotevře inventář.");
    }

    @Subcommand("reload")
    @CommandAlias("sr")
    @CommandPermission("craftkeeper.reloadprices")
    public void reloadPrices(CommandSender sender) {
        if (sender instanceof Player) {
            ChatInfo.info((Player) sender, "Reloaduji ceny...");
        }
        Main.getSellManager().reloadPrices();
    }
}
