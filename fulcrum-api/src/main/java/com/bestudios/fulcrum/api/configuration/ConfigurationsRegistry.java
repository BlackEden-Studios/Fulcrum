package com.bestudios.fulcrum.api.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry interface for managing multiple configuration holders within a plugin.
 * <p>
 * This interface defines methods for registering, retrieving, and managing
 * different configuration files used by the plugin. Implementations should
 * provide thread-safe access to the configurations.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see ConfigurationHolder
 */
public interface ConfigurationsRegistry<T extends FileConfiguration> {
  /**
   * Registers a new configuration holder with the registry.
   *
   * @param name    The name of the configuration holder.
   * @param holder  The configuration holder instance.
   */
  void register(@NotNull String name, @NotNull ConfigurationHolder<T> holder);
  /**
   * Unregisters a configuration holder from the registry.
   *
   * @param name The name of the configuration holder to unregister.
   */
  void unregister(@NotNull String name);
  /**
   * Checks if a configuration holder is registered with this registry.
   *
   * @param name The name of the configuration holder to check.
   * @return true if the holder is registered, false otherwise.
   */
  boolean isRegistered(@NotNull String name);
  /**
   * Retrieves a configuration holder by its name.
   *
   * @param name The name of the configuration holder.
   * @return The configuration holder instance, or null if not found.
   */
  @Nullable
  ConfigurationHolder<T> getHolder(@NotNull String name);
}
