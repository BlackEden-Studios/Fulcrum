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
import com.bestudios.fulcrum.api.util.Lock;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.imageio.spi.ServiceRegistry;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Abstract base class for Fulcrum-based Paper plugins providing core functionality
 * for command registration, configuration management, and debug mode support.
 *
 * <p>This class extends {@link JavaPlugin} and implements {@link FulcrumPlugin},
 * serving as the foundation for all Fulcrum framework plugins. It automatically
 * initializes and manages registries for commands and configurations, providing
 * a streamlined API for plugin development.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>Automatic registration of base configuration (config.yml)</li>
 *   <li>Multi-language support with configurable language files</li>
 *   <li>Built-in debug command for runtime debugging</li>
 *   <li>Simple API for child plugins to register custom commands</li>
 * </ul>
 *
 * <p>Child plugins should override {@link #registerAdditionalCommands()} and {@link #additionalInitializationTasks()} to perform custom initialization
 * tasks such as registering event listeners, scheduling tasks, or setting up
 * additional configurations. Always call {@code super.onEnable()} to ensure
 * proper initialization of the base functionality.</p>
 *
 * <p>Same goes for {@link #additionalTerminationTasks()} in {@link #onDisable()} to handle any cleanup tasks.</p>
 *
 * <p>The default configuration structure expects:</p>
 * <ul>
 *   <li>A {@code config.yml} file in the plugin's data folder</li>
 *   <li>A {@code language} key in config.yml specifying the language code (defaults to "en")</li>
 *   <li>A corresponding language file (e.g., {@code en.yml}) in the data folder</li>
 * </ul>
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see CommandsRegistry
 * @see ConfigurationsRegistry
 */
public abstract class FulcrumPlugin extends JavaPlugin{

  /** Default language id code used if none is specified in the configuration. */
  private final String DEFAULT_LANGUAGE = "en";

  /** Registry for commands*/
  private CommandsRegistry<CommandTree, CommandWrapper> commandsRegistry;
  /** Registry for configurations*/
  private ConfigurationsRegistry configurationsRegistry;
  /** Service registry */
  private ServiceRegistry servicesRegistry;
  /** Debug mode flag */
  private boolean debugMode = false;

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
   */
  private void initializeRegistries() {
    this.commandsRegistry = new DefaultCommandsRegistry(this);
    this.configurationsRegistry = new DefaultConfigurationsRegistry(this);
  }

  /**
   * Registers the base configuration file (config.yml) with the ConfigurationsRegistry.
   */
  private void registerBaseConfiguration() {
    ConfigurationHolder<YamlConfiguration> configHolder = new DefaultConfigurationHolder.Builder(this)
            .setFileName(configurationFileName("config"))
            .setDataFolder(getDataFolder())
            .build();

    if (configHolder != null) {
      configurationsRegistry.register("config", configHolder);
      getLogger().info("Base configuration (config.yml) registered successfully.");
    } else {
      getLogger().severe("Failed to register base configuration!");
    }
  }

  /**
   * Registers the language configuration file with the ConfigurationsRegistry.
   * The language file name is determined from the base config.yml file.
   */
  private void registerLanguageConfiguration() {
    // Get the language file name from config.yml (default to "en.yml")
    ConfigurationHolder<?> configHolder = configurationsRegistry.getHolder("config");
    // Define the language file name
    String languageFileName = configHolder.getConfig() != null ?
            configurationFileName(configHolder.getConfig().getString( "language", DEFAULT_LANGUAGE)) :
            configurationFileName(DEFAULT_LANGUAGE);
    // Define the language folder
    File languageFolder = new File(getDataFolder(), "languages");
    // Create the language configuration holder
    DefaultConfigurationHolder langHolder = new DefaultConfigurationHolder.Builder(this)
            .setFileName(languageFileName)
            .setDataFolder(languageFolder)
            .build();

    // Register the language configuration holder
    if (langHolder != null) {
      configurationsRegistry.register("language", langHolder);
      getLogger().info("Language configuration (" + languageFileName + ") registered successfully.");
    } else {
      getLogger().severe("Failed to register language configuration!");
    }
  }

  /**
   * Registers the debug command for toggling debug mode.
   * Command format: /{plugin_name} debug
   */
  private void registerDebugCommand() {
    String pluginName = getName().toLowerCase();

    // Create the debug command wrapper
    CommandWrapper debugCommand = new CommandWrapper.Builder()
            .path("debug")
            .action(context -> {
              toggleDebugMode();
              context.sender().sendMessage("§aDebug mode " + (isDebugMode() ? "enabled" : "disabled") + ".");
              return true;
            })
            .build();

    // Create a map containing the debug command for the root *plugin_name*
    Map<String, CommandWrapper> commands = new HashMap<>();
    commands.put("debug", debugCommand);

    // Register the command with the CommandsRegistry
    boolean registered = commandsRegistry.register(pluginName, commands);

    if (registered) {
      getLogger().info("Debug command registered successfully.");
    } else {
      getLogger().warning("Failed to register debug command.");
    }
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
    if (commandName.isEmpty() || commands.isEmpty()) {
      getLogger().warning("Cannot register commands: command name or commands map is empty.");
      return false;
    }
    if (!(commandsRegistry instanceof DefaultCommandsRegistry)) {
      getLogger().severe("CommandsRegistry is not an instance of DefaultCommandsRegistry. Cannot register commands.");
      return false;
    }

    return commandsRegistry.register(commandName, commands);
  }

  public void setDebugMode(boolean debugMode) {
    this.debugMode = debugMode;
    getLogger().setLevel(debugMode ? Level.CONFIG : Level.INFO);
  }

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

  public ConfigurationsRegistry getConfigurationsRegistry() {
    return configurationsRegistry;
  }

  public FileConfiguration getLanguageConfiguration() {
    ConfigurationHolder<?> langHolder = configurationsRegistry.getHolder("language");
    return langHolder.getConfig();
  }

  public abstract DatabaseGateway getDatabaseGateway();

}
