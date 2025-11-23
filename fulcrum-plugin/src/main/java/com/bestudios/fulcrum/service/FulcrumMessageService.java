package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.database.DatabaseGateway;
import com.bestudios.fulcrum.api.database.DatabaseQuery;
import com.bestudios.fulcrum.api.service.messaging.Message;
import com.bestudios.fulcrum.api.service.messaging.MessageService;
import com.bestudios.fulcrum.api.util.Utils;
import com.bestudios.fulcrum.database.RedisQuery;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Redis-based implementation of MessageService.
 * Handles asynchronous message routing through a Redis database.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 */
public class FulcrumMessageService implements MessageService {

  /** The prefix for all message fields. */
  private static final String MESSAGE_PREFIX = "messages";
  /** The Fulcrum plugin instance. */
  private final FulcrumPlugin plugin;
  /** The service priority for this integration. */
  private final ServicePriority priority;
  /** The database gateway instance. */
  private final DatabaseGateway gateway;

  /**
   * Constructs a new FulcrumMessageService with the provided plugin and database gateway.
   *
   * @param plugin The Fulcrum plugin instance
   * @param gateway The database gateway instance
   */
  public FulcrumMessageService(@NotNull FulcrumPlugin plugin, @NotNull ServicePriority priority, @NotNull DatabaseGateway gateway) {
    this.plugin   = Objects.requireNonNull(plugin,   Utils.messageRequireNonNull("plugin"));
    this.priority = Objects.requireNonNull(priority, Utils.messageRequireNonNull("priority"));
    this.gateway  = Objects.requireNonNull(gateway,  Utils.messageRequireNonNull("gateway"));
  }

  @Override @NotNull
  public CompletableFuture<Boolean> sendMessage(@NotNull String channel, @NotNull UUID playerUUID, @NotNull Message message) {
    Objects.requireNonNull(channel,    Utils.messageRequireNonNull("channel"));
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));
    Objects.requireNonNull(message,    Utils.messageRequireNonNull("message"));
    // Create a future for the operation
    return CompletableFuture.supplyAsync(() -> {
      try {
        DatabaseQuery query = createQuery(channel, playerUUID);
        String fieldName = generateFieldName(message);
        byte[] messageData = message.toBytes();

        // Store as a hash field
        Map<byte[], byte[]> fieldMap = new HashMap<>();
        fieldMap.put(fieldName.getBytes(), messageData);
        gateway.setFields(query, fieldMap);

        return true;
      } catch (Exception e) {
        plugin.getLogger().severe("Failed to send message: " + e.getMessage());
        return false;
      }
    });
  }

  @Override @NotNull
  public CompletableFuture<Message> retrieveMessage(@NotNull String channel, @NotNull UUID playerUUID) {
    CompletableFuture<List<Message>> future = retrieveMessages(channel, playerUUID);
    return future.thenApply(messages -> {
      if (messages.isEmpty())
        return null;
      return messages.getFirst();
    });
  }

  @Override @NotNull
  public CompletableFuture<List<Message>> retrieveMessages(@NotNull String channel, @NotNull UUID playerUUID) {
    Objects.requireNonNull(channel,    Utils.messageRequireNonNull("channel"));
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));

    return CompletableFuture.supplyAsync(() -> {
      try {
        DatabaseQuery query = createQuery(channel, playerUUID);
        Map<byte[], byte[]> rawMessages = gateway.getKeys(query);
        if (rawMessages == null || rawMessages.isEmpty()) return Collections.emptyList();
        // Convert raw messages to messages
        List<Message> messages = new ArrayList<>();
        for (byte[] messageData : rawMessages.values()) {
          Message message = Message.fromBytes(messageData);
          if (message != null)
            messages.add(message);
        }

        // Sort by timestamp
        messages.sort(Comparator.comparingLong(Message::getTimestamp));
        return messages;

      } catch (Exception e) {
        plugin.getLogger().severe("Failed to retrieve messages: " + e.getMessage());
        return Collections.emptyList();
      }
    });
  }

  @Override
  public @NotNull CompletableFuture<Message> consumeMessage(@NotNull String channel, @NotNull UUID playerUUID) {
    Objects.requireNonNull(channel,    Utils.messageRequireNonNull("channel"));
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));

    CompletableFuture<Message> future = retrieveMessage(channel, playerUUID);
    return future.thenApplyAsync(message -> {
      if (message != null)
        try {
          gateway.deleteField(new RedisQuery(createQuery(channel, playerUUID).value(), generateFieldName(message)));
        } catch (Exception e) {
          plugin.getLogger().severe("Failed to consume message: " + e.getMessage());
        }
      return message;
    });
  }

  @Override @NotNull
  public CompletableFuture<List<Message>> consumeMessages(@NotNull String channel, @NotNull UUID playerUUID) {
    Objects.requireNonNull(channel,    Utils.messageRequireNonNull("channel"));
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));

    CompletableFuture<List<Message>> future = retrieveMessages(channel, playerUUID);
    return future.thenApply(messages -> {
      if (!messages.isEmpty())
        try {
            gateway.deleteField(createQuery(channel, playerUUID));
        } catch (Exception e) {
          plugin.getLogger().severe("Failed to consume messages: " + e.getMessage());
        }
      return messages;
    });
  }

  @Override @NotNull
  public CompletableFuture<Boolean> hasMessages(@NotNull String channel, @NotNull UUID playerUUID) {
    Objects.requireNonNull(channel,    Utils.messageRequireNonNull("channel"));
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));

    return CompletableFuture.supplyAsync(() -> {
      try {
        DatabaseQuery query = createQuery(channel, playerUUID);
        Boolean exists = gateway.exists(query);
        return exists != null && exists;
      } catch (Exception e) {
        plugin.getLogger().severe("Failed to check messages: " + e.getMessage());
        return false;
      }
    });
  }

  @Override @NotNull
  public CompletableFuture<Boolean> clearMessages(@NotNull String channel, @NotNull UUID playerUUID) {
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));

    return CompletableFuture.supplyAsync(() -> {
      try {
        DatabaseQuery query = createQuery(channel, playerUUID);
        gateway.deleteField(query);
        plugin.getLogger().info("Cleared messages for player " + playerUUID + " in channel " + channel);
        return true;
      } catch (Exception e) {
        plugin.getLogger().severe("Failed to clear messages: " + e.getMessage());
        return false;
      }
    });
  }

  @Override @NotNull
  public CompletableFuture<Boolean> clearAllMessages(@NotNull UUID playerUUID) {
    Objects.requireNonNull(playerUUID, Utils.messageRequireNonNull("player UUID"));
    return CompletableFuture.supplyAsync(() -> {
      try {
        DatabaseQuery query = new RedisQuery(MESSAGE_PREFIX, playerUUID.toString());
        gateway.deleteField(query);
        plugin.getLogger().info("Cleared all messages for player " + playerUUID);
        return true;
      } catch (Exception e) {
        plugin.getLogger().severe("Failed to clear all messages: " + e.getMessage());
        return false;
      }
    });
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

