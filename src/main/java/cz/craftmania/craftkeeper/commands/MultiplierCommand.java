package cz.craftmania.craftkeeper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.MultiplierAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("multiplier|mp")
@Description("Umožní management multiplierů")
public class MultiplierCommand extends BaseCommand {

    @HelpCommand
    public void helpCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§e§lAutosell commands:");
        help.showHelp();
    }

    @Default
    public void showCurrentMultiplierStatus(CommandSender sender) {
        // todo
    }

    @Subcommand("debug")
    @CommandAlias("mpd")
    public void debug(CommandSender sender) {
        List<Multiplier> multipliers = Main.getMultiplierManager().getMultipliers();
        for (Multiplier multiplier : multipliers) {
            Logger.info(multiplier.toString());
        }
    }

    @Subcommand("debug personal")
    @CommandAlias("mpdp")
    public void debugPersonal(CommandSender sender) {
        sender.sendMessage("Adding personal multiplier...");
        Player player = (Player) sender;
        MultiplierAPI.createNewPersonalMultiplier(player, 32525, 0.8);
    }

    @Subcommand("debug global")
    @CommandAlias("mpdg")
    public void debugGlobal(CommandSender sender, int precentage) {
        double boostBy = (double)precentage / (double)100;
        sender.sendMessage("Adding global multiplier..." + boostBy);
        Player player = (Player) sender;
        MultiplierAPI.createNewGlobalMultiplier(player, 32342, (double)precentage / (double)100);
    }
}
