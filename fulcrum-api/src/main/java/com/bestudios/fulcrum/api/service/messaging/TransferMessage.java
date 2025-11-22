package com.bestudios.fulcrum.api.service.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Message representing a player transfer event between servers.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see Message
 */
public final class TransferMessage extends Message {

  public static final String TYPE = "TRANSFER";

  private final TransferReason reason;
  private final String source;
  private final String destination;
  private final Map<String, String> metadata;

  public enum TransferReason {
    KICKED, BANNED, SERVER_SWITCH, LOBBY_RETURN, GAME_END, MAINTENANCE, QUEUE_SYSTEM, STAFF_MOVE, PLUGIN_COMMAND, NETWORK_ERROR
  }

  public TransferMessage(
          @NotNull TransferReason reason,
          @NotNull String source,
          @NotNull String destination,
          @Nullable Map<String, String> metadata) {
    super(TYPE);

    this.reason = Objects.requireNonNull(reason, "Reason cannot be null");
    this.source = Objects.requireNonNull(source, "Source cannot be null");
    this.destination = Objects.requireNonNull(destination, "Destination cannot be null");

    if (source.isEmpty()) throw new IllegalArgumentException("Source cannot be empty");
    if (destination.isEmpty()) throw new IllegalArgumentException("Destination cannot be empty");

    this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
  }

  @NotNull
  public TransferReason getReason() {
    return reason;
  }

  @NotNull
  public String getSource() {
    return source;
  }

  @NotNull
  public String getDestination() {
    return destination;
  }

  @NotNull
  public Map<String, String> getMetadata() {
    return new HashMap<>(metadata);
  }

  @Nullable
  public String getMetadata(@NotNull String key) {
    return metadata.get(key);
  }

  @Override
  protected void serializeFields(@NotNull Map<String, String> map) {
    map.put("reason", reason.name());
    map.put("source", source);
    map.put("destination", destination);

    // Prefix metadata to avoid conflicts
    for (Map.Entry<String, String> entry : metadata.entrySet()) {
      map.put("meta_" + entry.getKey(), entry.getValue());
    }
  }

  @Nullable
  static TransferMessage fromMapInternal(@NotNull Map<String, String> map) {
    String reason = map.get("reason");
    String source = map.get("source");
    String destination = map.get("destination");

    if (reason == null || source == null || destination == null) {
      return null;
    }

    // Extract metadata
    Map<String, String> metadata = new HashMap<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith("meta_")) {
        metadata.put(key.substring(5), entry.getValue());
      }
    }

    return new TransferMessage(TransferReason.valueOf(reason), source, destination, metadata);
  }
}
