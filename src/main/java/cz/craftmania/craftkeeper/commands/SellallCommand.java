package cz.craftmania.craftkeeper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.objects.SellPricesCustom;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.wake.craftprison.objects.Rank;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("sellall|sa")
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

    @Subcommand("mine")
    @CommandAlias("sam")
    @CommandCompletion("[Mine]")
    public void sellAllCustomMine(CommandSender sender, String mineName) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            SellPricesCustom sellPrices = Main.getSellManager().getSellPricesByMineName(mineName);

            if (sellPrices == null) {
                ChatInfo.error(player, "Zadal jsi neplatné jméno dolu!");
                return;
            }

            if (!player.hasPermission("craftkeeper.mine." + mineName)) {
                ChatInfo.error(player, "Nemáš právo prodávat na tomto dole!");
                return;
            }

            KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
            if (keeperPlayer == null) {
                ChatInfo.error(player, "Nastala chyba při získávání tvých dat. Prosím, odpoj se a připoj. Pokud tento problém bude přetrvávat, napiš nám na Disocrd -> #bugy_a_problemy");
                return;
            }
            keeperPlayer.refreshPlayerRank();

            if (keeperPlayer.getPlayerRank() != Rank.Z) {
                ChatInfo.error(player, "Nemůžeš zde prodávat, nemáš rank Z!");
                return;
            }

            Main.getSellManager().sellEverythingByMineName(keeperPlayer, mineName);
        } else
            Logger.danger("Konzole nic neprodá.");
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
