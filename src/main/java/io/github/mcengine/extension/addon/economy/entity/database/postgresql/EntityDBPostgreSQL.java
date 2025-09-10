package io.github.mcengine.extension.addon.economy.entity.database.postgresql;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.common.economy.MCEngineEconomyCommon;
import io.github.mcengine.extension.addon.economy.entity.database.EntityDB;

/**
 * PostgreSQL implementation of {@link EntityDB}.
 * <p>
 * Uses the unified Economy DB API ({@code executeQuery} / {@code getValue}) so it
 * does not hold a raw JDBC {@code Connection}. This keeps it compatible with both
 * SQL and NoSQL backends through a common interface.
 */
public class EntityDBPostgreSQL implements EntityDB {

    /**
     * Logger for diagnostics and setup messages.
     */
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs the DB helper for PostgreSQL.
     *
     * @param logger extension logger
     */
    public EntityDBPostgreSQL(MCEngineExtensionLogger logger) {
        this.logger = logger;
    }

    @Override
    public void ensureSchema() {
        final String sql = """
            CREATE TABLE IF NOT EXISTS economy_entity_kill_log (
                kill_id SERIAL PRIMARY KEY,
                player_uuid VARCHAR(36),
                entity_type TEXT,
                coin_type TEXT,
                amount INT,
                created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try {
            MCEngineEconomyCommon.getApi().executeQuery(sql);
            if (logger != null) logger.info("[EntityDB] PostgreSQL schema ensured.");

            // Example: verify table exists / row count using getValue
            Integer count = MCEngineEconomyCommon.getApi()
                    .getValue("SELECT COUNT(*) FROM economy_entity_kill_log;", Integer.class);
            if (logger != null) logger.info("[EntityDB] PostgreSQL kill_log rows: " + count);
        } catch (Exception e) {
            if (logger != null) logger.warning("[EntityDB] PostgreSQL schema creation failed: " + e.getMessage());
        }
    }

    @Override
    public void insertKillLog(String playerUuid, String entityType, String coinType, int amount) {
        // NOTE: Using string formatting for demonstration to align with executeQuery(String).
        // In production, prefer parameterized execution in the underlying implementation.
        final String sql = """
            INSERT INTO economy_entity_kill_log (player_uuid, entity_type, coin_type, amount, created_time)
            VALUES ('%s', '%s', '%s', %d, CURRENT_TIMESTAMP);
            """.formatted(playerUuid, entityType, coinType, amount);
        try {
            MCEngineEconomyCommon.getApi().executeQuery(sql);
            if (logger != null) logger.info("[EntityDB] PostgreSQL KillLog inserted for player=" + playerUuid);

            // Example: read back the latest id using getValue
            Long lastId = MCEngineEconomyCommon.getApi()
                    .getValue("SELECT MAX(kill_id) FROM economy_entity_kill_log;", Long.class);
            if (logger != null) logger.info("[EntityDB] PostgreSQL last inserted kill_id=" + lastId);
        } catch (Exception e) {
            if (logger != null) logger.warning("[EntityDB] PostgreSQL insertKillLog failed: " + e.getMessage());
        }
    }
}
