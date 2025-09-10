package io.github.mcengine.extension.addon.economy.entity.database.sqlite;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.common.economy.MCEngineEconomyCommon;
import io.github.mcengine.extension.addon.economy.entity.database.EntityDB;

/**
 * SQLite implementation of {@link EntityDB}.
 * <p>
 * Uses the unified {@link io.github.mcengine.common.economy.database.MCEngineEconomyApiDBInterface}
 * API instead of holding a raw JDBC connection.
 */
public class EntityDBSQLite implements EntityDB {

    /** Logger for diagnostics and setup messages. */
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs the DB helper for SQLite.
     *
     * @param logger extension logger
     */
    public EntityDBSQLite(MCEngineExtensionLogger logger) {
        this.logger = logger;
    }

    @Override
    public void ensureSchema() {
        final String sql = """
            CREATE TABLE IF NOT EXISTS economy_entity_kill_log (
                kill_id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid VARCHAR(36),
                entity_type TEXT,
                coin_type TEXT,
                amount INT,
                created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try {
            MCEngineEconomyCommon.getApi().executeQuery(sql);
            if (logger != null) logger.info("[EntityDB] SQLite schema ensured.");

            // Example use of getValue: count how many rows exist after creation
            Integer count = MCEngineEconomyCommon.getApi()
                    .getValue("SELECT COUNT(*) FROM economy_entity_kill_log;", Integer.class);
            if (logger != null) logger.info("[EntityDB] Current kill_log rows: " + count);

        } catch (Exception e) {
            if (logger != null) logger.warning("[EntityDB] SQLite schema creation failed: " + e.getMessage());
        }
    }

    @Override
    public void insertKillLog(String playerUuid, String entityType, String coinType, int amount) {
        final String sql = """
            INSERT INTO economy_entity_kill_log (player_uuid, entity_type, coin_type, amount, created_time)
            VALUES ('%s', '%s', '%s', %d, CURRENT_TIMESTAMP);
            """.formatted(playerUuid, entityType, coinType, amount);
        try {
            MCEngineEconomyCommon.getApi().executeQuery(sql);

            if (logger != null) logger.info("[EntityDB] KillLog inserted for player=" + playerUuid);

            // Example: query back the last inserted row ID
            Long lastId = MCEngineEconomyCommon.getApi()
                    .getValue("SELECT MAX(kill_id) FROM economy_entity_kill_log;", Long.class);
            if (logger != null) logger.info("[EntityDB] Last inserted kill_id=" + lastId);

        } catch (Exception e) {
            if (logger != null) logger.warning("[EntityDB] SQLite insertKillLog failed: " + e.getMessage());
        }
    }
}
