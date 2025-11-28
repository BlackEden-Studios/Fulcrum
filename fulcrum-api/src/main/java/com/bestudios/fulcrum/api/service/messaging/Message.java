package com.bestudios.fulcrum.api.service.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for all messages in the system.
 * Provides serialization/deserialization support with type discrimination.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see TransferMessage
 * @see NotificationMessage
 * @see DataSyncMessage
 */
public abstract class Message {

  /** Gson instance for serialization/deserialization */
  protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  /** Type identifier for this message */
  protected final String type;
  /** Timestamp when this message was created */
  protected final long timestamp;

  /**
   * Constructor.
   * @param messageType The message type string
   */
  protected Message(@NotNull String messageType) {
    this(messageType, -1L);
  }

  /**
   * Constructor.
   * @param messageType             The message type string
   * @param timestampInMilliseconds The timestamp in milliseconds
   */
  protected Message(@NotNull String messageType, long timestampInMilliseconds) {
    this.type = messageType;
    this.timestamp = timestampInMilliseconds > 0 ? timestampInMilliseconds : System.currentTimeMillis();
  }

  /**
   * Gets the type identifier for this message.
   *
   * @return The message type string
   */
  @NotNull
  public String getMessageType() {
    return type;
  }

  /**
   * Gets the timestamp when this message was created.
   *
   * @return The timestamp in milliseconds
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Converts this message to a Map for storage.
   *
   * @return Map representation of the message
   */
  @NotNull
  public Map<String, String> toMap() {
    Map<String, String> map = new ConcurrentHashMap<>();
    map.put("type", type);
    map.put("timestamp", String.valueOf(timestamp));
    serializeFields(map);
    return map;
  }

  /**
   * Serializes message-specific fields into the map.
   * Subclasses must implement this to add their own fields.
   *
   * @param map The map to populate with fields
   */
  protected abstract void serializeFields(@NotNull Map<String, String> map);

  /**
   * Converts this message to a JSON string.
   *
   * @return JSON representation of the message
   */
  @NotNull
  public String toJson() {
    return GSON.toJson(this.toMap());
  }

  /**
   * Converts this message to a byte array for storage.
   *
   * @return Byte array representation
   */
  public byte[] toBytes() {
    return toJson().getBytes();
  }

  /**
   * Creates a Message instance from a Map using type discrimination.
   *
   * @param map The map containing message data
   * @return Message instance or null if invalid
   */
  @Nullable
  public static Message fromMap(@Nullable Map<String, String> map) {
    // Empty map is invalid
    if (map == null || map.isEmpty()) return null;
    // Empty message type is invalid
    String type = map.get("type");
    if (type == null) return null;

    // Factory pattern for creating specific message types
    return switch (type) {
      case TransferMessage.TYPE     -> TransferMessage.fromMapInternal(map);
      case NotificationMessage.TYPE -> NotificationMessage.fromMapInternal(map);
      case DataSyncMessage.TYPE     -> DataSyncMessage.fromMapInternal(map);
      default -> null;
    };
  }

  /**
   * Creates a Message from JSON string.
   *
   * @param json The JSON string
   * @return Message instance or null if invalid
   */
  @Nullable
  public static Message fromJson(@Nullable String json) {
    // Empty JSON is invalid
    if (json == null || json.isEmpty()) return null;

    try {
      Map<String, String> map = new ConcurrentHashMap<>();
      JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
      jsonObject.entrySet().forEach(
              entry -> map.put(entry.getKey(), entry.getValue().getAsString()));
      return fromMap(map);
    } catch (JsonSyntaxException e) {
      return null;
    }

  }

  /**
   * Creates a Message from a byte array.
   *
   * @param bytes The byte array
   * @return Message instance or null if invalid
   */
  @Nullable
  public static Message fromBytes(byte[] bytes) {
    // Empty byte array is invalid
    if (bytes == null || bytes.length == 0) return null;

    return fromJson(new String(bytes));
  }
}
