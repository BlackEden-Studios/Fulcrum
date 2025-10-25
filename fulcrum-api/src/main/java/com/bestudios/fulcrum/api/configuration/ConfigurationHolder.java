package com.bestudios.fulcrum.api.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Represents a general-purpose holder interface for configuration management in Fulcrum plugins.
 * <p>
 * Implementations of this interface abstract access to file-based configurations loaded
 * using Bukkit’s {@link FileConfiguration} system. This provides consistent access to
 * reading, reloading, and saving configuration data for plugins.
 * </p>
 *
 * <p>
 * Example usage inside plugins:
 * <pre>{@code
 * ConfigurationHolder<YamlConfiguration> holder = new DefaultConfigurationHolder.Builder(plugin)
 *     .setDataFolder(plugin.getDataFolder())
 *     .setFileName("settings.yml")
 *     .build();
 * YamlConfiguration config = holder.getConfig();
 * }</pre>
 * </p>
 *
 * @param <ConfigurationT> The type of FileConfiguration (usually {@link org.bukkit.configuration.file.YamlConfiguration})
 *
 * @see ConfigurationsRegistry
 * @since 1.0
 */
public interface ConfigurationHolder<ConfigurationT extends FileConfiguration> {

  /**
   * Retrieves the loaded configuration instance.
   * Implementations should load it from disk if it is not yet loaded.
   *
   * @return The loaded configuration object, or {@code null} if loading failed.
   */
  @Nullable
  ConfigurationT getConfig();

  /**
   * Reloads configuration data from the file system.
   * <p>
   * Implementations must overwrite the existing configuration state in memory with
   * a fresh copy loaded from the disk.
   * </p>
   *
   * @return {@code true} if the reload succeeded, {@code false} otherwise.
   */
  boolean reloadConfig();

  /**
   * Persists the in-memory configuration to disk.
   *
   * @return {@code true} if the save operation completed successfully,
   *         {@code false} if writing to disk failed.
   */
  boolean saveConfig();

  /**
   * Retrieves the name of the configuration file.
   *
   * @return The name of the configuration file, without its path.
   */
  String getConfigName();

  /**
   * Checks whether the configuration file exists in the file system.
   *
   * @return {@code true} if the file exists physically, {@code false} otherwise.
   */
  boolean configExists();

  /**
   * Gets the file object for the currently loaded configuration.
   *
   * @return The {@link File} representing the configuration file.
   */
  File getConfigFile();
}
