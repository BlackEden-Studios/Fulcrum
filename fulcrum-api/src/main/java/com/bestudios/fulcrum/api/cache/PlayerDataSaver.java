package com.bestudios.fulcrum.api.cache;

import java.util.Map;
import java.util.UUID;

/**
 * Functional interface for saving player-specific data.
 *
 * @param <T> The type of player data to save
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see SmartCache
 * @see SessionCache
 * @see PlayerDataLoader
 */
@FunctionalInterface
public interface PlayerDataSaver<T> {
  /**
   * Saves player data for the given UUID.
   * This method should:
   * - Serialize the player data to a format suitable for storage
   * - Encapsulate any database or file system operations
   * - Handle any exceptions appropriately
   *
   * @param data     The player data to save
   * @return true if the data was saved successfully, false otherwise
   */
  boolean save(Map.Entry<UUID, T> data);

}
