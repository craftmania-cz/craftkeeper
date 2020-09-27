package cz.craftmania.craftkeeper.utils;

import cz.craftmania.craftkeeper.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {

    private static final String pluginName = ChatColor.YELLOW + "[CraftRoleplay] ";

    public static void info(String s) {
        Bukkit.getConsoleSender().sendMessage(pluginName + ChatColor.WHITE + s);
    }

    public static void danger(String s) {
        Bukkit.getConsoleSender().sendMessage(pluginName + ChatColor.RED + s);
    }

    public static void success(String s) {
        Bukkit.getConsoleSender().sendMessage(pluginName + ChatColor.GREEN + s);
    }

    public static void debug(String s) {
        if (Main.getInstance().isDebug())
            Bukkit.getConsoleSender().sendMessage(pluginName + ChatColor.WHITE + s);
    }

    public static void debugSQL(String s) {
        if (Main.getInstance().isDebugSQL())
            Bukkit.getConsoleSender().sendMessage(pluginName + ChatColor.WHITE + s);
    }
}
