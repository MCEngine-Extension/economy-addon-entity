package io.github.mcengine.addon.currency.entity.listener;

import io.github.mcengine.api.currency.MCEngineCurrencyApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class EntityListener implements Listener {

    private final Plugin plugin;
    private final MCEngineAddOnLogger logger;
    private final MCEngineCurrencyApi currencyApi;
    private final Random random = new Random();
    private final Map<EntityType, RewardConfig> rewardMap = new HashMap<>();

    public EntityListener(Plugin plugin, MCEngineAddOnLogger logger) {
        this.logger = logger;
        this.plugin = plugin;
        this.currencyApi = MCEngineCurrencyApi.getApi();
        loadAllMobConfigs();
    }

    private void loadAllMobConfigs() {
        File baseDir = new File(plugin.getDataFolder().getParentFile(), "MCEngineCurrency/addons/MCEngineEntity");
        if (!baseDir.exists()) {
            logger.warning("Directory not found: " + baseDir.getAbsolutePath());
            return;
        }

        logger.info("Loading configs from: " + baseDir.getAbsolutePath());

        List<File> yamlFiles = new ArrayList<>();
        collectYamlFiles(baseDir, yamlFiles);

        for (File file : yamlFiles) {
            logger.info("Reading file: " + file.getName());

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String entityStr = config.getString("entity", "").toUpperCase(Locale.ROOT);
            String coinType = config.getString("coinType", "coin");
            String amountRange = config.getString("amount", "0");

            try {
                EntityType type = EntityType.valueOf(entityStr);
                rewardMap.put(type, new RewardConfig(coinType, amountRange));
                logger.info("Loaded reward for: " + type.name() +
                        " => " + coinType + " " + amountRange);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid entity type in " + file.getName() + ": " + entityStr);
            }
        }
    }

    private void collectYamlFiles(File dir, List<File> outList) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                collectYamlFiles(f, outList);
            } else if (f.getName().toLowerCase().endsWith(".yml")) {
                outList.add(f);
            }
        }
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

    private record RewardConfig(String coinType, String amountRange) {
        int getRandomAmount(Random random) {
            if (amountRange.contains("~")) {
                String[] parts = amountRange.split("~");
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                return min + random.nextInt(max - min + 1);
            }
            return Integer.parseInt(amountRange.trim());
        }
    }
}
