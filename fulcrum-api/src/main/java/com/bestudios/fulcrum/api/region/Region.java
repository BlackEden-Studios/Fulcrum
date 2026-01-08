package com.bestudios.fulcrum.api.region;

import com.bestudios.fulcrum.api.configuration.ConfigurationHolder;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Represents a generic region within the Fulcrum framework.
 * <p>
 * This interface treats region data as a dynamic registry of Key-Value pairs,
 * allowing flexible metadata storage without polluting the API with specific methods.
 * </p>
 *
 * @param <C> The type of configuration object (e.g., YamlConfiguration)
 */
public interface Region<C extends FileConfiguration> {

  /**
   * @return The unique identifier for this region.
   */
  @NotNull String getID();

  /**
   * Gets the configuration holder for this region.
   * @return The configuration holder.
   */
  @NotNull ConfigurationHolder<C> getConfiguration();

  /**
   * Checks if the given position is spatially "inside" this region.
   * The logic (Cuboid, Polygon, Distance) is defined by the implementation.
   *
   * @param position The position to check.
   * @return true if the position is within the region bounds.
   */
  boolean contains(@NotNull Position position);

  // --- Dynamic Data Access ---

  /**
   * Retrieves a value from the region's data registry.
   *
   * @param key The data key (e.g., "owner", "spawn_point").
   * @return An Optional containing the value if present and correctly cast, empty otherwise.
   */
  <T> Optional<T> getData(@NotNull String key, @NotNull Class<T> type);

  /**
   * Retrieves a raw value from the region's data registry.
   * @param key The data key.
   * @return The value, or null if not present.
   */
  @Nullable Object getData(@NotNull String key);

  /**
   * @return A read-only view of all data currently held by this region.
   */
  @NotNull Map<String, Object> getAllData();

  // --- Lifecycle ---

  /**
   * Refreshes the region's data from the configuration file.
   */
  void refresh();

  /**
   * Saves the region's data to the configuration file.
   */
  void save();
}