package com.bestudios.fulcrum.api.service.permission;

import com.bestudios.fulcrum.api.service.Service;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Permission Service Interface.
 * <p>
 * Provides methods to check, add, and remove permissions for players,
 * as well as check group membership.
 * <p>
 * All methods return a CompletableFuture to allow for asynchronous handling.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see Service
 * @see CompletableFuture
 */
public interface PermissionsService extends Service {

  /**
   * Checks if a {@link CommandSender} has a permission node.
   * <p>
   * This will return the result of bukkit's generic hasPermission() method.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * <p>
   * This method will explicitly fail if the registered permission system does not register permissions in bukkit.
   * @param sender     to check permissions on
   * @param permission to check for
   * @return Success or Failure
   */
  default CompletableFuture<Boolean> has(@NotNull final CommandSender sender, @NotNull final String permission) {
    return CompletableFuture.completedFuture(sender.hasPermission(permission));
  }

  /**
   * Checks if {@link Player} with the given UUID has a permission node.
   * <p>
   * This will return the result of bukkit's generic hasPermission() method.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * <p>
   * The default implementation will return false if the player is not online.
   * <p>
   * This method will explicitly fail if the registered permission system does not register permissions in bukkit.
   * @param playerID   UUID of the player to check
   * @param permission Permission node
   * @return Success or Failure
   */
  default CompletableFuture<Boolean> has(@NotNull final UUID playerID, @NotNull final String permission) {
    return has(Bukkit.getOfflinePlayer(playerID), permission);
  }

  /**
   * Checks if {@link Player} has a permission node.
   * <p>
   * This will return the result of bukkit's generic hasPermission() method.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * <p>
   * This method will explicitly fail if the registered permission system does not register permissions in bukkit.
   * @param player     Player Object
   * @param permission Permission node
   * @return Success or Failure
   */
  default CompletableFuture<Boolean> has(@NotNull final Player player, @NotNull final String permission) {
    return CompletableFuture.completedFuture(player.hasPermission(permission));
  }

  /**
   * Checks if {@link Player} with the given name has a permission node.
   * <p>
   * This will return the result of bukkit's generic hasPermission() method.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * <p>
   * The default implementation will return false if the player is not online.
   * <p>
   * This method will explicitly fail if the registered permission system does not register permissions in bukkit.
   * @param playerName Name of the player to check
   * @param permission Permission node
   * @return Success or Failure
   */
  default CompletableFuture<Boolean> has(@NotNull final String playerName, @NotNull final String permission) {
    Player player = Bukkit.getPlayer(playerName);
    if (player == null) return CompletableFuture.completedFuture(false);
    return has(player, permission);
  }

  /**
   * Checks if {@link OfflinePlayer} has a permission node.
   * <p>
   * This will return the result of bukkit's generic hasPermission() method.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * <p>
   * This method will explicitly fail if the registered permission system does not register permissions in bukkit.
   * @param player     OfflinePlayer Object
   * @param permission Permission node
   * @return Success or Failure
   */
  default CompletableFuture<Boolean> has(@NotNull final OfflinePlayer player, @NotNull final String permission) {
    if (player.isOnline()) {
      assert player.getPlayer() != null;
      return has(player.getPlayer(), permission);
    }
    return CompletableFuture.completedFuture(false);
  }

  /**
   * Adds a permission node to a {@link Player} with the given UUID.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param playerID   UUID of the player
   * @param permission Permission node
   * @return Success or Failure
   */
  CompletableFuture<Boolean> add(@NotNull final UUID playerID, @NotNull final String permission);

  /**
   * Adds a permission node to a {@link Player}.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param player     Player Object
   * @param permission Permission node
   * @return Success or Failure
   */
  CompletableFuture<Boolean> add(@NotNull final Player player, @NotNull final String permission);
  
  /**
   * Adds a permission node to a {@link Player} with the given name.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param playerName Name of the player
   * @param permission Permission node
   * @return Success or Failure
   */
  CompletableFuture<Boolean> add(@NotNull final String playerName, @NotNull final String permission);
  
  /**
   * Adds a permission node to a {@link OfflinePlayer}.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param player     OfflinePlayer Object
   * @param permission Permission node
   * @return Success or Failure
   */
  CompletableFuture<Boolean> add(@NotNull final OfflinePlayer player, @NotNull final String permission);

  /**
   * Removes a permission node from a {@link Player} with the given UUID.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param playerID   UUID of the player
   * @param permission Permission node
   * @return Success or Failure
   */
  CompletableFuture<Boolean> remove(@NotNull final UUID playerID, @NotNull final String permission);

  /**
   * Removes a permission node from a {@link Player}.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param player     Player Object
   * @param permission Permission node
   * @return Success or Failure
   */
  CompletableFuture<Boolean> remove(@NotNull final Player player, @NotNull final String permission);
  
  /**
   * Removes a permission node from a {@link Player} with the given name.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param playerName Name of the player
   * @param permission Permission node
   * @return Success or Failure
   */
  CompletableFuture<Boolean> remove(@NotNull final String playerName, @NotNull final String permission);
  
  /**
   * Removes a permission node from a {@link OfflinePlayer}.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param player     OfflinePlayer Object
   * @param permission Permission node
   * @return Success or Failure
   */
  CompletableFuture<Boolean> remove(@NotNull final OfflinePlayer player, @NotNull final String permission);

  /**
   * Checks if a {@link Player} with the given UUID is in a permission group.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param playerID  UUID of the player to check
   * @param groupName Name of the group
   * @return Success or Failure
   */
  CompletableFuture<Boolean> is(@NotNull final UUID playerID, @NotNull final String groupName);

  /**
   * Checks if a {@link Player} is in a permission group.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param player    Player Object
   * @param groupName Name of the group
   * @return Success or Failure
   */
  CompletableFuture<Boolean> is(@NotNull final Player player, @NotNull final String groupName);

  /**
   * Checks if a {@link Player} with the given name is in a permission group.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param playerName Name of the player to check
   * @param groupName  Name of the group
   * @return Success or Failure
   */
  CompletableFuture<Boolean> is(@NotNull final String playerName, @NotNull final String groupName);

  /**
   * Checks if a {@link OfflinePlayer} is in a permission group.
   * <p>
   * Use the method thenAccept() to handle the result asynchronously when it is ready.
   * @param player    OfflinePlayer Object
   * @param groupName Name of the group
   * @return Success or Failure
   */
  CompletableFuture<Boolean> is(@NotNull final OfflinePlayer player, @NotNull final String groupName);
}
