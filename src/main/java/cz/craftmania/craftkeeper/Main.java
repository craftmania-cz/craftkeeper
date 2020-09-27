package cz.craftmania.craftkeeper;

import cz.craftmania.craftkeeper.sql.SQLManager;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.configs.ConfigAPI;
import cz.craftmania.craftlibs.sentry.CraftSentry;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    // Instance
    private @Getter static Main instance;

    // ConfigAPI
    private @Getter static ConfigAPI configAPI;

    // Debug
    private @Getter boolean debug = false;
    private @Getter boolean debugSQL = false;

    // SQL
    private @Getter SQLManager sqlManager;

    // Sentry
    private CraftSentry sentry = null;

    @Override
    public void onEnable() {
        instance = this;
        long start = System.currentTimeMillis();
        Logger.info("Naačítání pluginu CraftKeeper!");

        // Config
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        configAPI = new ConfigAPI(this);
        loadConfiguration();

        // Sentry integration
        if (!(Objects.requireNonNull(getConfig().getString("sentry-dsn")).length() == 0) && Bukkit.getPluginManager().isPluginEnabled("CraftLibs")) {
            String dsn = getConfig().getString("sentry-dsn");
            Logger.info("Sentry integration je aktivní: §7" + dsn);
            sentry = new CraftSentry(dsn);
        } else {
            Logger.danger("Sentry integration neni aktivovana!");
        }

        // HikariCP - SQL
        initDatabase();

        // PlaceholderAPI
        Logger.info("Registruji PlaceholderAPI CraftRoleplay extension!");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            //new RoleplayExtension().register();
        }

        // Bukkit events
        Logger.info("Probíhá registrace eventů!");
        loadEvents();

        // Commands
        Logger.info("Probíhá registrace příkazů pomocí Aikar commands!");
        loadCommands();

        Logger.info("Načítání dokončeno! (Zabralo to " + (System.currentTimeMillis() - start) + "ms)");
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void loadConfiguration() {

    }

    private void loadEvents() {
        PluginManager pm = getServer().getPluginManager();

    }

    private void loadCommands() {

    }

    private void initDatabase() {
        sqlManager = new SQLManager(this);
    }

    /**
     * Odesilá exception na Sentry
     */
    public void sendSentryException(Exception exception) {
        if (sentry == null) {
            Logger.danger("Sentry neni aktivovany, error nebude zaslan!");
            return;
        }
        sentry.sendException(exception);
    }
}
