package cz.craftmania.craftkeeper.utils;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.objects.Multiplier;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class MessageMaker {

    public static String end(Multiplier multiplier) {
        String message = "";

        switch (multiplier.getType()) {
            case EVENT:
                message += "§e§l[!!] §eEvent Multiplier s §a" + (multiplier.getPercentageBoost() * 100) + "%§e boostem právě skončil!";
                break;
            case GLOBAL:
                message += "§e§l[!!] §eGlobal Multiplier od hráče §a" + multiplier.getTarget() + " §es §a" + (multiplier.getPercentageBoost() * 100) + "%§e boostem právě skončil!";
                break;
            case PERSONAL:
                message += "§e§l[!!] §eTvůj Multiplier s §a" + (multiplier.getPercentageBoost() * 100) + "%§e boostem právě skončil!";
                break;
        }

        return message;
    }

    public static String start(Multiplier multiplier) {
        String message = "";

        switch (multiplier.getType()) {
            case EVENT:
                message += "§a§l[!!] §aEvent Multiplier s §e" + (multiplier.getPercentageBoost() * 100) + "%§a boostem právě začal! Skončí za §e" + multiplier.getRemainingTimeReadable() + "§a.";
                break;
            case GLOBAL:
                message += "§a§l[!!] §aGlobal Multiplier od hráče §e" + multiplier.getTarget() + " §as §e" + (multiplier.getPercentageBoost() * 100) + "%§a boostem právě začal! Skončí za §e" + multiplier.getRemainingTimeReadable() + "§a.";
                break;
            case PERSONAL:
                message += "§a§l[!!] §aTvůj Multiplier s §e" + (multiplier.getPercentageBoost() * 100) + "%§a boostem právě začal! Skončí za §e" + multiplier.getRemainingTimeReadable() + "§a.";
                break;
        }

        return message;
    }

    public static void announceEnd(Multiplier multiplier) {
        String message = end(multiplier);
        FileConfiguration config = Main.getInstance().getConfig();
        switch (multiplier.getType()) {
            case GLOBAL:
                if (!config.getBoolean("multipliers.global.message-on-end"))
                    return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(message);
                    player.sendTitle("§aSkončil Global Multiplier!", "§e" + Math.round(multiplier.getPercentageBoost() * 100) + "%§a Boost, Od hráče §e" + multiplier.getTarget() + "§a!", 10, 20*3, 10);

                }
                break;
            case EVENT:
                if (!config.getBoolean("multipliers.event.message-on-end"))
                    return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(message);
                    player.sendTitle("§aSkončil Event Multiplier!", "§e" + Math.round(multiplier.getPercentageBoost() * 100) + "%§a Boost", 10, 20*3, 10);
                }
                break;
            case PERSONAL:
                if (!config.getBoolean("multipliers.personal.message-on-end"))
                    return;
                Player player = Bukkit.getPlayer(multiplier.getTargetUUID());
                if (player != null) {
                    player.sendMessage(message);
                    player.sendTitle("§aSkončil ti Multiplier!", "§e" + Math.round(multiplier.getPercentageBoost() * 100) + "%§a Boost", 10, 20*3, 10);
                }
                break;
        }
    }

    public static void announceStart(Multiplier multiplier) {
        String message = start(multiplier);
        FileConfiguration config = Main.getInstance().getConfig();
        switch (multiplier.getType()) {
            case GLOBAL:
                if (!config.getBoolean("multipliers.global.message-on-start"))
                    return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(message);
                    player.sendTitle("§aZapočal Global Multiplier!", "§e" + Math.round(multiplier.getPercentageBoost() * 100) + "%§a Boost, Od hráče §e" + multiplier.getTarget() + "§a!", 10, 20*3, 10);
                }
                break;
            case EVENT:
                if (!config.getBoolean("multipliers.event.message-on-start"))
                    return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(message);
                    player.sendTitle("§aZapočal Event Multiplier!", "§e" + Math.round(multiplier.getPercentageBoost() * 100) + "%§a Boost", 10, 20*3, 10);
                }
                break;
            case PERSONAL:
                if (!config.getBoolean("multipliers.personal.message-on-start"))
                    return;
                Player player = Bukkit.getPlayer(multiplier.getTargetUUID());
                if (player != null) {
                    player.sendMessage(message);
                    player.sendTitle("§aZapočal ti Multiplier!", "§e" + Math.round(multiplier.getPercentageBoost() * 100) + "%§a Boost", 10, 20*3, 10);
                }
                break;
        }
    }
}
