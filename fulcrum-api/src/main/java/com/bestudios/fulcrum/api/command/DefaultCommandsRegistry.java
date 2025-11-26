package com.bestudios.fulcrum.api.command;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link CommandsRegistry} that acts as a registry pattern for plugin commands.
 * <p>
 * This implementation is thread-safe and uses {@link CommandTree} internally to handle command execution
 * and tab completion.
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * DefaultCommandsRegistry registry = new DefaultCommandsRegistry(plugin);
 * CommandTree commandTree = new CommandTree.Builder()
 *                                          .command("help", context -> {
 *                                            context.sender().sendMessage("Help message");
 *                                            return true;
 *                                          })
 *                                          .build();
 * registry.register("mycommand", commandTree);
 * }</pre>
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see CommandsRegistry
 * @see CommandTree
 */
public class DefaultCommandsRegistry implements CommandsRegistry<CommandTree, CommandWrapper> {

  /** A thread-safe hashmap of registered commands, keyed by the command name */
  private final Map<String, CommandTree> commandsMap = new ConcurrentHashMap<>();

  /** The plugin instance used for command registration with Bukkit */
  private final JavaPlugin plugin;

  /**
   * Creates a new DefaultCommandsRegistry instance.
   *
   * @param plugin the JavaPlugin instance that owns this registry (must not be null)
   * @throws NullPointerException if plugin instance is null
   */
  public DefaultCommandsRegistry(JavaPlugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
  }

  /**
   * {@inheritDoc}
   * <p>
   * Returns the complete CommandTree instance for the given command root
   */
  @Override
  public @Nullable CommandTree getCommandHandler(String commandRoot) {
    return commandsMap.get(commandRoot);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Checks the internal command map for the presence of the specified command name
   */
  @Override
  public boolean isRegistered(String commandName) {
    return commandsMap.containsKey(commandName);
  }

  /**
   * Registers a command with the specified name and command tree.
   * <p>
   * This method performs the following operations:
   * <ul>
   *   <li>Validates that the command name and command tree are not null or empty</li>
   *   <li>Stores the command tree in the internal registry</li>
   *   <li>Sets the executor and tab completer in Bukkit's command system</li>
   *   <li>Logs any errors that occur during registration</li>
   * </ul>
   *
   * <p>
   * <b>Important:</b>
   * <p>
   * The command must be declared in the plugin's
   * plugin.yml file before it can be registered. If the command is not found,
   * this method will return false.
   *
   * @param commandName the name of the command to register (must not be null or empty)
   * @param command the command tree to register (must not be null)
   * @return true if the command was registered successfully; false if validation failed,
   *         the command was not found in plugin.yml, or an exception occurred
   */
  @Override
  public boolean register(@NotNull String commandName, @NotNull CommandTree command) {
    // Validate command name
    if (commandName.isBlank()) {
      this.sendWarningForEmpty(commandName, "command name");
      return false;
    }

    try {
      // Register the command in the internal map
      commandsMap.put(commandName, command);

      // Get the Bukkit command
      PluginCommand pluginCommand = plugin.getCommand(commandName);
      // If the command is null, the command was not found in plugin.yml
      Objects.requireNonNull(pluginCommand,"Command '" + commandName + "' not found in plugin.yml");

      pluginCommand.setExecutor(command);
      pluginCommand.setTabCompleter(command);
      return true;

    } catch (Exception e) {
      plugin.getLogger().severe("Failed to register command '" + commandName + "': " + e.getMessage() +
                                "\n" + Arrays.toString(e.getStackTrace()));
      commandsMap.remove(commandName); // Rollback on error
      return false;
    }
  }

  /**
   * Registers a command with the specified name by building a CommandTree from the given map of subcommands.
   * <p>
   * This method constructs a CommandTree using the CommandWrapper instances in the given map.
   *
   * @param commandName the name of the command to register (must not be null or empty)
   * @param subcommands a map where keys are command paths and values are CommandWrapper instances
   * @return true if the command was registered successfully, false if validation failed,
   *         the command was not found in plugin.yml, or an exception occurred
   */
  @Override
  public boolean register(@NotNull String commandName, @NotNull Map<String, CommandWrapper> subcommands) {
    // Validate command name
    if (commandName.isBlank()) {
      sendWarningForEmpty(commandName, "command name");
      return false;
    }

    // Validate subcommands map
    if (subcommands.isEmpty()) {
      sendWarningForEmpty(commandName, "subcommands map");
      return false;
    }

    try {
      return populateTree(commandName, subcommands);

    } catch (Exception e) {
      plugin.getLogger().severe("Failed to build CommandTree for command '" + commandName + "': " + e.getMessage() +
                                "\n" + Arrays.toString(e.getStackTrace()));
      return false;
    }
  }

  /**
   * Populates a CommandTree from the given map of subcommands or creates a new CommandTree if none exists.
   * @param commandName The name of the command to register
   * @param subcommands A map where keys are command paths and values are CommandWrapper instances.
   * @return true if the command was populated successfully, false otherwise.
   */
  private boolean populateTree(@NotNull String commandName, @NotNull Map<String, CommandWrapper> subcommands) {
    CommandTree tree = getCommandHandler(commandName) != null ?
                       // Use the existing tree
                       getCommandHandler(commandName) :
                       // Create a new tree
                       new CommandTree.Builder()
                                      .basePermission(commandName.toLowerCase() + ".use")
                                      .usageMessage("Usage: /" + commandName + " <subcommand>")
                                      .build();
    // Validate tree
    Objects.requireNonNull(tree, "CommandTree cannot be null");
    // Process each CommandWrapper and add to the tree
    for (Map.Entry<String, CommandWrapper> entry : subcommands.entrySet()) {
      // Skip empty paths
      String path = entry.getKey();
      if (path.isBlank()) continue;
      // Skip null CommandWrappers
      CommandWrapper wrapper = entry.getValue();
      if (wrapper == null) continue;
      // Register the command
      tree.registerFromCommandWrapper(wrapper);
    }

    // Register the tree
    return register(commandName, tree);

  }

  /**
   * Logs a warning message indicating that the given command name is empty.
   * @param commandName The name of the command that is empty.
   * @param type        The type of the empty value (e.g., command name, subcommand map).
   */
  private void sendWarningForEmpty(String commandName, String type) {
    plugin.getLogger().warning("Cannot register command '" + commandName + "': " + type + " is empty");
  }
}
