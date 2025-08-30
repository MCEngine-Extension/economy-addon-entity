package io.github.mcengine.extension.addon.economy.entity;

import io.github.mcengine.api.economy.extension.addon.IMCEngineEconomyAddOn;
import io.github.mcengine.api.core.MCEngineCoreApi;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.common.economy.MCEngineEconomyCommon;
import io.github.mcengine.extension.addon.economy.entity.listener.EntityListener;
import io.github.mcengine.extension.addon.economy.entity.util.EntityUtil;
import io.github.mcengine.extension.addon.economy.entity.util.EntityConfigUtil;
import io.github.mcengine.extension.addon.economy.entity.database.EntityDB;
import io.github.mcengine.extension.addon.economy.entity.database.sqlite.EntityDBSQLite;
import io.github.mcengine.extension.addon.economy.entity.database.mysql.EntityDBMySQL;
import io.github.mcengine.extension.addon.economy.entity.database.postgresql.EntityDBPostgreSQL;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.sql.Connection;

/**
 * Main class for the MCEngineEntity Economy AddOn.
 * Registers entity listeners and loads entity reward configurations.
 */
public class Entity implements IMCEngineEconomyAddOn {

    /**
     * Configuration folder path for entity rewards.
     */
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
        EntityConfigUtil.createConfig(plugin, folderPath, logger);

        // License check
        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String licenseType = config.getString("license", "free");

        if (!"free".equalsIgnoreCase(licenseType)) {
            logger.warning("License is not 'free'. Disabling Entity AddOn.");
            return;
        }

        try {
            // Ensure DB schema (dialect-specific) for optional entity logging
            Connection conn = MCEngineEconomyCommon.getApi().getDBConnection();
            String dbType;
            try {
                dbType = plugin.getConfig().getString("database.type", "sqlite");
            } catch (Throwable t) {
                dbType = "sqlite";
            }

            EntityDB entityDB;
            switch (dbType == null ? "sqlite" : dbType.toLowerCase()) {
                case "mysql" -> entityDB = new EntityDBMySQL(conn, logger);
                case "postgresql", "postgres" -> entityDB = new EntityDBPostgreSQL(conn, logger);
                case "sqlite" -> entityDB = new EntityDBSQLite(conn, logger);
                default -> {
                    logger.warning("Unknown database.type='" + dbType + "', defaulting to SQLite for Entity.");
                    entityDB = new EntityDBSQLite(conn, logger);
                }
            }
            entityDB.ensureSchema();

            // Create example config files
            EntityUtil.createSimpleFiles(plugin, folderPath, logger);

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
