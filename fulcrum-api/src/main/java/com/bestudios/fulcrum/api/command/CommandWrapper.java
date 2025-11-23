package com.bestudios.fulcrum.api.command;

import com.bestudios.fulcrum.api.command.CommandTree.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper class to encapsulate command metadata in a Minecraft plugin.
  *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see CommandTree
 */
public record CommandWrapper(
        String commandPath,
        CommandAction commandAction,
        PlayerCommandAction playerCommandAction,
        TabCompleteFunction tabCompleter,
        String permission
) {

  /**
   * Private constructor - use Builder pattern for instantiation
   */
  public CommandWrapper {
  }

  /**
   * Gets the command path (e.g., "command subcommand")
   */
  @Override
  @NotNull
  public String commandPath() {
    return commandPath;
  }

  /**
   * Gets the general command action (for any CommandSender)
   */
  @Override
  @Nullable
  public CommandAction commandAction() {
    return commandAction;
  }

  /**
   * Gets the player-specific command action
   */
  @Override
  @Nullable
  public PlayerCommandAction playerCommandAction() {
    return playerCommandAction;
  }

  /**
   * Gets the tab completion function
   */
  @Override
  @Nullable
  public TabCompleteFunction tabCompleter() {
    return tabCompleter;
  }

  /**
   * Gets the permission required for this command
   */
  @Override
  @Nullable
  public String permission() {
    return permission;
  }

  /**
   * Checks if this wrapper has a player-specific command
   */
  public boolean isPlayerCommand() {
    return playerCommandAction != null;
  }

  /**
   * Builder class for creating CommandWrapper instances
   */
  public static class Builder {
    private String commandPath;
    private CommandAction commandAction;
    private PlayerCommandAction playerCommandAction;
    private TabCompleteFunction tabCompleter;
    private String permission;

    /**
     * Sets the command path (required)
     *
     * @param path The command path, e.g., "help" or "admin reload"
     */
    public Builder path(@NotNull String path) {
      if (path.trim().isEmpty()) {
        throw new IllegalArgumentException("Command path cannot be null or empty");
      }
      this.commandPath = path;
      return this;
    }

    /**
     * Sets the general command action
     *
     * @param action The action to execute for any CommandSender
     */
    public Builder action(@NotNull CommandAction action) {
      this.playerCommandAction = null;
      this.commandAction = action;
      return this;
    }

    /**
     * Sets the player-specific command action
     *
     * @param action The action to execute for Player senders only
     */
    public Builder playerAction(@NotNull PlayerCommandAction action) {
      this.commandAction = null;
      this.playerCommandAction = action;
      return this;
    }

    /**
     * Sets the tab completion function
     *
     * @param completer The function to provide tab completions
     */
    public Builder tabCompleter(@Nullable TabCompleteFunction completer) {
      this.tabCompleter = completer;
      return this;
    }

    /**
     * Sets the permission required for this command
     *
     * @param permission The permission string
     */
    public Builder permission(@Nullable String permission) {
      this.permission = permission;
      return this;
    }

    /**
     * Builds the CommandWrapper instance
     */
    public CommandWrapper build() {
      if (commandPath == null || commandPath.trim().isEmpty()) {
        throw new IllegalStateException("Command path must be set");
      }

      if (commandAction == null && playerCommandAction == null) {
        throw new IllegalStateException("Either command action or player command action must be set");
      }

      return new CommandWrapper(commandPath, commandAction, playerCommandAction, tabCompleter, permission);
    }
  }
}

