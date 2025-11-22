package com.bestudios.fulcrum.api.configuration;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.util.Utils;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Default implementation of {@link ConfigurationHolder} for YAML-based configurations.
 * <p>
 * This class provides built-in support for:
 * <ul>
 *   <li>Automatic configuration loading and reloading</li>
 *   <li>Integration with plugin resource defaults</li>
 *   <li>Version comparison and automatic configuration updates</li>
 *   <li>Builder-based instantiation using a fluent API</li>
 * </ul>
 *
 * The configuration file is based on Bukkit's {@link YamlConfiguration} system
 * and automatically ensures the data folder exists before loading configurations.
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * DefaultConfigurationHolder holder = new DefaultConfigurationHolder.Builder(plugin)
 *     .setFileName("config.yml")
 *     .setDataFolder(plugin.getDataFolder())
 *     .build();
 *
 * YamlConfiguration config = holder.getConfig();
 * config.getString("database.username");
 * }</pre>
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see ConfigurationHolder
 * @see ConfigurationsRegistry
 */
public class DefaultConfigurationHolder implements ConfigurationHolder<YamlConfiguration> {

  protected final FulcrumPlugin plugin;
  protected YamlConfiguration config;
  protected File configFile;
  protected File dataFolder;

  /**
   * Private constructor used internally by the {@link Builder}.
   *
   * @param plugin     The owning plugin instance.
   * @param dataFolder The directory where configuration files are stored.
   * @param configFile The target configuration file.
   */
  private DefaultConfigurationHolder(FulcrumPlugin plugin, File dataFolder, File configFile) {
    this.plugin = plugin;
    this.dataFolder = dataFolder;
    this.configFile = configFile;
    this.config = YamlConfiguration.loadConfiguration(configFile);
  }

  /**
   * Factory-style builder for creating instances of {@link DefaultConfigurationHolder}.
   */
  public static class Builder {

    private final FulcrumPlugin plugin;
    private File dataFolder;
    private String fileName;

    /**
     * Constructs a new {@link Builder} for constructing {@link DefaultConfigurationHolder} objects.
     *
     * @param plugin The plugin associated with the configuration file.
     *               Must not be {@code null}.
     */
    public Builder(FulcrumPlugin plugin) {
      this.plugin = plugin;
      this.dataFolder = plugin.getDataFolder();
    }

    /**
     * Defines the data folder where the configuration file will be stored.
     *
     * @param dataFolder A {@link File} object representing the target directory.
     * @return The current builder instance for method chaining.
     */
    public Builder setDataFolder(File dataFolder) {
      this.dataFolder = dataFolder;
      return this;
    }

    /**
     * Defines the data folder path as a {@link String}.
     *
     * @param dataFolderPath The path to the data folder (either relative or absolute).
     * @return The current builder instance for chaining.
     */
    public Builder setDataFolder(String dataFolderPath) {
      return setDataFolder(new File(dataFolderPath));
    }

    /**
     * Sets the configuration filename (e.g., {@code "config.yml"}).
     *
     * @param fileName The configuration filename.
     * @return The current builder instance for method chaining.
     */
    public Builder setFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    /**
     * Finalizes setup and builds a fully-initialized {@link DefaultConfigurationHolder} instance.
     * <p>
     * The method performs validation, creates missing directories, injects defaults from plugin
     * resources, and ensures outdated configurations are updated based on version comparison.
     * </p>
     *
     * @return A fully initialized {@link DefaultConfigurationHolder} or {@code null} if initialization fails.
     * @throws IllegalStateException If the required parameters are not properly configured.
     */
    public DefaultConfigurationHolder build() {
      if (plugin == null)
        throw new IllegalStateException("Plugin reference cannot be null");

      if (fileName == null || fileName.trim().isEmpty())
        throw new IllegalStateException("File name cannot be null or empty");

      if (dataFolder == null)
        throw new IllegalStateException("Data folder cannot be null");

      ensureDataFolderExists(dataFolder);

      File configFile = new File(dataFolder, fileName);
      YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
      YamlConfiguration defaultConfig = loadDefaultConfigFromResources(fileName);

      // Copy defaults if found inside resource JAR
      if (defaultConfig != null) {
        config.setDefaults(defaultConfig);
        config.options().copyDefaults(true);
      }

      String version = plugin.getDescription().getVersion();
      // Automatic version synchronization
      if (Utils.compareVersions(version, config.getString("version", version)) > 0) {
        plugin.getLogger().warning("Outdated configuration version detected. Updating the configuration file.");
        config.set("version", version);
        plugin.getLogger().config("Configuration version updated to " + version);
      }

      try {
        config.save(configFile);
      } catch (IOException e) {
        plugin.getLogger().severe("Failed to save configuration: " + configFile.getPath());
        return null;
      }

      return new DefaultConfigurationHolder(plugin, dataFolder, configFile);
    }

    /**
     * Ensures that the plugin data folder exists.
     *
     * @param dataFolder The directory to check or create.
     * @throws RuntimeException If the directory cannot be created.
     */
    private void ensureDataFolderExists(File dataFolder) {
      if (!dataFolder.exists() && !dataFolder.mkdirs()) {
        throw new RuntimeException("Failed to create plugin data folder: " + dataFolder.getPath());
      }
    }

    /**
     * Loads a default YAML configuration from within the plugin JAR resources.
     *
     * @param resourcePath The path to the resource relative to the root of the plugin package.
     * @return The {@link YamlConfiguration} loaded from the resource, or {@code null} if not found.
     */
    private YamlConfiguration loadDefaultConfigFromResources(String resourcePath) {
      InputStream stream = plugin.getResource(resourcePath);
      if (stream == null) {
        plugin.getLogger().severe("Could not find resource: " + resourcePath);
        return null;
      }

      Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
      return YamlConfiguration.loadConfiguration(reader);
    }
  }

  @Override
  public YamlConfiguration getConfig() {
    if (config == null) config = YamlConfiguration.loadConfiguration(configFile);
    return config;
  }

  @Override
  public boolean reloadConfig() {
    if (configFile == null || !configFile.exists()) {
      plugin.getLogger().severe("Could not reload config: " + getConfigName());
      return false;
    }
    config = YamlConfiguration.loadConfiguration(configFile);
    return true;
  }

  @Override
  public boolean saveConfig() {
    try {
      config.save(configFile);
      return true;
    } catch (IOException e) {
      plugin.getLogger().severe("Could not save config: " + getConfigName());
      return false;
    }
  }

  @Override
  public String getConfigName() {
    return configFile.getName();
  }

  @Override
  public boolean configExists() {
    return configFile.exists();
  }

  @Override
  public File getConfigFile() {
    return configFile;
  }
}
