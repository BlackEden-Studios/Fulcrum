package com.bestudios.fulcrum.api.service.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message for synchronizing arbitrary data between servers.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Message
 */
public final class DataSyncMessage extends Message {

  /** The type of this message */
  public static final String TYPE = "DATA_SYNC";

  /** The key for the data to be synced */
  private final String key;
  /** The data to be synced */
  private final Map<String, String> data;

  /**
   * Creates a new DataSyncMessage.
   * @param dataKey The key for the data to be synced
   * @param data    The data to be synced
   */
  public DataSyncMessage(@NotNull String dataKey, @NotNull Map<String, String> data) {
    super(TYPE);
    this.key = Objects.requireNonNull(dataKey, "Data key cannot be null");
    this.data = new ConcurrentHashMap<>(Objects.requireNonNull(data, "Data cannot be null"));
  }

  /**
   * Gets the key for the data to be synced.
   * @return The data key
   */
  @NotNull
  public String getDataKey() {
    return key;
  }

  /**
   * Gets the data to be synced.
   * @return The data
   */
  @NotNull
  public Map<String, String> getData() {
    return new ConcurrentHashMap<>(data);
  }

  /**
   * Serializes the message's fields into the map.
   * @param map The map to serialize into
   */
  @Override
  protected void serializeFields(@NotNull Map<String, String> map) {
    map.put("dataKey", key);
    for (Map.Entry<String, String> entry : data.entrySet())
      map.put("data_" + entry.getKey(), entry.getValue());
  }

  @Nullable
  static DataSyncMessage fromMapInternal(@NotNull Map<String, String> map) {
    String dataKey = map.get("dataKey");
    if (dataKey == null) return null;

    Map<String, String> data = new ConcurrentHashMap<>();
    // Strip metadata prefix to avoid conflicts
    for (Map.Entry<String, String> entry : map.entrySet())
      if (entry.getKey().startsWith("data_")) data.put(entry.getKey().substring(5), entry.getValue());
    return new DataSyncMessage(dataKey, data);
  }
}
