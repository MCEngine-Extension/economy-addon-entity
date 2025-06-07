package io.github.mcengine.addon.currency.entity;

import io.github.mcengine.api.currency.addon.IMCEngineCurrencyAddOn;
import io.github.mcengine.api.mcengine.MCEngineApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import io.github.mcengine.addon.currency.entity.listener.EntityListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Entity implements IMCEngineCurrencyAddOn {

    /**
     * Invoked by the core plugin to initialize this AddOn.
     *
     * @param plugin The plugin instance used for context and logging.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineEntity");
        try {
            // Register listener
            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new EntityListener(plugin), plugin);
        } catch (Exception e) {
            logger.warning("Failed to initialize Entity: " + e.getMessage());
            e.printStackTrace();
        }

        MCEngineApi.checkUpdate(plugin, "github", "MCEngine-AddOn", "currency-entity", plugin.getConfig().getString("github.token", "null"));
    }
}
