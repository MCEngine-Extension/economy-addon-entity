package io.github.mcengine.extension.addon.currency.entity.listener;

import io.github.mcengine.common.party.MCEnginePartyCommon;
import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil;
import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil.RewardConfig;
import io.github.mcengine.common.currency.MCEngineCurrencyCommon;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
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
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Listener that rewards players with currency when they kill configured entity types.
 * If the killer is in a party, the reward is split among all party members (including offline).
 * If party support is unavailable, fallback to normal individual reward.
 */
public class EntityListener implements Listener {

    /** Plugin instance used for scheduling and accessing the data folder. */
    private final Plugin plugin;

    /** Logger used to print debug or info messages. */
    private final MCEngineExtensionLogger logger;

    /** API used for modifying player currency. */
    private final MCEngineCurrencyCommon currencyApi;

    /** Random instance used to calculate reward amounts. */
    private final Random random = new Random();

    /** Cached reward configurations by entity type. */
    private final Map<EntityType, RewardConfig> rewardMap;

    /**
     * Constructs a new EntityListener and loads reward configurations for all mobs.
     *
     * @param plugin     The plugin instance for scheduling tasks.
     * @param folderPath The path to the folder containing entity config files.
     * @param logger     Logger instance for debug/info output.
     */
    public EntityListener(Plugin plugin, String folderPath, MCEngineExtensionLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.currencyApi = MCEngineCurrencyCommon.getApi();
        this.rewardMap = EntityUtil.loadAllMobConfigs(plugin, folderPath, logger);
    }

    /**
     * Called when an entity dies. If the killer is a player and the entity type has
     * a reward configured, it will asynchronously calculate and award the player or
     * party members (online and offline) with currency. If party support is unavailable,
     * the reward is given to the player directly.
     *
     * @param event The entity death event triggered by Bukkit.
     */
    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player killer = event.getEntity().getKiller();
            if (killer == null) return;

            EntityType type = event.getEntityType();
            logger.info(killer.getName() + " killed entity: " + type.name());

            RewardConfig config = rewardMap.get(type);
            if (config == null) {
                logger.info("No reward config found for: " + type.name());
                return;
            }

            int rewardAmount = config.getRandomAmount(random);
            MCEnginePartyCommon partyApi = MCEnginePartyCommon.getApi();

            if (partyApi != null) {
                String partyId = partyApi.findPlayerPartyId(killer);
                if (partyId != null) {
                    // Workaround: collect members by scanning online players
                    Set<UUID> members = Bukkit.getOnlinePlayers().stream()
                            .filter(p -> {
                                String pid = partyApi.findPlayerPartyId(p);
                                return pid != null && pid.equals(partyId);
                            })
                            .map(Player::getUniqueId)
                            .collect(Collectors.toSet());

                    // Always include killer if not online (e.g. fallback scenario)
                    members.add(killer.getUniqueId());

                    int share = rewardAmount / members.size();
                    for (UUID memberId : members) {
                        currencyApi.addCoin(memberId, config.coinType(), share);
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (UUID memberId : members) {
                            Player member = Bukkit.getPlayer(memberId);
                            if (member != null) {
                                member.sendMessage("§aYour party earned §e" + rewardAmount + " " + config.coinType() +
                                        "§a from defeating a §6" + type.name().toLowerCase(Locale.ROOT) +
                                        "§a. You received §e" + share + "§a.");
                            }
                        }
                    });

                    logger.info("Distributed " + rewardAmount + " " + config.coinType() + " to party: " + partyId);
                    return;
                }
            }

            // Fallback or solo reward
            currencyApi.addCoin(killer.getUniqueId(), config.coinType(), rewardAmount);

            Bukkit.getScheduler().runTask(plugin, () -> {
                killer.sendMessage("§aYou earned §e" + rewardAmount + " " + config.coinType() +
                        "§a for defeating a §6" + type.name().toLowerCase(Locale.ROOT) + "§a.");
            });

            logger.info("Rewarded " + killer.getName() + " with " + rewardAmount + " " + config.coinType());
        });
    }
}
