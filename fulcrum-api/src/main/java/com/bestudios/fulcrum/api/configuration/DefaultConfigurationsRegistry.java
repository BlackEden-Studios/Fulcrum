package com.bestudios.fulcrum.api.configuration;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link ConfigurationsRegistry} that manages
 * {@link DefaultConfigurationHolder} instances in a thread-safe manner.
 *
 * <p>This registry allows the plugin to register and retrieve multiple configurations safely
 * in a multithreaded environment. While the interface accepts any
 * {@link ConfigurationHolder} implementation, this default implementation
 * restricts registration to {@link DefaultConfigurationHolder} instances only.
 *
 * <p>Example usage:
 * <pre>{@code
 * ConfigurationsRegistry registry = new DefaultConfigurationsRegistry();
 * DefaultConfigurationHolder holder = new DefaultConfigurationHolder.Builder(plugin)
 *     .setFileName("config.yml")
 *     .setDataFolder(plugin.getDataFolder())
 *     .build();
 *
 * registry.register("main-config", holder);
 * DefaultConfigurationHolder retrieved = registry.getHolder("main-config");
 * }</pre>
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see ConfigurationsRegistry
 * @see DefaultConfigurationHolder
 */
public class DefaultConfigurationsRegistry implements ConfigurationsRegistry {

  /**
   * Internal thread-safe map storing registered configuration holders.
   * Keys are configuration names, values are the holder instances.
   */
  private final Map<String, DefaultConfigurationHolder> configs = new ConcurrentHashMap<>();

  /**
   * The plugin instance
   */
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
   *
   * <p>This implementation only accepts {@link DefaultConfigurationHolder} instances.
   * If a holder with the same name already exists, it will be replaced.
   *
   * @param name the unique identifier for this configuration holder; must not be null
   * @param holder the configuration holder to register, must be an instance of {@link DefaultConfigurationHolder}
   * @throws IllegalArgumentException if the holder is not an instance of {@link DefaultConfigurationHolder}
   * @throws NullPointerException if name or holder is null
   */
  @Override
  public void register(String name, @NotNull ConfigurationHolder<?> holder) {
    if (!(holder instanceof DefaultConfigurationHolder)) {
      throw new IllegalArgumentException("Holder must be an instance of DefaultConfigurationHolder");
    }
    this.configs.put(name, (DefaultConfigurationHolder) holder);
    this.plugin.getLogger().info("Registered configuration holder '" + name + "'");
  }

  /**
   * Unregisters the configuration holder associated with the specified name.
   *
   * <p>If no holder is registered with the given name, this method does nothing.
   *
   * @param name the unique identifier of the configuration holder to remove
   */
  @Override
  public void unregister(String name) {
    if (!this.configs.containsKey(name)) return;
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
  public boolean isRegistered(String name) {
    return configs.containsKey(name);
  }

  /**
   * Retrieves the configuration holder registered with the specified name.
   *
   * @param name the unique identifier of the configuration holder to retrieve
   * @return the {@link DefaultConfigurationHolder} associated with the given name,
   *         or {@code null} if no holder is registered with that name
   */
  @Override
  public @NotNull DefaultConfigurationHolder getHolder(String name) {
    return configs.get(name);
  }
}
