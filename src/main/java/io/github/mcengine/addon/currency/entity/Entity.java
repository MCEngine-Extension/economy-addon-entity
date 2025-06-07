package io.github.mcengine.addon.currency.entity;

import io.github.mcengine.api.currency.addon.IMCEngineCurrencyAddOn;
import io.github.mcengine.api.mcengine.MCEngineApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import io.github.mcengine.addon.currency.entity.listener.EntityListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
            pluginManager.registerEvents(new EntityListener(plugin, logger), plugin);

            // Create example config files
            createSimpleFiles(plugin);

        } catch (Exception e) {
            logger.warning("Failed to initialize Entity: " + e.getMessage());
            e.printStackTrace();
        }

        MCEngineApi.checkUpdate(plugin, "github", "MCEngine-AddOn", "currency-entity", plugin.getConfig().getString("github.token", "null"));
    }

    /**
     * Creates example zombie.yml and skeleton.yml files if they don't exist.
     */
    private void createSimpleFiles(Plugin plugin) {
        File addonDir = new File(plugin.getDataFolder().getParentFile(), "MCEngineCurrency/addons/MCEngineEntity");

        if (!addonDir.exists() && !addonDir.mkdirs()) {
            plugin.getLogger().warning("Failed to create MCEngineEntity config folder.");
            return;
        }

        File zombieFile = new File(addonDir, "zombie.yml");
        File skeletonFile = new File(addonDir, "skeleton.yml");

        if (!zombieFile.exists()) {
            try (FileWriter writer = new FileWriter(zombieFile)) {
                writer.write("entity: ZOMBIE\n");
                writer.write("coinType: coin\n");
                writer.write("amount: 100~200\n");
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create zombie.yml: " + e.getMessage());
            }
        }

        if (!skeletonFile.exists()) {
            try (FileWriter writer = new FileWriter(skeletonFile)) {
                writer.write("entity: SKELETON\n");
                writer.write("coinType: copper\n");
                writer.write("amount: 50\n");
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create skeleton.yml: " + e.getMessage());
            }
        }
    }
}
