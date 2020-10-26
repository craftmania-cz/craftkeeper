package cz.craftmania.craftkeeper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.utils.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("autosell")
@Description("Umožní ti spravovat autosell")
public class AutosellCommand extends BaseCommand {

    @HelpCommand
    public void helpCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§e§lAutosell commands:");
        help.showHelp();
    }

    @Default
    public void changeAutosellStatus(CommandSender sender) {
        Player player = (Player) sender;
        KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
        keeperPlayer.setInAutoSellMode(!keeperPlayer.isInAutoSellMode());
        ChatInfo.info(player, "Autosell mode set to: " + keeperPlayer.isInAutoSellMode());
    }

    @Subcommand("debug")
    @CommandAlias("ad")
    public void debug(CommandSender sender) {
        Player player = (Player) sender;
        KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
        ChatInfo.info(player, "Autosell mode: " + keeperPlayer.isInAutoSellMode());
        ChatInfo.info(player, "Your to pay: " + keeperPlayer.getToPayFromAutosell());
        for (KeeperPlayer keeperPlayerInList : Main.getKeeperManager().getKeeperPlayers()) {
            if (keeperPlayerInList.isInAutoSellMode()) {
                Logger.debug("KeeperPlayer '" + keeperPlayerInList.getPlayer().getName() + "' is in autosell mode.");
            }
        }
    }
}
