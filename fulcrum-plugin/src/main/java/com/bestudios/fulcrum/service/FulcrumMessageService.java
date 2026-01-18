package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.service.database.DatabaseService;
import com.bestudios.fulcrum.api.service.database.DatabaseQuery;
import com.bestudios.fulcrum.api.service.messaging.Message;
import com.bestudios.fulcrum.api.service.messaging.MessageService;
import com.bestudios.fulcrum.api.util.Utils;
import com.bestudios.fulcrum.database.RedisQuery;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis-based implementation of MessageService.
 * Handles asynchronous message routing through a Redis database.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Message
 */
public class FulcrumMessageService implements MessageService {

  /** The prefix for all message fields. */
  private static final String MESSAGE_PREFIX = "messages";
  /** The Fulcrum plugin instance. */
  private final FulcrumPlugin plugin;
  /** The service priority for this integration. */
  private final ServicePriority priority;
  /** The database gateway instance. */
  private final DatabaseService gateway;

  /**
   * Constructs a new FulcrumMessageService with the provided plugin and database gateway.
   *
   * @param pluginRef The Fulcrum plugin instance
   * @param databaseService The database gateway instance
   */
  public FulcrumMessageService(
          @NotNull FulcrumPlugin pluginRef,
          @NotNull ServicePriority servicePriority,
          @NotNull DatabaseService databaseService
  ) {
    this.plugin   = Objects.requireNonNull(pluginRef,       Utils.messageRequireNonNull("plugin"));
    this.priority = Objects.requireNonNull(servicePriority, Utils.messageRequireNonNull("priority"));
    this.gateway  = Objects.requireNonNull(databaseService, Utils.messageRequireNonNull("gateway"));
  }

  @Override @NotNull
  public CompletableFuture<Boolean> sendMessage(
          @NotNull String channel,
          @NotNull UUID playerUUID,
          @NotNull Message message
  ) {
    Objects.requireNonNull(channel,    Utils.messageRequireNonNull("channel"));
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));
    Objects.requireNonNull(message,    Utils.messageRequireNonNull("message"));
    // Create a future for the operation
    return CompletableFuture.supplyAsync(() -> {

      DatabaseQuery query = createQuery(channel, playerUUID);
      Map<byte[], byte[]> fieldMap = new ConcurrentHashMap<>();

      // Store as a hash field
      fieldMap.put(generateFieldName(message).getBytes(), message.toBytes());

      try {
        gateway.setFields(query, fieldMap);
      } catch (IOException e) {
        plugin.getLogger().severe("Failed to store message: " + e.getMessage());
        return false;
      }
      return true;
    });
  }

  @Override @NotNull
  public CompletableFuture<Message> retrieveMessage(@NotNull String channel, @NotNull UUID playerUUID) {
    CompletableFuture<List<Message>> future = retrieveMessages(channel, playerUUID);
    return future.thenApplyAsync(
            messages -> {
              if (messages.isEmpty()) return null;
              return messages.getFirst();
            }
    );
  }

  @Override @NotNull
  public CompletableFuture<List<Message>> retrieveMessages(@NotNull String channel, @NotNull UUID playerUUID) {
    checkInput(channel, playerUUID);
    return CompletableFuture.supplyAsync(() -> {

        DatabaseQuery query = createQuery(channel, playerUUID);
        Map<byte[], byte[]> rawMessages = gateway.getKeys(query);
        if (rawMessages == null || rawMessages.isEmpty()) return Collections.emptyList();

        // Convert raw messages to messages
        List<Message> messages = new ArrayList<>();
        for (byte[] messageData : rawMessages.values()) {
          Message message = Message.fromBytes(messageData);
          if (message != null) messages.add(message);
        }

        // Sort by timestamp
        messages.sort(Comparator.comparingLong(Message::getTimestamp));
        return messages;
    });
  }

  @Override
  public @NotNull CompletableFuture<Message> consumeMessage(@NotNull String channel, @NotNull UUID playerUUID) {
    checkInput(channel, playerUUID);
    CompletableFuture<Message> future = retrieveMessage(channel, playerUUID);
    return future.thenApplyAsync(message -> {
      deleteField(playerUUID, new RedisQuery(createQuery(channel, playerUUID).value(), generateFieldName(message)));
      return message;
    });
  }

  @Override @NotNull
  public CompletableFuture<List<Message>> consumeMessages(@NotNull String channel, @NotNull UUID playerUUID) {
    checkInput(channel, playerUUID);
    CompletableFuture<List<Message>> future = retrieveMessages(channel, playerUUID);
    return future.thenApplyAsync(messages -> {
      deleteField(playerUUID, createQuery(channel, playerUUID));
      return messages;
    });
  }

  @Override @NotNull
  public CompletableFuture<Boolean> hasMessages(@NotNull String channel, @NotNull UUID playerUUID) {
    checkInput(channel, playerUUID);
    return CompletableFuture.supplyAsync(() -> {
      Boolean messageExists = gateway.exists(createQuery(channel, playerUUID));
      return messageExists != null && messageExists;
    });
  }

  @Override @NotNull
  public CompletableFuture<Boolean> clearMessages(@NotNull String channel, @NotNull UUID playerUUID) {
    checkInput(channel, playerUUID);
    return CompletableFuture.supplyAsync(
            () -> deleteField(playerUUID, createQuery(channel, playerUUID)));
  }

  @Override @NotNull
  public CompletableFuture<Boolean> clearAllMessages(@NotNull UUID playerUUID) {
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));
    return CompletableFuture.supplyAsync(
            () -> deleteField(playerUUID, new RedisQuery(MESSAGE_PREFIX, playerUUID.toString())));
  }

  /**
   * Creates a Redis query for a specific player and channel.
   *
   * @param playerUUID The UUID of the player
   * @param channel    The channel to where the message is sent
   * @return A DatabaseQuery for the specified player and channel
   */
  private DatabaseQuery createQuery(@NotNull String channel, @NotNull UUID playerUUID) {
    return new RedisQuery(MESSAGE_PREFIX, playerUUID + ":" + channel);
  }

  /**
   * Generates a field name for a message based on its type and timestamp.
   *
   * @param message The message to generate the field name for
   * @return The generated field name
   */
  private String generateFieldName(@NotNull Message message) {
    return message.getMessageType() + ":" + message.getTimestamp();
  }

  /**
   * Deletes all messages for a specific player with a specific query.
   * @param playerID The UUID of the player
   * @param query    The query to delete messages for
   * @return True if the operation was successful, false otherwise
   */
  private boolean deleteField(UUID playerID, DatabaseQuery query)  {
    try {
      gateway.deleteField(query);
      plugin.getLogger().info("Cleared messages for player " + playerID);
      return true;
    } catch (IOException e) {
      plugin.getLogger().severe("Failed to clear messages: " + e.getMessage());
      return false;
    }
  }

  @Override
  public ServicePriority getPriority() {
    return this.priority;
  }

  @Override
  public String getPluginName() {
    return "Fulcrum";
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public String getPluginVersion() {
    return plugin.getDescription().getVersion();
  }
}

