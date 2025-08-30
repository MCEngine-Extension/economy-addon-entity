package io.github.mcengine.extension.addon.economy.entity.database.sqlite;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.extension.addon.economy.entity.database.EntityDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * SQLite implementation of {@link EntityDB}.
 */
public class EntityDBSQLite implements EntityDB {

    /**
     * Shared JDBC connection provided by the economy module.
     */
    private final Connection conn;

    /**
     * Logger for diagnostics and setup messages.
     */
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs the DB helper for SQLite.
     *
     * @param conn   JDBC connection
     * @param logger extension logger
     */
    public EntityDBSQLite(Connection conn, MCEngineExtensionLogger logger) {
        this.conn = conn;
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
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
            if (logger != null) logger.info("[EntityDB] SQLite schema ensured.");
        } catch (Exception e) {
            if (logger != null) logger.warning("[EntityDB] SQLite schema creation failed: " + e.getMessage());
        }
    }

    @Override
    public void insertKillLog(String playerUuid, String entityType, String coinType, int amount) {
        final String sql = """
            INSERT INTO economy_entity_kill_log (player_uuid, entity_type, coin_type, amount, created_time)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP);
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerUuid);
            ps.setString(2, entityType);
            ps.setString(3, coinType);
            ps.setInt(4, amount);
            ps.executeUpdate();
        } catch (Exception e) {
            if (logger != null) logger.warning("[EntityDB] SQLite insertKillLog failed: " + e.getMessage());
        }
    }
}
