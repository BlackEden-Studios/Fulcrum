package com.bestudios.fulcrum.api.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Utility class for common command-related tasks.
 */
public class CommandUtils {

  /** The default locale for command tree messages */
  public static final Locale DEFAULT_LOCALE = Locale.US;

  /**
   * Utility method to filter a list of strings based on a partial match.
   * This is used for command tab completion to filter possibilities.
   *
   * @param possibilities The list of possible completions
   * @param partial       The partial string to match against
   * @return A filtered list of strings that start with the given partial string
   */
  public static List<String> filterCompletions(@NotNull List<String> possibilities, @NotNull String partial) {
    return possibilities.stream()
                        .filter(s -> s.toLowerCase(DEFAULT_LOCALE)
                                .startsWith(partial.toLowerCase(DEFAULT_LOCALE)))
                        .collect(Collectors.toList());
  }

  /**
   * Utility method to get player completions for a specific argument index in the command context.
   * This is used for tab completion to suggest online players based on the partial input.
   *
   * @param context        The command context
   * @param playerArgIndex The index of the argument where player names are expected
   * @return A list of player names that match the partial input
   */
  public static List<String> playerTabCompletions(CommandContext context, int playerArgIndex) {
    // 1. Check if there are enough arguments to provide the player argument
    if (context.remainingArgs().length <= playerArgIndex) {
      String partial = context.remainingArgs().length > (playerArgIndex - 1) ?
              context.remainingArgs()[(playerArgIndex - 1)] :
              "";

      return Bukkit.getOnlinePlayers().stream()
              .map(Player::getName)
              .filter(name -> name.toLowerCase(DEFAULT_LOCALE)
                      .startsWith(partial.toLowerCase(DEFAULT_LOCALE)))
              .collect(Collectors.toList());
    }
    return List.of();
  }

  /**
   * Utility method to get the target player from the command context.
   * If no player is specified, it defaults to the command sender if they are a player.
   * If the target player is not found or not online, it sends an error message.
   *
   * @param context        The command context
   * @param playerArgIndex The index of the argument where the player name is expected (starting at 1)
   * @return The target player or null if not found
   */
  @Nullable
  public static Player getTargetPlayer(CommandContext context, int playerArgIndex, boolean implicitSender) {
    // 1. Check if there are enough arguments to provide the player argument
    if (context.remainingArgs().length >= playerArgIndex)
      return Bukkit.getPlayer(context.remainingArgs()[playerArgIndex - 1]);
    // 2. Check if the sender implicitly provides the player argument
    if (context.isPlayer() && implicitSender) return context.getPlayer();
    // 3. No arguments and console sender: return null
    return null;
  }

  /**
   * Asynchronously retrieves a target OfflinePlayer.
   * <p>
   * Priority:
   * 1. If an argument exists at the index, look up that player (Async).
   * 2. If no argument exists and the sender is a player, return sender (Sync).
   * 3. Otherwise, return null.
   *
   * @param context        The command context
   * @param playerArgIndex The index of the argument (starting at 1)
   * @return A CompletableFuture containing the OfflinePlayer or null
   */
  public static CompletableFuture<OfflinePlayer> getTargetOfflinePlayerAsync(
          CommandContext context,
          int playerArgIndex,
          boolean implicitSender
  ) {
    // 1. Check if the user provided a specific argument
    if (context.remainingArgs().length >= playerArgIndex)
      // Run the lookup on a separate thread to avoid blocking the main server tick
      return CompletableFuture.supplyAsync(() -> {
        // This method might trigger a web request to Mojang
        return Bukkit.getOfflinePlayer(context.remainingArgs()[playerArgIndex - 1]);
      });
    // 2. Fallback: If no argument is provided, use the sender if they are a player
    if (context.isPlayer() && implicitSender) { return CompletableFuture.completedFuture(context.getPlayer()); }
    // 3. No argument and console sender: return null
    return CompletableFuture.completedFuture(null);
  }
}
