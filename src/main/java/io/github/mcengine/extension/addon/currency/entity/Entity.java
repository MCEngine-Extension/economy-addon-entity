package io.github.mcengine.extension.addon.currency.entity;

import io.github.mcengine.api.currency.extension.addon.IMCEngineCurrencyAddOn;
import io.github.mcengine.api.core.MCEngineCoreApi;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.extension.addon.currency.entity.listener.EntityListener;
import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Main class for the MCEngineEntity currency addon.
 * Registers entity listeners and loads entity reward configurations.
 */
public class Entity implements IMCEngineCurrencyAddOn {

    /**
     * Called when the addon is loaded by the plugin.
     * Registers listeners, loads example files, and checks for updates.
     *
     * @param plugin The Bukkit plugin instance.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineExtensionLogger logger = new MCEngineExtensionLogger(plugin, "AddOn", "MCEngineEntity");
        String folderPath = "extensions/addons/configs/MCEngineEntity";
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

    @Override
    public void setId(String id) {
        MCEngineCoreApi.setId("mcengine-entity");
    }

    @Override
    public void onDisload(Plugin plugin) {}
}
