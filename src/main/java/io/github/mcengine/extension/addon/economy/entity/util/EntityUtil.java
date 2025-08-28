package io.github.mcengine.extension.addon.economy.entity.util;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for handling entity reward configurations.
 * Includes methods for creating example files and loading reward data.
 */
public class EntityUtil {

    /**
     * Creates example YAML configuration files for ZOMBIE and SKELETON
     * inside the "default" directory if they do not already exist.
     *
     * @param plugin     The plugin instance used to resolve the data folder.
     * @param folderPath The folder path relative to the plugin's data folder.
     * @param logger     Extension logger for reporting creation issues.
     */
    public static void createSimpleFiles(Plugin plugin, String folderPath, MCEngineExtensionLogger logger) {
        File defaultDir = new File(plugin.getDataFolder(), folderPath + "/default");

        if (!defaultDir.exists() && !defaultDir.mkdirs()) {
            logger.warning("Failed to create default entity config folder.");
            return;
        }

        File zombieFile = new File(defaultDir, "zombie.yml");
        File skeletonFile = new File(defaultDir, "skeleton.yml");

        if (!zombieFile.exists()) {
            try (FileWriter writer = new FileWriter(zombieFile)) {
                writer.write("entity: ZOMBIE\n");
                writer.write("coinType: coin\n");
                writer.write("amount: 100~200\n");
            } catch (IOException e) {
                logger.warning("Failed to create zombie.yml: " + e.getMessage());
            }
        }

        if (!skeletonFile.exists()) {
            try (FileWriter writer = new FileWriter(skeletonFile)) {
                writer.write("entity: SKELETON\n");
                writer.write("coinType: copper\n");
                writer.write("amount: 50\n");
            } catch (IOException e) {
                logger.warning("Failed to create skeleton.yml: " + e.getMessage());
            }
        }
    }

    /**
     * Loads all reward configuration files from the "default" folder
     * and maps them to their respective entity types.
     *
     * @param plugin     The plugin instance.
     * @param folderPath The folder path relative to the plugin's data folder.
     * @param logger     Logger for reporting invalid configs or errors.
     * @return A map of EntityType to its RewardConfig.
     */
    public static Map<EntityType, RewardConfig> loadAllMobConfigs(Plugin plugin, String folderPath, MCEngineExtensionLogger logger) {
        Map<EntityType, RewardConfig> rewardMap = new HashMap<>();
        File defaultDir = new File(plugin.getDataFolder(), folderPath + "/default");

        if (!defaultDir.exists()) {
            logger.warning("Directory not found: " + defaultDir.getAbsolutePath());
            return rewardMap;
        }

        logger.info("Loading configs from: " + defaultDir.getAbsolutePath());

        List<File> yamlFiles = new ArrayList<>();
        collectYamlFiles(defaultDir, yamlFiles);

        for (File file : yamlFiles) {
            logger.info("Reading file: " + file.getName());

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String entityStr = config.getString("entity", "").toUpperCase(Locale.ROOT);
            String coinType = config.getString("coinType", "coin");
            String amountRange = config.getString("amount", "0");

            try {
                EntityType type = EntityType.valueOf(entityStr);
                rewardMap.put(type, new RewardConfig(coinType, amountRange));
                logger.info("Loaded reward for: " + type.name() + " => " + coinType + " " + amountRange);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid entity type in " + file.getName() + ": " + entityStr);
            }
        }

        return rewardMap;
    }

    /**
     * Recursively collects all .yml files in a given directory.
     *
     * @param dir     The root directory to search.
     * @param outList The output list to fill with found YAML files.
     */
    private static void collectYamlFiles(File dir, List<File> outList) {
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

    /**
     * Record holding reward configuration for a specific entity.
     *
     * @param coinType    The type of coin to reward.
     * @param amountRange The amount range (e.g., "100~200" or "50").
     */
    public record RewardConfig(String coinType, String amountRange) {

        /**
         * Returns a randomly selected reward amount based on the configured range.
         *
         * @param random Random instance to use.
         * @return A reward value within the specified range.
         */
        public int getRandomAmount(Random random) {
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
