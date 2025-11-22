package com.bestudios.fulcrum.api.cache;

import java.util.UUID;

/**
 * Functional interface for loading player-specific data.
 * Implementations should handle both loading existing data and creating defaults.
 *
 * @param <T> The type of player data to load
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see SessionCache
 */
@FunctionalInterface
public interface PlayerDataLoader<T> {
  /**
   * Loads or creates player data for the given UUID.
   * This method should:
   * - Query a database or file system for existing data
   * - Return default data if none exists
   * - Handle any exceptions appropriately
   *
   * @param playerID The player's unique identifier
   * @return The loaded or newly created player data
   */
  T load(UUID playerID);
}

