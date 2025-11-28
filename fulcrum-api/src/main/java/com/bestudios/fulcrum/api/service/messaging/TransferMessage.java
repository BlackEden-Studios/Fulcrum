package com.bestudios.fulcrum.api.service.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

  /** The type of this message */
  public static final String TYPE = "TRANSFER";

  /** The reason for the transfer */
  private final TransferReason reason;
  /** The source server */
  private final String source;
  /** The destination server */
  private final String destination;
  /** Metadata associated with the transfer */
  private final Map<String, String> metadata;

  /** The reason for the transfer */
  public enum TransferReason {
    KICKED, BANNED, SERVER_SWITCH, LOBBY_RETURN, GAME_END, MAINTENANCE, QUEUE_SYSTEM, STAFF_MOVE, PLUGIN_COMMAND, NETWORK_ERROR
  }

  /**
   * Creates a new TransferMessage.
   * @param transferReason     The reason for the transfer
   * @param sourceServer       The source server
   * @param destinationServer  The destination server
   * @param additionalMetadata Metadata associated with the transfer
   */
  public TransferMessage(
          @NotNull TransferReason transferReason,
          @NotNull String sourceServer,
          @NotNull String destinationServer,
          @Nullable Map<String, String> additionalMetadata
  ) {
    super(TYPE);
    // Validate arguments
    this.reason      = Objects.requireNonNull(transferReason, "Reason cannot be null");
    this.source      = Objects.requireNonNull(sourceServer, "Source cannot be null");
    this.destination = Objects.requireNonNull(destinationServer, "Destination cannot be null");

    if (sourceServer.isEmpty()) throw new IllegalArgumentException("Source cannot be empty");
    if (destinationServer.isEmpty()) throw new IllegalArgumentException("Destination cannot be empty");

    this.metadata = additionalMetadata != null ?
                    new ConcurrentHashMap<>(additionalMetadata) :
                    new ConcurrentHashMap<>();
  }

  /**
   * Gets the reason for the transfer.
   * @return The reason for the transfer
   */
  @NotNull
  public TransferReason getReason() {
    return reason;
  }

  /**
   * Gets the source server.
   * @return The source server
   */
  @NotNull
  public String getSource() {
    return source;
  }

  /**
   * Gets the destination server.
   * @return The destination server
   */
  @NotNull
  public String getDestination() {
    return destination;
  }

  /**
   * Gets the metadata associated with the transfer.
   * @return The metadata
   */
  @NotNull
  public Map<String, String> getMetadata() {
    return new ConcurrentHashMap<>(metadata);
  }

  /**
   * Gets a metadata value by key.
   * @param key The key to get the value for
   * @return The value associated with the key, or null if it doesn't exist
   */
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
    for (Map.Entry<String, String> entry : metadata.entrySet())
      map.put("meta_" + entry.getKey(), entry.getValue());
  }

  @Nullable
  static TransferMessage fromMapInternal(@NotNull Map<String, String> map) {
    String reason = map.get("reason");
    String source = map.get("source");
    String destination = map.get("destination");

    if (reason == null || source == null || destination == null) return null;

    // Extract metadata
    Map<String, String> metadata = new ConcurrentHashMap<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith("meta_")) metadata.put(key.substring(5), entry.getValue());
    }

    return new TransferMessage(TransferReason.valueOf(reason), source, destination, metadata);
  }
}
