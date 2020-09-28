package cz.craftmania.craftkeeper.utils.configs;

import cz.craftmania.craftkeeper.Main;
import cz.craftmania.craftkeeper.utils.Logger;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class ConfigAPI {

    private Plugin plugin;
    private Main api;

    private HashMap<String, Config> configs;

    private int autoSave = -1;

    public ConfigAPI(Main api) {
        this.api = api;
        this.plugin = Main.getInstance();
        this.configs = new HashMap<>();
    }

    public ConfigAPI(Main api, boolean enableAutoSave, int intervalInMin) {
        this(api);
        setAutoSave(enableAutoSave, intervalInMin);
    }

    public static void exportFile(Class reference, String resourcePath, String savePath) {
        InputStream ddlStream = reference.getClassLoader().getResourceAsStream(resourcePath);
        File target = new File(savePath);
        try {
            target.createNewFile();
            FileOutputStream fos = new FileOutputStream(savePath);
            byte[] buf = new byte[2048];
            int r;
            while ((r = ddlStream.read(buf)) != -1) {
                fos.write(buf, 0, r);
            }
        } catch (IOException e) {
            //EMPTY
        }
    }

    public void setAutoSave(boolean enabled, int intervalInMin) {
        if (autoSave == -1 && enabled) {
            runAutoSave(intervalInMin);
        } else if (!enabled) {
            stopAutoSave();
        } else {
            stopAutoSave();
            runAutoSave(intervalInMin);
        }
    }

    public void stopAutoSave() {
        if (plugin.getServer().getScheduler().isCurrentlyRunning(autoSave)) {
            plugin.getServer().getScheduler().cancelTask(autoSave);
        }
    }

    private void runAutoSave(int intervalInMin) {
        autoSave = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Config config : configs.values()) {
                config.reloadAuto();
            }
        }, 1200, intervalInMin * 60 * 20);
    }

    public void registerConfig(Config config) {
        configs.put(config.getName(), config);
        Logger.info("Config " + config.getName() + ".yml uspesne zaregistrovan!");
    }

    public Config getConfig(String name) {
        return configs.get(name);
    }

    /*
    This must be called onDisable().
    So that all configs are saved!
    It can be called from everywhere, but it's not useful.
     */
    public void saveConfigs() {
        for (Config config : configs.values()) {
            config.save();
        }
    }

    public void loadConfigs() {
        for (Config config : configs.values()) {
            config.load();
        }
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Main getApi() {
        return api;
    }
}
