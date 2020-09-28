package cz.craftmania.craftkeeper.utils;

import cz.craftmania.craftkeeper.Main;
import org.bukkit.scheduler.BukkitRunnable;

public class ProtectedAsync {

    public static void runAsync(Runnable e) {
        try {
            new BukkitRunnable() {
                @Override
                public void run() {
                    e.run();
                }
            }.runTaskAsynchronously(Main.getInstance());
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.danger("Nastala chyba v ProtectedAsync! Task se provede v Main threadu. (Creating async task)");
            try {
                e.run();
            } catch (Exception exception2) {
                exception2.printStackTrace();
                Main.getInstance().sendSentryException(exception2);
                Logger.danger("Nastala chyba v ProtectedAsync! (Runnable exception)");
            }
        }
    }
}
