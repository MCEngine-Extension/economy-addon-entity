package io.github.mcengine.extension.addon.currency.entity.listener;

import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil;
import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil.RewardConfig;
import io.github.mcengine.api.currency.MCEngineCurrencyApi;
import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class EntityListener implements Listener {

    private final Plugin plugin;
    private final MCEngineAddOnLogger logger;
    private final MCEngineCurrencyApi currencyApi;
    private final Random random = new Random();
    private final Map<EntityType, RewardConfig> rewardMap;

    public EntityListener(Plugin plugin, MCEngineAddOnLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.currencyApi = MCEngineCurrencyApi.getApi();
        this.rewardMap = EntityUtil.loadAllMobConfigs(plugin, logger);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            logger.info("Entity killed but no player killer.");
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

        killer.sendMessage("§aYou earned §e" + reward + " " + config.coinType() +
                "§a for defeating a §6" + type.name().toLowerCase(Locale.ROOT) + "§a.");
        logger.info("Rewarded " + killer.getName() + " with " + reward + " " + config.coinType());
    }
}
