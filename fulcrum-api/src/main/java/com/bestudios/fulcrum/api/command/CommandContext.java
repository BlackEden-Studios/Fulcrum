package com.bestudios.fulcrum.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Context for command execution, encapsulating sender, command, subcommands, and remaining arguments.
 * This allows commands to access the sender, command name, full path, and remaining arguments easily.
 *
 * @param sender        The command sender (e.g., player or console)
 * @param command       The command being executed (subcommands + remainingArgs)
 * @param subcommands   The subcommands used in the command path (e.g., "sub1 sub2" in "root sub1 sub2")
 * @param remainingArgs The remaining arguments after the command path (e.g., "remaining" in "root subs remaining")
 */
public record CommandContext(CommandSender sender, Command command, String[] subcommands, String[] remainingArgs) {

  /**
   * Check if the command context has subcommands.
   *
   * @return true if the command context has subcommands, false otherwise
   */
  @Contract(pure = true)
  public boolean hasSubcommands() {
    return subcommands.length > 0;
  }

  @Contract(pure = true)
  public @Nullable String getSubcommand(int index) {
    return index < subcommands.length && index > 0 ? subcommands[index-1] : null;
  }

  /**
   * Checks if there are any remaining arguments after the subcommands path.
   *
   * @return true if there are remaining arguments, false otherwise
   */
  @Contract(pure = true)
  public boolean hasArgs() {
    return remainingArgs.length > 0;
  }

  /**
   * Gets the argument at the specified index from the remaining arguments.
   *
   * @param index the index of the argument to retrieve
   * @return the argument at the specified index, or null if the index is out of bounds
   */
  @Contract(pure = true)
  public @Nullable String getArg(int index) {
    return index < remainingArgs.length && index > 0 ? remainingArgs[index-1] : null;
  }

  /**
   * Gets the argument at the specified index from the remaining arguments or returns a default value if OutOfBounds.
   *
   * @param index        the index of the argument to retrieve
   * @param defaultValue the default value to return if the index is out of bounds
   * @return the argument at the specified index, or the default value if out of bounds
   */
  @Contract(pure = true)
  public String getArg(int index, String defaultValue) {
    return index < remainingArgs.length ? remainingArgs[index] : defaultValue;
  }

  /**
   * Checks if the command sender is a player.
   *
   * @return true if the sender is a player, false otherwise
   */
  @Contract(pure = true)
  public boolean isPlayer() {
    return sender instanceof Player;
  }

  /**
   * Gets the player if the command sender is a player, otherwise returns null.
   *
   * @return the player if the sender is a player, null otherwise
   */
  @Contract(pure = true)
  public @Nullable Player getPlayer() {
    return isPlayer() ? (Player) sender : null;
  }
}