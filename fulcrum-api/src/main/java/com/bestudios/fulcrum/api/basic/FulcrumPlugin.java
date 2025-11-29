package com.bestudios.fulcrum.api.basic;

import com.bestudios.fulcrum.api.command.CommandTree;
import com.bestudios.fulcrum.api.command.CommandsRegistry;
import com.bestudios.fulcrum.api.command.CommandWrapper;
import com.bestudios.fulcrum.api.command.DefaultCommandsRegistry;
import com.bestudios.fulcrum.api.configuration.ConfigurationHolder;
import com.bestudios.fulcrum.api.configuration.ConfigurationsRegistry;
import com.bestudios.fulcrum.api.configuration.DefaultConfigurationHolder;
import com.bestudios.fulcrum.api.configuration.DefaultConfigurationsRegistry;
import com.bestudios.fulcrum.api.database.DatabaseGateway;
import com.bestudios.fulcrum.api.service.ServicesRegistry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Abstract base class for Fulcrum-based Paper plugins providing core functionality
 * for command registration, configuration management, and debug mode support.
 * <p>
 * This class extends {@link JavaPlugin} and implements {@link FulcrumPlugin},
 * serving as the foundation for all Fulcrum framework plugins. It automatically
 * initializes and manages registries for commands and configurations, providing
 * a streamlined API for plugin development.
 * <p>
 * Features include:
 * <ul>
 *   <li>Automatic registration of base configuration (config.yml)</li>
 *   <li>Multi-language support with configurable language files</li>
 *   <li>Built-in debug command for runtime debugging</li>
 *   <li>Simple API for child plugins to register custom commands</li>
 * </ul>
 * <p>
 * Child plugins should override {@link #registerAdditionalCommands()} and {@link #additionalInitializationTasks()} to perform custom initialization
 * tasks such as registering event listeners, scheduling tasks, or setting up
 * additional configurations. Always call {@code super.onEnable()} to ensure
 * proper initialization of the base functionality.
 * <p>
 * The same goes for {@link #additionalTerminationTasks()} in {@link #onDisable()} to handle any cleanup tasks.
 * <p>
 * The default configuration structure expects:
 * <ul>
 *   <li>A {@code config.yml} file in the plugin's data folder</li>
 *   <li>A {@code language} key in config.yml specifying the language code (defaults to "en")</li>
 *   <li>A corresponding language file (e.g., {@code en.yml}) in the data folder</li>
 * </ul>
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see CommandsRegistry
 * @see ConfigurationsRegistry
 */
public abstract class FulcrumPlugin extends JavaPlugin{

  /** Default language id code used if none is specified in the configuration. */
  private static final String DEFAULT_LANGUAGE = "en";

  /** Registry for commands*/
  private CommandsRegistry<CommandTree, CommandWrapper> commandsRegistry;
  /** Registry for configurations*/
  private ConfigurationsRegistry<YamlConfiguration> configurationsRegistry;
  /** Service registry */
  protected ServicesRegistry servicesRegistry;
  /** Debug mode flag */
  private boolean debugMode;

  /**
   * This method is called when the plugin is enabled.
   * Child plugins can override this method to perform initialization tasks.
   * For example, setting up configurations or registering event listeners.
   *
   * @see FulcrumPlugin#onDisable()
   */
  @Override
  public void onEnable() {
    // Show the plugin title in the console
    showPluginTitle();

    // Initialize registries
    initializeRegistries();

    // Initialize base configuration (config.yml)
    registerBaseConfiguration();

    // Initialize language configuration
    registerLanguageConfiguration();

    // Register debug command
    registerDebugCommand();

    // Register other commands if needed
    registerAdditionalCommands();

    // Additional initialization tasks can be performed by child classes here
    additionalInitializationTasks();

    getLogger().info("Plugin initialized successfully.");
  }

  @Override
  public void onDisable() {
    // Perform any additional termination tasks needed by child classes
    additionalTerminationTasks();

    getLogger().info("Plugin disabled.");
  }

  /** Allows child classes to show the plugin title in the console */
  abstract protected void showPluginTitle();
  /** Allows child classes to register additional commands */
  abstract protected void registerAdditionalCommands();
  /** Allows child classes to perform additional initialization tasks */
  abstract protected void additionalInitializationTasks();
  /** Allows child classes to perform additional termination tasks */
  abstract protected void additionalTerminationTasks();

  /**
   * Initializes the registries for the plugin.
   * This method is called during plugin initialization.
   * <p>
   * Can be overridden to add additional registries
   */
  protected void initializeRegistries() {
    this.commandsRegistry       = new DefaultCommandsRegistry(this);
    this.configurationsRegistry = new DefaultConfigurationsRegistry(this);
  }

  /**
   * Registers the base configuration file (config.yml) with the ConfigurationsRegistry.
   */
  private void registerBaseConfiguration() {
    configurationsRegistry.register(
      "config",
      new DefaultConfigurationHolder(this, getDataFolder(), new File(getDataFolder(), configurationFileName("config")))
    );
    getLogger().info("Base configuration (config.yml) registered successfully.");
  }

  /**
   * Registers the language configuration file with the ConfigurationsRegistry.
   * The language file name is determined from the base config.yml file.
   */
  private void registerLanguageConfiguration() {
    // Get the language file name from config.yml (default to "en.yml")
    ConfigurationHolder<?> configHolder = configurationsRegistry.getHolder("config");
    assert configHolder != null;
    // Define the language folder
    File languageFolder = new File(getDataFolder(), "languages");
    // Define the language file
    String languageFileName = configHolder.getConfig() != null ?
            configurationFileName(configHolder.getConfig().getString( "language", DEFAULT_LANGUAGE)) :
            configurationFileName(DEFAULT_LANGUAGE);
    File languageFile = new File(languageFolder, languageFileName);

    // Register the language configuration holder
    configurationsRegistry.register(
            "language",
            new DefaultConfigurationHolder(this, languageFolder, languageFile)
    );
  }

  /**
   * Registers the debug command for toggling debug mode.
   * Command format: /{plugin_name} debug
   */
  private void registerDebugCommand() {

    // Create the debug command wrapper
    CommandWrapper debugCommand = new CommandWrapper.Builder()
            .path("debug")
            .action(context -> {
              toggleDebugMode();
              context.sender().sendMessage("§aDebug mode " + (isDebugMode() ? "enabled" : "disabled") + ".");
              return true;
            })
            .build();

    // Register the command with the CommandsRegistry
    if (commandsRegistry.register(this.getName().toLowerCase(), Map.of("debug", debugCommand)))
      getLogger().info("Debug command registered successfully.");
    else getLogger().warning("Failed to register debug command.");
  }

  /**
   * Returns the configuration file name for the given name.
   * @param name the name of the configuration (e.g., "config", "language")
   * @return the configuration file name for the given name
   */
  private String configurationFileName(String name) {
    return name + ".yml";
  }

  /**
   * Allows child classes to register custom commands using CommandWrapper instances.
   *
   * @param commandName the root command name (must be defined in plugin.yml)
   * @param commands a map of command paths to CommandWrapper instances
   * @return true if registration was successful, false otherwise
   */
  protected boolean registerCommands(@NotNull String commandName, @NotNull Map<String, CommandWrapper> commands) {
    // Validate command name and commands map
    if (commandName.isEmpty() || commands.isEmpty()) {
      getLogger().warning("Cannot register commands: command name or commands map is empty.");
      return false;
    }
    // Register the commands
    return commandsRegistry.register(commandName, commands);
  }

  /**
   * Sets the debug mode flag.
   * @param debugMode true to enable debug mode, false to disable
   */
  public void setDebugMode(boolean debugMode) {
    this.debugMode = debugMode;
    getLogger().setLevel(debugMode ? Level.CONFIG : Level.INFO);
  }

  /**
   * Toggles the debug mode flag.
   */
  public void toggleDebugMode() {
    setDebugMode(!debugMode);
  }

  /**
   * Checks if debug mode is currently enabled.
   *
   * @return true if debug mode is enabled, false otherwise
   */
  public boolean isDebugMode() {
    return debugMode;
  }

  public CommandsRegistry<CommandTree, CommandWrapper> getCommandsRegistry() {
    return commandsRegistry;
  }

  public ConfigurationsRegistry<YamlConfiguration> getConfigurationsRegistry() {
    return configurationsRegistry;
  }

  public FileConfiguration getLanguageConfiguration() {
    ConfigurationHolder<?> langHolder = configurationsRegistry.getHolder("language");
    assert langHolder != null;
    return langHolder.getConfig();
  }

  public abstract DatabaseGateway getDatabaseGateway();

}
