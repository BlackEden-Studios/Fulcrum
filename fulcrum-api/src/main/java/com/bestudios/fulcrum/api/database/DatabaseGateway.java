package com.bestudios.fulcrum.api.database;

import com.bestudios.fulcrum.api.service.Service;
import com.bestudios.fulcrum.api.util.Lock;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

/**
 * Interface representing a gateway for database operations.
 * Provides methods for initializing, shutting down, and performing CRUD operations on the database.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see DatabaseQuery
 * @see Lock
 */
public interface DatabaseGateway extends Service {

  /**
   * Initializes the database connection with the provided configuration values.
   *
   * @param lock   The lock provided by the calling plugin
   * @param config YAML configuration from the Settings Manager
   * @return true if the connection was successful, false otherwise
   */
  boolean initialize(@NotNull Lock lock, @NotNull FileConfiguration config);

  /**
   * Shuts down the database connection, ensuring all connections are properly closed before termination.
   *
   * @param lock The lock provided by the calling plugin
   * @return true if shutdown was successful, false otherwise
   */
  boolean shutdown(@NotNull Lock lock);

  /**
   * Checks if the database gateway is currently enabled.
   *
   * @return true if enabled, false otherwise
   */
  boolean isEnabled();

  /**
   * Creates a new database query for the specified table.
   *
   * @param table The table to query
   * @return A new database query
   */
  DatabaseQuery createQuery(@NotNull String table);

  /**
   * Creates a new database query for the specified table and key.
   *
   * @param table The table to query
   * @param key   The key to query
   * @return A new database query
   */
  DatabaseQuery createQuery(@NotNull String table, @NotNull String key);

  /**
   * Retrieves a specific field from the database based on the provided query.
   *
   * @param query The database query
   * @return The value of the field, or null if not found
   */
  byte[] getField(@NotNull DatabaseQuery query);

  /**
   * Retrieves all key-value pairs from the database based on the provided query.
   *
   * @param query The database query
   * @return A map of key-value pairs, or null if not found
   */
  @Nullable Map<byte[], byte[]> getKeys(@NotNull DatabaseQuery query);

  /**
   * Checks if a specific entry exists in the database based on the provided query.
   *
   * @param query The database query
   * @return true if the entry exists, false if it does not, or null if an error occurs
   */
  @Nullable Boolean exists(@NotNull DatabaseQuery query);

  /**
   * Sets a specific field in the database to the provided value based on the query.
   *
   * @param query The database query
   * @param value The value to set
   * @throws IOException if an error occurs during the operation
   */
  void setField(@NotNull DatabaseQuery query, byte[] value) throws IOException;

  /**
   * Sets multiple fields in the database based on the provided query and map of fields and values.
   *
   * @param query            The database query
   * @param fieldsAndValues  A map of fields and their corresponding values to set
   * @throws IOException if an error occurs during the operation
   */
  void setFields(@NotNull DatabaseQuery query, @NotNull Map<byte[], byte[]> fieldsAndValues) throws IOException;

  /**
   * Deletes a specific entry from the database based on the provided query.
   *
   * @param query The database query
   * @throws IOException if an error occurs during the operation
   */
  void deleteField(@NotNull DatabaseQuery query) throws IOException;
}
