package io.github.mcengine.extension.addon.currency.entity.listener;

import io.github.mcengine.common.party.MCEnginePartyCommon;
import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil;
import io.github.mcengine.extension.addon.currency.entity.util.EntityUtil.RewardConfig;
import io.github.mcengine.common.currency.MCEngineCurrencyCommon;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
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
 * Listener that rewards players with currency when they kill configured entity types
 * or MythicMobs. If the killer is in a party, the reward is split among all party members
 * (including offline). If party support is unavailable, fallback to individual reward.
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

    /** Folder path for loading config files. */
    private final String folderPath;

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
        this.folderPath = folderPath;
        this.rewardMap = EntityUtil.loadAllMobConfigs(plugin, folderPath, logger);
    }

    /**
     * Handles the logic when an entity is killed.
     * <p>
     * If the killer is a player and the entity has a configured reward (either from
     * the default Bukkit EntityType or via a MythicMob configuration), the player
     * or their party members will be awarded currency asynchronously.
     * <p>
     * - If the entity is a regular Bukkit entity (e.g., ZOMBIE), it checks the default config.
     * - If the entity was spawned by MythicMobs and contains metadata "MythicMobName",
     *   it loads the config from the MythicMobs config directory using the mob's internal name.
     * <p>
     * If the killer is in a party, the reward is divided equally among online party members.
     * Otherwise, the killer receives the full reward.
     *
     * @param event The {@link EntityDeathEvent} triggered when an entity dies.
     */
    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player killer = event.getEntity().getKiller();
            if (killer == null) return;

            Entity entity = event.getEntity();
            EntityType type = entity.getType();

            logger.info(killer.getName() + " killed entity: " + type.name());

            RewardConfig config = rewardMap.get(type);

            // Check for MythicMob metadata if no vanilla config found
            if (config == null && entity.hasMetadata("MythicMobName")) {
                String mobName = entity.getMetadata("MythicMobName").get(0).asString();
                config = EntityUtil.getMythicMobReward(plugin, folderPath, mobName);
                if (config == null) {
                    logger.info("No reward config found for MythicMob: " + mobName);
                    return;
                }
                logger.info("Loaded MythicMob reward config: " + mobName);
            }

            if (config == null) return;

            final int rewardAmount = config.getRandomAmount(random);
            final String coinType = config.coinType();
            MCEnginePartyCommon partyApi = MCEnginePartyCommon.getApi();

            if (partyApi != null) {
                String partyId = partyApi.findPlayerPartyId(killer);
                if (partyId != null) {
                    Set<UUID> members = Bukkit.getOnlinePlayers().stream()
                            .filter(p -> {
                                String pid = partyApi.findPlayerPartyId(p);
                                return pid != null && pid.equals(partyId);
                            })
                            .map(Player::getUniqueId)
                            .collect(Collectors.toSet());

                    members.add(killer.getUniqueId());

                    final int share = rewardAmount / members.size();
                    for (UUID memberId : members) {
                        currencyApi.addCoin(memberId, coinType, share);
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (UUID memberId : members) {
                            Player member = Bukkit.getPlayer(memberId);
                            if (member != null) {
                                member.sendMessage("§aYour party earned §e" + rewardAmount + " " + coinType +
                                        "§a from defeating a §6" + type.name().toLowerCase(Locale.ROOT) +
                                        "§a. You received §e" + share + "§a.");
                            }
                        }
                    });

                    logger.info("Distributed " + rewardAmount + " " + coinType + " to party: " + partyId);
                    return;
                }
            }

            currencyApi.addCoin(killer.getUniqueId(), coinType, rewardAmount);

            Bukkit.getScheduler().runTask(plugin, () -> {
                killer.sendMessage("§aYou earned §e" + rewardAmount + " " + coinType +
                        "§a for defeating a §6" + type.name().toLowerCase(Locale.ROOT) + "§a.");
            });

            logger.info("Rewarded " + killer.getName() + " with " + rewardAmount + " " + coinType);
        });
    }
}
