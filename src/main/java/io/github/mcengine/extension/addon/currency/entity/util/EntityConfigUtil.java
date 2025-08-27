package io.github.mcengine.extension.addon.currency.entity.util;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for creating the main configuration file for the Entity AddOn.
 */
public class EntityConfigUtil {

    /**
     * Creates the default config.yml for the Entity AddOn if it does not exist.
     *
     * @param plugin     The plugin instance.
     * @param folderPath The folder path relative to the plugin's data directory.
     * @param logger     Extension logger for reporting creation results.
     */
    public static void createConfig(Plugin plugin, String folderPath, MCEngineExtensionLogger logger) {
        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");

        if (configFile.exists()) return;

        File configDir = configFile.getParentFile();
        if (!configDir.exists() && !configDir.mkdirs()) {
            logger.warning("Failed to create entity config directory: " + configDir.getAbsolutePath());
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        config.options().header("Configuration file for MCEngineEntity AddOn");

        config.set("license", "free");

        try {
            config.save(configFile);
            logger.info("Created default Entity config: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            logger.warning("Failed to save Entity config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
