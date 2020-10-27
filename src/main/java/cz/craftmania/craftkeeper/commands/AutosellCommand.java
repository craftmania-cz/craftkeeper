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

        if (!keeperPlayer.canHaveAutosell()) {
            long cooldownDuration = keeperPlayer.getAutosellCooldownTo();
            if (cooldownDuration != 0) {
                // Oznámí že má cooldown
                ChatInfo.error(player, "Autosell si můžeš zapnout až za §e" + keeperPlayer.getRemainingCooldownInMS() / 1000 + " §csekund!");
                return;
            } else {
                // Vypne autosell
                keeperPlayer.disableAutosellMode();
                ChatInfo.info(player, "Vypnul sis Autosell!");
                return;
            }
        }
        keeperPlayer.enableAutosellMode();

        String message = "Zapnul sis Autosell!";
        if (!Main.getInstance().getConfig().getBoolean("autosell.duration.infinite")) {
            long duration = Main.getInstance().getConfig().getLong("autosell.duration.time") / 1000;
            message += " Autosell bude aktivní §e" + duration + " §asekund!";
        }
        ChatInfo.success(player, message);
    }

    @Subcommand("debug")
    @CommandAlias("ad")
    public void debug(CommandSender sender) {
        Player player = (Player) sender;
        KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);
        ChatInfo.info(player, "Autosell mode: " + keeperPlayer.isInAutoSellMode());
        ChatInfo.info(player, "Your to pay: " + keeperPlayer.getToPayFromAutosell());
        ChatInfo.info(player, "Remaining duration: " + keeperPlayer.getRemainingDurationInMS());
        ChatInfo.info(player, "Remaining cooldown: " + keeperPlayer.getRemainingCooldownInMS());
        ChatInfo.info(player, "Can have autosell: " + keeperPlayer.canHaveAutosell());
        for (KeeperPlayer keeperPlayerInList : Main.getKeeperManager().getKeeperPlayers()) {
            if (keeperPlayerInList.isInAutoSellMode()) {
                Logger.debug("KeeperPlayer '" + keeperPlayerInList.getPlayer().getName() + "' is in autosell mode.");
            }
        }
    }
}
