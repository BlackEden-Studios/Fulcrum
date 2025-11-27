package com.bestudios.fulcrum.api.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link ConfigurationsRegistry} that manages
 * {@link DefaultConfigurationHolder} instances in a thread-safe manner.
 * <p>
 * This registry allows the plugin to register and retrieve multiple configurations safely
 * in a multithreaded environment. While the interface accepts any
 * {@link ConfigurationHolder} implementation, this default implementation
 * restricts registration to {@link DefaultConfigurationHolder} instances only.
 * <p>
 * Example usage:
 * <pre>{@code
 * ConfigurationsRegistry registry = new DefaultConfigurationsRegistry();
 * DefaultConfigurationHolder holder = new DefaultConfigurationHolder();
 *
 * registry.register("main-config", holder);
 * DefaultConfigurationHolder retrieved = registry.getHolder("main-config");
 * }</pre>
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see ConfigurationsRegistry
 * @see DefaultConfigurationHolder
 */
public class DefaultConfigurationsRegistry implements ConfigurationsRegistry<YamlConfiguration> {

  private static final String NAME_ERROR = "Configuration name cannot be null or blank";

  /** Internal thread-safe map storing registered configuration holders. */
  private final Map<String, ConfigurationHolder<YamlConfiguration>> configs = new ConcurrentHashMap<>();

  /** The plugin instance */
  private final JavaPlugin plugin;

  /**
   * Constructs a new DefaultConfigurationsRegistry associated with the given plugin.
   *
   * @param plugin the JavaPlugin instance this registry is associated with
   */
  public DefaultConfigurationsRegistry(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * Registers a configuration holder with the specified name.
   * <p>
   * This implementation only accepts {@link DefaultConfigurationHolder} instances.
   * <p>
   * If a holder with the same name already exists, it will be replaced.
   *
   * @param name   the unique identifier for this configuration holder
   * @param holder the configuration holder to register
   * @throws IllegalArgumentException if name parameter is blank
   * @throws NullPointerException     if name or holder are null
   */
  @Override
  public void register(@NotNull String name, @NotNull ConfigurationHolder<YamlConfiguration> holder) {
    // Validate
    Objects.requireNonNull(name, NAME_ERROR);
    Objects.requireNonNull(holder, "Configuration holder cannot be null");
    if (name.isBlank()) throw new IllegalArgumentException(NAME_ERROR);

    // Register
    this.configs.put(name, holder);
    this.plugin.getLogger().info("Registered configuration holder '" + name + "'");
  }

  /**
   * Unregisters the configuration holder associated with the specified name.
   * <p>
   * If no holder is registered with the given name, this method does nothing.
   *
   * @param name the unique identifier of the configuration holder to remove
   */
  @Override
  public void unregister(@NotNull String name) {
    Objects.requireNonNull(name, NAME_ERROR);
    this.configs.remove(name);
    plugin.getLogger().info("Unregistered configuration holder '" + name + "'");
  }

  /**
   * Checks whether a configuration holder is registered with the specified name.
   *
   * @param name the unique identifier to check
   * @return {@code true} if a holder is registered with the given name,
   *         {@code false} otherwise
   */
  @Override
  public boolean isRegistered(@NotNull String name) {
    Objects.requireNonNull(name, NAME_ERROR);
    return configs.containsKey(name);
  }

  /**
   * Retrieves the configuration holder registered with the specified name.
   *
   * @param name the unique identifier of the configuration holder to retrieve
   * @return the {@link DefaultConfigurationHolder} associated with the given name,
   * or {@code null} if no holder is registered with that name
   */
  @Override @Nullable
  public ConfigurationHolder<YamlConfiguration> getHolder(@NotNull String name) {
    Objects.requireNonNull(name, NAME_ERROR);
    return configs.get(name);
  }
}
