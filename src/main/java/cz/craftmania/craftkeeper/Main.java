package cz.craftmania.craftkeeper;

import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlugin;
import co.aikar.commands.PaperCommandManager;
import cz.craftmania.craftkeeper.commands.AutosellCommand;
import cz.craftmania.craftkeeper.commands.KeeperCommand;
import cz.craftmania.craftkeeper.commands.MultiplierCommand;
import cz.craftmania.craftkeeper.commands.SellallCommand;
import cz.craftmania.craftkeeper.extension.KeeperExtension;
import cz.craftmania.craftkeeper.listeners.PlayerListener;
import cz.craftmania.craftkeeper.managers.AutosellManager;
import cz.craftmania.craftkeeper.managers.KeeperManager;
import cz.craftmania.craftkeeper.managers.MultiplierManager;
import cz.craftmania.craftkeeper.managers.SellManager;
import cz.craftmania.craftkeeper.objects.KeeperPlayer;
import cz.craftmania.craftkeeper.sql.SQLManager;
import cz.craftmania.craftkeeper.utils.Logger;
import cz.craftmania.craftkeeper.utils.configs.Config;
import cz.craftmania.craftkeeper.utils.configs.ConfigAPI;
import cz.craftmania.craftlibs.sentry.CraftSentry;
import cz.wake.craftprison.api.PrisonAPI;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class Main extends JavaPlugin {

    // Instance
    private @Getter static Main instance;
    // ConfigAPI
    private @Getter static ConfigAPI configAPI;
    // CraftKeeper
    private @Getter static KeeperManager keeperManager;
    private @Getter static SellManager sellManager;
    private @Getter static AutosellManager autosellManager;
    private @Getter static MultiplierManager multiplierManager;
    // CraftPrison
    private @Getter static PrisonAPI prisonAPI;
    // Economy
    private @Getter static Economy vaultEconomy;
    // Luckyperms
    private @Getter static LuckPerms luckPermsAPI;
    // Minepacks API
    private @Getter static MinepacksPlugin minepacksPlugin;
    // SQL
    private @Getter static SQLManager sqlManager;
    // Commands
    private PaperCommandManager commandManager;
    // Sentry
    private CraftSentry sentry = null;
    // Debug
    private @Getter boolean debug = true;
    private @Getter boolean debugSQL = false;
    private @Getter boolean debugBlockBreak = false;

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
            new KeeperExtension().register();
        }

        // Managers
        Logger.info("Načítám managery!");
        keeperManager = new KeeperManager();
        sellManager = new SellManager();
        autosellManager = new AutosellManager();
        multiplierManager = new MultiplierManager();

        // Economy
        Logger.info("Probíhá načítání ekonomiky!");
        vaultEconomy = cz.craftmania.crafteconomy.Main.getVaultEconomy();

        // Prison
        Logger.info("Probíhá načítání PrisonAPI!");
        prisonAPI = new PrisonAPI();

        // Luckperms
        Logger.info("Získávám si LuckPerms API!");
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsAPI = provider.getProvider();
        }

        Logger.info("Získávám si Minepacks API!");
        Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin("Minepacks");
        if (bukkitPlugin instanceof MinepacksPlugin) { //TODO: Po airplane přidání nefunguje?
            minepacksPlugin = (MinepacksPlugin) bukkitPlugin;
        } else {
            Logger.danger("Minepacks není načteno!");
        }

        // PlaceholderAPI
        Logger.info("Registruji PlaceholderAPI CraftRoleplay extension!");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            //new RoleplayExtension().register();
        }

        // Bukkit events
        Logger.info("Probíhá registrace listenerů!");
        loadListeners();

        // Commands
        Logger.info("Probíhá registrace příkazů pomocí Aikar commands!");
        commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        loadCommands();

        Logger.info("Probíhá registrace runnablů!");
        loadRunnables();

        Logger.info("Probíhá načítání GLOBAL a EVENT Multiplierů z SQL!");
        Main.getMultiplierManager().loadGlobalMultipliers();

        Logger.info("Načítání dokončeno! (Zabralo to " + (System.currentTimeMillis() - start) + "ms)");
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void loadConfiguration() {
        Config sellPrices = new Config(Main.getConfigAPI(), "sellprices");
        Main.getConfigAPI().registerConfig(sellPrices);

        debug = getConfig().getBoolean("debug");
        debugSQL = getConfig().getBoolean("debugSQL");
        debugBlockBreak = getConfig().getBoolean("debugBlockBreak");
    }

    private void loadListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(), this);
        autosellManager.registerBlockBreakEvent();
    }

    private void loadCommands() {
        commandManager.registerCommand(new SellallCommand());
        commandManager.registerCommand(new AutosellCommand());
        commandManager.registerCommand(new MultiplierCommand());
        commandManager.registerCommand(new KeeperCommand());
    }

    private void loadRunnables() {
        // Autosell runnable
        long updateTime = getConfig().getLong("autosell.update-time");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (KeeperPlayer keeperPlayer : Main.getKeeperManager().getKeeperPlayers()) {
                    keeperPlayer.updateVaultBalance();
                }
            }
        }.runTaskTimerAsynchronously(this, 20L, (20 * updateTime) / 1000);
    }

    private void initDatabase() {
        sqlManager = new SQLManager(this);

        if (!sqlManager.tableMultiplierExists())
            sqlManager.createMultipliersTable();
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
