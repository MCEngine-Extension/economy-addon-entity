package io.github.mcengine.extension.addon.currency.entity.util;

import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EntityUtil {

    public static void createSimpleFiles(Plugin plugin) {
        File addonDir = new File(plugin.getDataFolder(), "configs/addons/MCEngineEntity");

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

    public static Map<EntityType, RewardConfig> loadAllMobConfigs(Plugin plugin, MCEngineAddOnLogger logger) {
        Map<EntityType, RewardConfig> rewardMap = new HashMap<>();
        File baseDir = new File(plugin.getDataFolder(), "configs/addons/MCEngineEntity");

        if (!baseDir.exists()) {
            logger.warning("Directory not found: " + baseDir.getAbsolutePath());
            return rewardMap;
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
                logger.info("Loaded reward for: " + type.name() + " => " + coinType + " " + amountRange);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid entity type in " + file.getName() + ": " + entityStr);
            }
        }

        return rewardMap;
    }

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

    public record RewardConfig(String coinType, String amountRange) {
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
