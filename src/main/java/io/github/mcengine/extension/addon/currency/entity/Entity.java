package io.github.mcengine.extension.addon.currency.entity;

import io.github.mcengine.api.currency.extension.addon.IMCEngineCurrencyAddOn;
import io.github.mcengine.api.core.MCEngineCoreApi;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.extension.addon.currency.entity.listener.EntityListener;
import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil;
import io.github.mcengine.extension.addon.currency.entity.util.EntityConfigUtil;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;

/**
 * Main class for the MCEngineEntity currency addon.
 * Registers entity listeners and loads entity reward configurations.
 */
public class Entity implements IMCEngineCurrencyAddOn {

    /** Configuration folder path for entity rewards. */
    private final String folderPath = "extensions/addons/configs/MCEngineEntity";

    /**
     * Called when the addon is loaded by the plugin.
     * Registers listeners, loads example files, and checks for updates.
     *
     * @param plugin The Bukkit plugin instance.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineExtensionLogger logger = new MCEngineExtensionLogger(plugin, "AddOn", "MCEngineEntity");

        // Create default config.yml
        EntityConfigUtil.createConfig(plugin, folderPath);

        // License check
        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String licenseType = config.getString("license", "free");

        if (!"free".equalsIgnoreCase(licenseType)) {
            logger.warning("License is not 'free'. Disabling Entity AddOn.");
            return;
        }

        try {
            // Create example config files
            EntityUtil.createSimpleFiles(plugin, folderPath);

            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new EntityListener(plugin, folderPath, logger), plugin);

        } catch (Exception e) {
            logger.warning("Failed to initialize Entity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the unique identifier for the Entity AddOn.
     *
     * @param id The identifier string.
     */
    @Override
    public void setId(String id) {
        MCEngineCoreApi.setId("mcengine-entity");
    }

    /**
     * Called when the AddOn is disabled or unloaded.
     *
     * @param plugin The Bukkit plugin instance.
     */
    @Override
    public void onDisload(Plugin plugin) {}
}
