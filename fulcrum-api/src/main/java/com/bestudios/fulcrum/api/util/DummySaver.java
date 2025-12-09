package com.bestudios.fulcrum.api.util;

import com.bestudios.fulcrum.api.cache.PlayerDataSaver;

import java.util.Map;
import java.util.UUID;

/**
 * Dummy implementation of {@link PlayerDataSaver} that simulates a successful save.
 * <p>
 * This is useful for testing purposes or when no data needs to be saved.
 * @param <T> The type of data to save
 */
public class DummySaver<T> implements PlayerDataSaver<T> {

  @Override
  public boolean save(Map.Entry<UUID, T> data) {
    return true;
  }
}