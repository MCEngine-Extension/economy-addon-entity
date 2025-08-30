package io.github.mcengine.extension.addon.economy.entity.database;

/**
 * Abstraction for Economy Entity database operations (multi-dialect support).
 *
 * <p>Implementations should manage the table:</p>
 * <ul>
 *   <li><strong>economy_entity_kill_log</strong>:
 *       kill_id (PK), player_uuid, entity_type, coin_type, amount, created_time</li>
 * </ul>
 */
public interface EntityDB {

    /** Creates required tables if they don't already exist. */
    void ensureSchema();

    /**
     * Inserts a kill log row (optional utility for future features).
     *
     * @param playerUuid UUID string of the player
     * @param entityType killed entity type name
     * @param coinType   rewarded coin type
     * @param amount     rewarded amount
     */
    void insertKillLog(String playerUuid, String entityType, String coinType, int amount);
}
