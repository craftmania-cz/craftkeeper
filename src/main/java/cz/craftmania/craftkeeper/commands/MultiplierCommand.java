package cz.craftmania.craftkeeper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import cz.craftmania.craftkeeper.objects.MultiplierType;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.MultiplierAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("multiplier|mpk|mp")
@Description("Umožní management multiplierů")
public class MultiplierCommand extends BaseCommand {

    @HelpCommand
    public void helpCommand(CommandSender sender, CommandHelp help) {
        sender.sendMessage("§e§lMultiplier commands:");
        help.showHelp();
    }

    @Default
    public void showCurrentMultiplierStatus(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<Multiplier> multiplierList = Main.getMultiplierManager().getActiveMultipliersForPlayer(player);
            if (multiplierList.size() == 0) {
                ChatInfo.error(player, "Nemáš žádné aktivní Multipliery!");
                return;
            }
            player.sendMessage("\n§e§lAktivní Multipliery§e:");
            double pb = 0.0;
            for (Multiplier multiplier : multiplierList) {
                player.sendMessage(" §8- §e" + multiplier.getType().translate() + " §7- §eBoost: §a" + (multiplier.getPercentageBoost() * 100) + "% §7- §eSkončí za: §a" + multiplier.getRemainingTimeReadable());
                pb += multiplier.getPercentageBoost();
            }
            if (multiplierList.size() > 1) {
                player.sendMessage("§eDohromady se tvoje prodejní ceny zvýší o §a" + (pb * 100) + "%§e!");
            }
        }
    }

    @CommandPermission("craftkeeper.multiplier.create")
    @Subcommand("create")
    public void createMultiplier(CommandSender sender, String nick, String type, long lengthMS, double percent) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            ChatInfo.error(p, "Ale ale... Co to zkoušíš? Tento příkaz je jen pro konzoli.");
            return;
        }
        Player player = Bukkit.getPlayer(nick);
        double percentageBoost = (double) percent / 100;

        MultiplierType multiplierType = MultiplierType.getByName(type);
        if (multiplierType == null) {
            Logger.danger("[!!!!!] Něco využilo příkaz /multiplier create a použilo to špatný typ multiplieru ('" + type + "')!");
            return;
        }
        if (player == null && multiplierType == MultiplierType.PERSONAL) {
            Logger.danger("[!!!!!] Player objekt hráče '" + nick + "' je z nějakého důvodu null. Chudák, nedostane Multiplier. Každopádně tato chyba by nikdy neměla nastat.");
            return;
        }
        switch (multiplierType) {
            case EVENT:
                MultiplierAPI.createNewEventMultiplier(lengthMS, percentageBoost);
                break;
            case GLOBAL:
                MultiplierAPI.createNewGlobalMultiplier(nick, lengthMS, percentageBoost);
                break;
            case PERSONAL:
                MultiplierAPI.createNewPersonalMultiplier(player, lengthMS, percentageBoost);
                break;
        }
    }
}
