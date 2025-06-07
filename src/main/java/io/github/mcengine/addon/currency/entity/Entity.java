package io.github.mcengine.addon.currency.entity;

import io.github.mcengine.api.currency.addon.IMCEngineCurrencyAddOn;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;

import org.bukkit.plugin.Plugin;

public class Entity implements IMCEngineCurrencyAddOn {

    /**
     * Invoked by the core plugin to initialize this AddOn.
     *
     * @param plugin The plugin instance used for context and logging.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineEntity");
    }
}
