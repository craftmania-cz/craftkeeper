package cz.craftmania.craftkeeper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import cz.craftmania.craftcore.spigot.messages.chat.ChatInfo;
import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftlibs.utils.TextComponentBuilder;
import cz.craftmania.craftlibs.utils.actions.ConfirmAction;
import cz.craftmania.craftlibs.utils.actions.IdentifierNotSetException;
import cz.craftmania.craftlibs.utils.actions.PlayerNotSetException;
import net.md_5.bungee.api.chat.TextComponent;
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

        if (!player.hasPermission(Main.getInstance().getConfig().getString("autosell.permission-node"))) {
            ChatInfo.error(player, "Na Autosell nemáš permisse!");
            return;
        }

        KeeperPlayer keeperPlayer = Main.getKeeperManager().getKeeperPlayer(player);

        if (!keeperPlayer.canHaveAutosell()) {
            long cooldownDuration = keeperPlayer.getAutosellCooldownTo();
            if (cooldownDuration != 0) {
                // Oznámí že má cooldown
                ChatInfo.error(player, "Autosell si můžeš zapnout až za §e" + keeperPlayer.getRemainingCooldownInMS() / 1000 + " §csekund!");
                return;
            } else {
                // Vypne autosell
                try {
                    ConfirmAction.Action action = new ConfirmAction.Builder()
                            .setPlayer(player)
                            .generateIdentifier()
                            .setDelay(10L)
                            .addComponent(a -> new TextComponentBuilder("§a§l>>§r§a Autosell ti vyprší za §e" + keeperPlayer.getRemainingDurationInMS() / 1000 + " §asekund!").getComponent())
                            .addComponent(a -> new TextComponentBuilder("§6§l[!]§r§6 Pokud chceš vypnout Autosell, klikni na tuto zprávu!")
                                    .setPerformedCommand(a.getConfirmationCommand())
                                    .setTooltip("§eKlikni zde pro vypnutí")
                                    .getComponent())
                            .setRunnable(a -> {
                                keeperPlayer.disableAutosellMode();
                                ChatInfo.info(player, "Vypnul sis Autosell!");
                            })
                            .setExpireRunnable(a -> {}).build();
                    action.sendTextComponents();
                } catch (PlayerNotSetException | IdentifierNotSetException e) {
                    e.printStackTrace();
                    Main.getInstance().sendSentryException(e);
                    Logger.danger("Nastala chyba při dotazování hráče o vypnutí autosellu!");
                    ChatInfo.error(player, "Nastala chyba při pdotazování o vypnutí Autosellu. Prosím, nahlaš tuto chybu na našem discordu v channelu #bugy_a_problemy!");
                }
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
}
