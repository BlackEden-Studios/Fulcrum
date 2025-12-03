package com.bestudios.fulcrum.api.configuration;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.util.Utils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of {@link ConfigurationHolder} for YAML-based configurations.
 * <p>
 * This class provides built-in support for:
 * <ul>
 *   <li>Automatic configuration loading and reloading</li>
 *   <li>Integration with plugin resource defaults</li>
 *   <li>Version comparison and automatic configuration updates</li>
 * </ul>
 *
 * The configuration file is based on Bukkit's {@link YamlConfiguration} system
 * and automatically ensures the data folder exists before loading configurations.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see ConfigurationHolder
 * @see ConfigurationsRegistry
 */
public class DefaultConfigurationHolder implements ConfigurationHolder<YamlConfiguration> {

  /** The plugin instance. */
  protected final FulcrumPlugin plugin;
  /** The configuration data folder. */
  protected File folder;
  /** The configuration file. */
  protected File  file;
  /** The configuration instance. */
  protected YamlConfiguration config;


  /**
   * Constructs a new {@link DefaultConfigurationHolder} instance.
   *
   * @param pluginRef  The owning plugin instance.
   * @param dataFolder The directory where configuration files are stored.
   * @param configFile The target configuration file.
   */
  public DefaultConfigurationHolder(
          @NotNull FulcrumPlugin pluginRef,
          @NotNull File dataFolder,
          @NotNull File configFile
  ) {
    this.plugin = Objects.requireNonNull(pluginRef, "Plugin reference cannot be null");
    this.folder = Objects.requireNonNull(dataFolder, "Data folder cannot be null");
    this.file   = Objects.requireNonNull(configFile, "Config file cannot be null");

    this.validate(Set.of(this.folder), File::mkdirs, "Failed to create data folder");
    this.validate(Set.of(this.file),   File::createNewFile, "Failed to create config file");

    // Synchronize versions if possible
    if (!synchronizeVersions())
      plugin.getLogger().warning("Failed to update version for " + configFile.getName() + ".");

    // Load the configuration
    this.config = YamlConfiguration.loadConfiguration(configFile);
  }

  @Override
  public YamlConfiguration getConfig() {
    if (config == null) reloadConfig();
    return config;
  }

  @Override
  public boolean reloadConfig() {
    if (file == null || !file.exists()) {
      plugin.getLogger().severe("Could not reload config: " + getConfigName());
      return false;
    }
    config = YamlConfiguration.loadConfiguration(file);
    return true;
  }

  @Override
  public boolean saveConfig() {
    try {
      config.save(file);
      return true;
    } catch (IOException e) {
      plugin.getLogger().severe("Could not save config: " + getConfigName());
      return false;
    }
  }

  @Override
  public String getConfigName() {
    return file.getName();
  }

  @Override
  public boolean configExists() {
    return file.exists();
  }

  @Override
  public File getConfigFile() {
    return file;
  }

  /**
   * Synchronizes the configuration file version with the one retrieved from the plugin's default resources.
   * @return true if the configuration was updated successfully, false otherwise.
   */
  private boolean synchronizeVersions() {
    YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);
    currentConfig.options().parseComments(true);
    String version = plugin.getDescription().getVersion();
    // If the version is up to date, return the configuration
    if (Utils.compareVersions(version, currentConfig.getString("version", version)) >= 0) return true;
    // Otherwise, update the configuration
    plugin.getLogger().warning("Outdated configuration version detected. Updating the configuration file.");
    currentConfig.set("version", version);
    plugin.getLogger().config("Configuration version updated to " + version);
    // Synchronize with defaults resources if available
    currentConfig.setDefaults(Utils.loadFromResources(plugin, file.getPath()));
    currentConfig.options().copyDefaults(true);
    // Save the updated configuration, if possible
    try {
      config.save(file);
      return true;
    } catch (IOException e) {
      plugin.getLogger().severe("Could not save config: " + file.getName());
      return false;
    }
  }
}
