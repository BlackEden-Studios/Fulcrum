package com.bestudios.fulcrum.api.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Central registry for plugin commands.
 *
 * <p>Implementations should handle command registration and provide
 * access to CommandExecutor and TabCompleter instances for Paper integration.</p>
 *
 * <p>This interface uses generics to ensure type safety while maintaining flexibility
 * for different command handler implementations. The generic type parameter must implement
 * both {@link CommandExecutor} and {@link TabCompleter} interfaces.</p>
 *
 * @param <T> the type of command handler that implements both CommandExecutor and TabCompleter
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see CommandExecutor
 * @see TabCompleter
 * @see CommandTree
 * @see CommandWrapper
 */
public interface CommandsRegistry<T extends CommandExecutor & TabCompleter, W extends CommandWrapper> {

  /**
   * Gets the CommandHandler for handling both command execution and tab completion.
   *
   * <p>This is a convenience method that returns the complete command handler
   * which implements both {@link CommandExecutor} and {@link TabCompleter} interfaces.
   * This allows access to the full handler object rather than just individual interfaces.</p>
   *
   * @param commandRoot the root command name for command execution and tab completion
   * @return the CommandHandler instance of type T, or null if no handler is registered
   */
  @Nullable
  T getCommandHandler(String commandRoot);

  /**
   * Registers a command handler with a Bukkit command.
   *
   * <p>This method associates the provided command handler with the specified command name
   * in the Bukkit plugin system. The command must be defined in the plugin's plugin.yml
   * configuration file before it can be registered.</p>
   *
   * @param commandName the name of the command to register (must match plugin.yml)
   * @param command the command handler to register (must not be null)
   * @return true if registration was successful, false otherwise (e.g., command not found in plugin.yml,
   *         null parameters, or registration failure)
   * @throws NullPointerException if commandName or command is null (implementation-dependent)
   */
  boolean register(String commandName, T command);

  /**
   * Registers multiple command wrappers under a single command name.
   *
   * <p>This method allows registering a set of command wrappers associated with
   * a specific command name. Each wrapper can represent a sub-command or functionality
   * under the main command.</p>
   *
   * @param commandName the root command name to register the wrappers under
   * @param subcommands a map of command paths to their corresponding CommandWrapper instances
   * @return true if registration was successful, false otherwise
   */
  boolean register(String commandName, Map<String, W> subcommands);

  /**
   * Checks if a command is registered with this registry.
   *
   * <p>This method verifies whether a command handler has been registered for
   * the specified command name. This is useful for preventing duplicate registrations
   * or checking command availability before attempting operations.</p>
   *
   * @param commandName the command name to check
   * @return true if the command is registered, false otherwise
   */
  boolean isRegistered(String commandName);
}
