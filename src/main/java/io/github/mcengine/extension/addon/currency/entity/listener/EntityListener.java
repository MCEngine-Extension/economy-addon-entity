package io.github.mcengine.extension.addon.currency.entity.listener;

import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil;
import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil.RewardConfig;
import io.github.mcengine.common.currency.MCEngineCurrencyCommon;
import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Listener that rewards players with currency when they kill configured entity types.
 * The reward configuration is defined per entity type and handled asynchronously.
 */
public class EntityListener implements Listener {

    /** Plugin instance used for scheduling and accessing the data folder. */
    private final Plugin plugin;

    /** Logger used to print debug or info messages. */
    private final MCEngineAddOnLogger logger;

    /** API used for modifying player currency. */
    private final MCEngineCurrencyCommon currencyApi;

    /** Random instance used to calculate reward amounts. */
    private final Random random = new Random();

    /** Cached reward configurations by entity type. */
    private final Map<EntityType, RewardConfig> rewardMap;

    /**
     * Constructs a new EntityListener and loads reward configurations for all mobs.
     *
     * @param plugin The plugin instance for scheduling tasks.
     * @param logger Logger instance for debug/info output.
     */
    public EntityListener(Plugin plugin, MCEngineAddOnLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.currencyApi = MCEngineCurrencyCommon.getApi();
        this.rewardMap = EntityUtil.loadAllMobConfigs(plugin, logger);
    }

    /**
     * Called when an entity dies. If the killer is a player and the entity type has
     * a reward configured, it will asynchronously calculate and award the player with currency.
     *
     * @param event The entity death event triggered by Bukkit.
     */
    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        // Run reward logic asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player killer = event.getEntity().getKiller();
            if (killer == null) {
                return;
            }

            EntityType type = event.getEntityType();
            logger.info(killer.getName() + " killed entity: " + type.name());

            RewardConfig config = rewardMap.get(type);
            if (config == null) {
                logger.info("No reward config found for: " + type.name());
                return;
            }

            int reward = config.getRandomAmount(random);
            currencyApi.addCoin(killer.getUniqueId(), config.coinType(), reward);

            // Notify the player on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                killer.sendMessage("§aYou earned §e" + reward + " " + config.coinType() +
                        "§a for defeating a §6" + type.name().toLowerCase(Locale.ROOT) + "§a.");
            });

            logger.info("Rewarded " + killer.getName() + " with " + reward + " " + config.coinType());
        });
    }
}
