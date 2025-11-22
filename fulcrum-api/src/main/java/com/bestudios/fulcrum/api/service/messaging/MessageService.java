package com.bestudios.fulcrum.api.service.messaging;

import com.bestudios.fulcrum.api.service.Service;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Common interface for routing messages between server instances.
 * Provides asynchronous operations for sending and retrieving messages.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see Message
 */
public interface MessageService extends Service {

  /**
   * Sends a message to the message queue for a specific player.
   *
   * @param channel    The channel to send the message to
   * @param playerUUID The UUID of the target player
   * @param message    The message to send
   * @return CompletableFuture that completes when the message is sent
   */
  @NotNull
  CompletableFuture<Boolean> sendMessage(@NotNull String channel, @NotNull UUID playerUUID, @NotNull Message message);

  /**
   * Retrieves the latest pending message for a player asynchronously.
   *
   * @param channel    The channel to retrieve the message from
   * @param playerUUID The UUID of the player
   * @return CompletableFuture containing the message, or null if none exist
   */
  @NotNull
  CompletableFuture<Message> retrieveMessage(@NotNull String channel, @NotNull UUID playerUUID);

  /**
   * Retrieves all pending messages for a player asynchronously.
   *
   * @param channel    The channel to retrieve messages from
   * @param playerUUID The UUID of the player
   * @return CompletableFuture containing list of messages, or empty list if none exist
   */
  @NotNull
  CompletableFuture<List<Message>> retrieveMessages(@NotNull String channel, @NotNull UUID playerUUID);

  /**
   * Retrieves and deletes the latest pending message for a player.
   *
   * @param channel    The channel to retrieve the message from
   * @param playerUUID The UUID of the player
   * @return CompletableFuture containing the message, or null if none exist
   */
  @NotNull
  CompletableFuture<Message> consumeMessage(@NotNull String channel, @NotNull UUID playerUUID);

  /**
   * Retrieves and deletes all pending messages for a player.
   *
   * @param channel    The channel to retrieve messages from
   * @param playerUUID The UUID of the player
   * @return CompletableFuture containing a list of messages, or empty list if none exist
   */
  @NotNull
  CompletableFuture<List<Message>> consumeMessages(@NotNull String channel, @NotNull UUID playerUUID);

  /**
   * Checks if messages exist for a player.
   *
   * @param channel    The channel to check messages from
   * @param playerUUID The UUID of the player
   * @return CompletableFuture containing true if messages exist
   */
  @NotNull
  CompletableFuture<Boolean> hasMessages(@NotNull String channel, @NotNull UUID playerUUID);

  /**
   * Deletes all messages for a player asynchronously in a specific channel.
   *
   * @param channel    The channel to delete messages from
   * @param playerUUID The UUID of the player
   * @return CompletableFuture that completes when deletion is done
   */
  @NotNull
  CompletableFuture<Boolean> clearMessages(@NotNull String channel, @NotNull UUID playerUUID);

  /**
   * Deletes all messages for a player asynchronously in all channels.
   *
   * @param playerUUID The UUID of the player
   * @return CompletableFuture that completes when deletion is done
   */
  @NotNull
  CompletableFuture<Boolean> clearAllMessages(@NotNull UUID playerUUID);
}
