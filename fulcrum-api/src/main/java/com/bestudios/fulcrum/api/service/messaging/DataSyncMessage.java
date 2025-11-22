package com.bestudios.fulcrum.api.service.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Message for synchronizing arbitrary data between servers.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see Message
 */
public final class DataSyncMessage extends Message {

  public static final String TYPE = "DATA_SYNC";

  private final String dataKey;
  private final Map<String, String> data;

  public DataSyncMessage(@NotNull String dataKey, @NotNull Map<String, String> data) {
    super(TYPE);
    this.dataKey = Objects.requireNonNull(dataKey, "Data key cannot be null");
    this.data = new HashMap<>(Objects.requireNonNull(data, "Data cannot be null"));
  }

  @NotNull
  public String getDataKey() {
    return dataKey;
  }

  @NotNull
  public Map<String, String> getData() {
    return new HashMap<>(data);
  }

  @Override
  protected void serializeFields(@NotNull Map<String, String> map) {
    map.put("dataKey", dataKey);
    for (Map.Entry<String, String> entry : data.entrySet()) {
      map.put("data_" + entry.getKey(), entry.getValue());
    }
  }

  @Nullable
  static DataSyncMessage fromMapInternal(@NotNull Map<String, String> map) {
    String dataKey = map.get("dataKey");
    if (dataKey == null) {
      return null;
    }

    Map<String, String> data = new HashMap<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      if (entry.getKey().startsWith("data_")) {
        data.put(entry.getKey().substring(5), entry.getValue());
      }
    }

    return new DataSyncMessage(dataKey, data);
  }
}
