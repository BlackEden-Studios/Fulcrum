package com.bestudios.fulcrum.api.region;

import com.bestudios.fulcrum.api.configuration.ConfigurationHolder;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skeletal implementation of a Data-Driven Region.
 * <br>
 * Manages the internal data registry and configuration lifecycle,
 * leaving the spatial logic {@link #contains(Position)} to specific implementations.
 */
public abstract class AbstractRegion<C extends FileConfiguration> implements Region<C> {

  protected final String id;
  protected final ConfigurationHolder<C> configurationHolder;

  /**
   * The dynamic data registry.
   * Thread-safe to allow concurrent reads/writes from services.
   */
  protected final Map<String, Object> dataRegistry;

  /**
   * Constructor
   * @param id The region ID.
   * @param configurationHolder The configuration holder.
   */
  public AbstractRegion(@NotNull String id, @NotNull ConfigurationHolder<C> configurationHolder) {
    this.id = Objects.requireNonNull(id, "Region ID cannot be null");
    this.configurationHolder = Objects.requireNonNull(configurationHolder, "ConfigHolder cannot be null");
    this.dataRegistry = new ConcurrentHashMap<>();
  }

  // --- Interface Implementation ---

  @Override
  public @NotNull String getID() {
    return id;
  }

  @Override
  public @NotNull ConfigurationHolder<C> getConfiguration() {
    return configurationHolder;
  }

  @Override
  public abstract boolean contains(@NotNull Position position);

  // --- Data Logic ---

  @Override
  public <T> Optional<T> getData(@NotNull String key, @NotNull Class<T> type) {
    Object val = dataRegistry.get(key);
    if (val != null && type.isAssignableFrom(val.getClass())) {
      return Optional.of(type.cast(val));
    }
    return Optional.empty();
  }

  @Override
  public @Nullable Object getData(@NotNull String key) {
    return dataRegistry.get(key);
  }

  @Override
  public @NotNull Map<String, Object> getAllData() {
    return Collections.unmodifiableMap(dataRegistry);
  }

  /**
   * Helper for subclasses to populate the registry.
   * @param key the key to set
   * @param value the value to set, or null to remove the key
   */
  protected void setData(@NotNull String key, @Nullable Object value) {
    if (value != null) {
      this.dataRegistry.put(key, value);
    } else {
      this.dataRegistry.remove(key);
    }
  }

  /**
  * Helper for subclasses to populate the registry.
  * @param key the key to set
  * @param value the value to set
  * @param defaultValue the default value to set if the value is null
  */
  protected void setData(@NotNull String key, @Nullable Object value, @NotNull Object defaultValue) {
    this.dataRegistry.put(key, Objects.requireNonNullElse(value, defaultValue));
  }

  // --- Lifecycle ---

  @Override
  public void refresh() {
    this.configurationHolder.reloadConfig();
    this.dataRegistry.clear(); // Clear old cache
    this.onLoad(); // Re-populate
  }

  @Override
  public void save() {
    this.onSave(); // Persist current map to config object
    this.configurationHolder.saveConfig();
  }

  /**
   * Called to populate the {@link #dataRegistry} from the configuration source.
   */
  protected abstract void onLoad();

  /**
   * Called to write the {@link #dataRegistry} contents back to the configuration source.
   */
  protected abstract void onSave();

  // --- Identity ---

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Region<?> region)) return false;
    return id.equals(region.getID());
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}