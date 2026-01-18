package com.bestudios.fulcrum.api.util;

import com.bestudios.fulcrum.api.cache.PlayerDataSaver;
import com.bestudios.fulcrum.api.cache.SessionCache;
import com.bestudios.fulcrum.api.cache.SmartCache;

import java.util.Map;
import java.util.UUID;

/**
 * Dummy implementation of {@link PlayerDataSaver} that simulates a successful save.
 * <br>
 * This is useful for testing purposes or when no data needs to be saved.
 * @param <T> The type of data to save
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see PlayerDataSaver
 * @see SmartCache
 * @see SessionCache
 */
public class DummySaver<T> implements PlayerDataSaver<T> {

  @Override
  public boolean save(Map.Entry<UUID, T> data) {
    return true;
  }
}