package com.bestudios.fulcrum.api.command;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link CommandsRegistry} that acts as a registry pattern for plugin commands.
 *
 * <p>This implementation is thread-safe and uses {@link CommandTree} internally to handle command execution
 * and tab completion.</p>
 *
 * <p><strong>Usage Example:</strong></p>
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
 * @since 1.0
 * @see CommandsRegistry
 * @see CommandTree
 */
public class DefaultCommandsRegistry implements CommandsRegistry<CommandTree, CommandWrapper> {

  /**
   * A thread-safe hashmap of registered commands, keyed by the command name.
   */
  private final Map<String, CommandTree> commandsMap = new ConcurrentHashMap<>();

  /**
   * The plugin instance used for command registration with Bukkit.
   * This is required to access the plugin's command registry.
   */
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
   *
   * <p>Returns the complete CommandTree instance for the given command root.</p>
   */
  @Override
  public @Nullable CommandTree getCommandHandler(String commandRoot) {
    return commandsMap.get(commandRoot);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks the internal command map for the presence of the specified command name.</p>
   */
  @Override
  public boolean isRegistered(String commandName) {
    return commandsMap.containsKey(commandName);
  }

  /**
   * Registers a command with the specified name and command tree.
   *
   * <p>This method performs the following operations:</p>
   * <ul>
   *   <li>Validates that the command name and command tree are not null or empty</li>
   *   <li>Stores the command tree in the internal registry</li>
   *   <li>Sets the executor and tab completer in Bukkit's command system</li>
   *   <li>Logs any errors that occur during registration</li>
   * </ul>
   *
   * <p><strong>Important:</strong> The command must be declared in the plugin's
   * plugin.yml file before it can be registered. If the command is not found,
   * this method will return false.</p>
   *
   * @param commandName the name of the command to register (must not be null or empty)
   * @param command the command tree to register (must not be null)
   * @return true if the command was registered successfully, false if validation failed,
   *         the command was not found in plugin.yml, or an exception occurred
   */
  @Override
  public boolean register(String commandName, CommandTree command) {
    if (commandName == null || commandName.isEmpty()) {
      plugin.getLogger().warning("Cannot register command: command name is null or empty");
      return false;
    }
    if (command == null) {
      plugin.getLogger().warning("Cannot register command '" + commandName + "': command tree is null");
      return false;
    }

    try {
      // Register the command in the internal map
      commandsMap.put(commandName, command);

      // Get the Bukkit command and set executor/tab completer
      org.bukkit.command.PluginCommand pluginCommand = plugin.getCommand(commandName);
      if (pluginCommand == null) {
        plugin.getLogger().severe("Command '" + commandName + "' not found in plugin.yml");
        commandsMap.remove(commandName); // Rollback
        return false;
      }

      pluginCommand.setExecutor(command);
      pluginCommand.setTabCompleter(command);
      return true;
    } catch (Exception e) {
      plugin.getLogger().severe("Failed to register command '" + commandName + "': " + e.getMessage());
      e.printStackTrace();
      commandsMap.remove(commandName); // Rollback on error
      return false;
    }
  }

  /**
   * Registers a command with the specified name by building a CommandTree from the given map of subcommands.
   * <p></p>
   * This method constructs a CommandTree using the CommandWrapper instances in the given map.
   *
   * @param commandName the name of the command to register (must not be null or empty)
   * @param subcommands a map where keys are command paths and values are CommandWrapper instances
   * @return true if the command was registered successfully, false if validation failed,
   *         the command was not found in plugin.yml, or an exception occurred
   */
  @Override
  public boolean register(String commandName, Map<String, CommandWrapper> subcommands) {
    // Validate command name
    if (commandName == null || commandName.isEmpty()) {
      plugin.getLogger().warning("Cannot register command: command name is null or empty");
      return false;
    }

    // Validate subcommands map
    if (subcommands == null || subcommands.isEmpty()) {
      plugin.getLogger().warning("Cannot register command '" + commandName + "': subcommands map is null or empty");
      return false;
    }

    try {
      if (isRegistered(commandName)) {
        // Command already registered, attempt to add subcommands to existing tree
        return addSubcommandsToTree(commandName, subcommands);
      } else {
        // Command not registered yet, build a new command tree
        return buildNewCommandTree(commandName, subcommands);
      }
    } catch (IllegalStateException e) {
      plugin.getLogger().severe("Failed to build CommandTree for command '" + commandName + "': " + e.getMessage());
      e.printStackTrace();
      return false;
    } catch (Exception e) {
      plugin.getLogger().severe("Unexpected error while registering command '" + commandName + "': " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  private boolean buildNewCommandTree(String commandName, Map<String, CommandWrapper> subcommands) {
    // Build the command tree from the subcommands map
    CommandTree.Builder builder = new CommandTree.Builder()
            .basePermission(commandName.toLowerCase() + ".use")
            .usageMessage("Usage: /" + commandName + " <subcommand>");

    // Process each CommandWrapper and add to the builder
    for (Map.Entry<String, CommandWrapper> entry : subcommands.entrySet()) {
      String path = entry.getKey();
      CommandWrapper wrapper = entry.getValue();

      if (wrapper == null) {
        plugin.getLogger().warning("Skipping null CommandWrapper for path: " + path);
        continue;
      }

      // Register the command action
      if (wrapper.isPlayerCommand()) {
        // Player-specific command
        CommandTree.PlayerCommandAction playerAction = wrapper.playerCommandAction();
        String permission = wrapper.permission();

        if (permission != null && !permission.isEmpty()) {
          builder.playerCommand(path, playerAction, permission);
        } else {
          builder.playerCommand(path, playerAction);
        }
      } else {
        // General command (for any CommandSender)
        CommandTree.CommandAction action = wrapper.commandAction();
        String permission = wrapper.permission();

        if (action != null) {
          if (permission != null && !permission.isEmpty()) {
            builder.command(path, action, permission);
          } else {
            builder.command(path, action);
          }
        } else {
          plugin.getLogger().warning("CommandWrapper for path '" + path + "' has no action defined");
        }
      }

      // Register tab completer if present
      CommandTree.TabCompleteFunction tabCompleter = wrapper.tabCompleter();
      if (tabCompleter != null) {
        builder.tabCompleter(path, tabCompleter);
      }
    }

    // Build the command tree
    CommandTree commandTree = builder.build();

    // Register using the existing register method
    return register(commandName, commandTree);

  }

  private boolean addSubcommandsToTree(String commandName, Map<String, CommandWrapper> subcommands) {
    CommandTree existingTree = getCommandHandler(commandName);
    if (existingTree == null) {
      plugin.getLogger().severe("Cannot add subcommands: CommandTree for '" + commandName + "' is null");
      return false;
    }
    for (Map.Entry<String, CommandWrapper> entry : subcommands.entrySet()) {
      String path = entry.getKey();
      CommandWrapper wrapper = entry.getValue();

      if (wrapper == null) {
        plugin.getLogger().warning("Skipping null CommandWrapper for path: " + path);
        continue;
      }

      // Register the command action
      if (wrapper.isPlayerCommand()) {
        // Player-specific command
        CommandTree.PlayerCommandAction playerAction = wrapper.playerCommandAction();
        String permission = wrapper.permission();

        if (permission != null && !permission.isEmpty()) {
          existingTree.registerPlayerCommand(path, playerAction, permission);
        } else {
          existingTree.registerPlayerCommand(path, playerAction);
        }
      } else {
        // General command (for any CommandSender)
        CommandTree.CommandAction action = wrapper.commandAction();
        String permission = wrapper.permission();

        if (action != null) {
          if (permission != null && !permission.isEmpty()) {
            existingTree.registerCommand(path, action, permission);
          } else {
            existingTree.registerCommand(path, action);
          }
        } else {
          plugin.getLogger().warning("CommandWrapper for path '" + path + "' has no action defined");
        }
      }

      // Register tab completer if present
      CommandTree.TabCompleteFunction tabCompleter = wrapper.tabCompleter();
      if (tabCompleter != null) {
        existingTree.registerTabCompleter(path, tabCompleter);
      }
    }
    return true;
  }
}
