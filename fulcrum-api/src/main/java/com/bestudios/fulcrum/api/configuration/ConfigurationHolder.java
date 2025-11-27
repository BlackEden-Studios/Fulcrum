package com.bestudios.fulcrum.api.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents a general-purpose holder interface for configuration management in Fulcrum plugins.
 * <p>
 * Implementations of this interface abstract access to file-based configurations loaded
 * using Bukkit’s {@link FileConfiguration} system. This provides consistent access to
 * reading, reloading, and saving configuration data for plugins.
 *
 * @param <T> The type of FileConfiguration (usually {@link YamlConfiguration})
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see ConfigurationsRegistry
 */
public interface ConfigurationHolder<T extends FileConfiguration> {

  /**
   * Retrieves the loaded configuration instance.
   * Implementations should load it from disk if it is not yet loaded.
   *
   * @return The loaded configuration object, or {@code null} if loading failed.
   */
  @Nullable
  T getConfig();

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

  /**
   * Validates a set of files using a provided action.
   *
   * @param resources     The files to validate
   * @param action        The action to perform on each file
   * @param errorTemplate The error message template to use when an exception occurs
   */
  default void validate(@NotNull Set<File> resources, FileOperation action, @NotNull String errorTemplate) {
    resources.forEach(file -> {
      try {
        action.execute(file);
      } catch (IOException e) {
        // Formats the error message with the specific file path
        throw new IllegalStateException(String.format(errorTemplate, file.getPath()), e);
      }
    });
  }

  // 1. Define a functional interface that allows Exceptions
  @FunctionalInterface
  interface FileOperation {
    void execute(File file) throws IOException;
  }
}
