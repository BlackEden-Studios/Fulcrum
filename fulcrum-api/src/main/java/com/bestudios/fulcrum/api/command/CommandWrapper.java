package com.bestudios.fulcrum.api.command;

import com.bestudios.fulcrum.api.command.CommandTree.CommandAction;
import com.bestudios.fulcrum.api.command.CommandTree.PlayerCommandAction;
import com.bestudios.fulcrum.api.command.CommandTree.TabCompleteFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper class to encapsulate command metadata in a Minecraft plugin.
  *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
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
   * Checks if this wrapper has a player-specific command
   */
  public boolean isPlayerCommand() {
    return playerCommandAction != null;
  }

  /**
   * Builder class for creating CommandWrapper instances
   */
  public static class Builder {

    /** The command path */
    private String builderCommandPath;
    /** The action executed by performing this command path */
    private CommandAction builderCommandAction;
    /** The action executed by performing this command path on a player */
    private PlayerCommandAction builderPlayerCommandAction;
    /** The function used to provide tab completions */
    private TabCompleteFunction builderTabCompleter;
    /** The permission required to execute this command */
    private String builderPermission;

    /**
     * Sets the command path (required)
     *
     * @param path The command path, e.g., "help" or "admin reload"
     */
    public Builder path(@NotNull String path) {
      if (path.isBlank())
        throw new IllegalArgumentException("Command path cannot be null or empty");
      this.builderCommandPath = path;
      return this;
    }

    /**
     * Sets the general command action
     *
     * @param action The action to execute for any CommandSender
     */
    public Builder action(@NotNull CommandAction action) {
      this.builderCommandAction = action;
      return this;
    }

    /**
     * Sets the player-specific command action
     *
     * @param action The action to execute for Player senders only
     */
    public Builder playerAction(@NotNull PlayerCommandAction action) {
      this.builderPlayerCommandAction = action;
      return this;
    }

    /**
     * Sets the tab completion function
     *
     * @param completer The function to provide tab completions
     */
    public Builder tabCompleter(@Nullable TabCompleteFunction completer) {
      this.builderTabCompleter = completer;
      return this;
    }

    /**
     * Sets the permission required for this command
     *
     * @param permissionChecked The permission string
     */
    public Builder permission(@Nullable String permissionChecked) {
      this.builderPermission = permissionChecked;
      return this;
    }

    /**
     * Builds the CommandWrapper instance
     */
    public CommandWrapper build() {
      if (builderCommandPath == null || builderCommandPath.isBlank())
        throw new IllegalStateException("Command path must be set");

      boolean hasAction = builderCommandAction != null;
      boolean hasPlayerAction = builderPlayerCommandAction != null;

      if (hasAction && hasPlayerAction)
        throw new IllegalStateException("Cannot set both CommandAction and PlayerCommandAction. Please choose one.");

      if (!hasAction && !hasPlayerAction)
        throw new IllegalStateException("Either command action or player command action must be set");

      return new CommandWrapper(
              builderCommandPath,
              builderCommandAction,
              builderPlayerCommandAction,
              builderTabCompleter,
              builderPermission
      );
    }
  }
}

