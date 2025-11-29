package com.bestudios.fulcrum.database;

import com.bestudios.fulcrum.Fulcrum;
import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.database.DatabaseGateway;
import com.bestudios.fulcrum.api.util.Lock;
import com.bestudios.fulcrum.api.database.DatabaseQuery;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementation of the DatabaseGateway interface based on Redis,
 * that provides methods to interact with a Redis database using Jedis.
 * <p>
 * It supports initializing and shutting down the connection pool,
 * as well as performing CRUD operations on the Redis database.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see DatabaseGateway
 * @see Jedis
 * @see JedisPool
 */
public class RedisDatabaseGateway implements DatabaseGateway {

  /** Redis connection pool */
  private JedisPool pool;
  /** Hostname of the Redis server */
  private String host;
  /** Port number of the Redis server */
  private int port;
  /** Password for Redis authentication */
  private String password;
  /** Redis database index */
  private int database;
  /** Maximum number of connections in the pool */
  private int maxConnections;
  /** Flag indicating if Redis is enabled */
  private boolean isEnabled;
  /** Reference to the Fulcrum plugin */
  private final FulcrumPlugin plugin;

  /**
   * Constructor for RedisDatabaseGateway.
   *
   * @param pluginRef The Fulcrum plugin instance
   */
  public RedisDatabaseGateway(FulcrumPlugin pluginRef) {
    this.plugin = pluginRef;
    this.isEnabled = false;
  }

  /**
   * Initializes the Redis connection pool with the provided configuration values.
   *
   * @param lock          The lock provided by the calling plugin
   * @param configuration File configuration containing Redis settings
   * @return true if the connection was successful, false otherwise
   */
  @Override
  public boolean initialize(@NotNull Lock lock, @NotNull FileConfiguration configuration) {
    // Validate the lock
    if (!(lock instanceof Fulcrum.FulcrumLock))
      throw new IllegalArgumentException("Lock provided is illegal");
    // Load configuration values
    this.host           = configuration.getString("database.host", "localhost");
    this.port           = configuration.getInt("database.port", 6380);
    this.password       = configuration.getString("database.password", "P455W0RD");
    this.database       = configuration.getInt("database.database", 0);
    this.maxConnections = configuration.getInt("database.max_connections", 16);
    // Establish the connection
    return connect();
  }

  /**
   * Shuts down the Redis connection pool, ensuring all connections are properly closed before termination.
   *
   * @param lock The lock provided by the calling plugin
   */
  @Override
  public boolean shutdown(@NotNull Lock lock) {
    // Validate the lock
    if (!(lock instanceof Fulcrum.FulcrumLock))
      throw new IllegalArgumentException("Lock provided is illegal");
    // Close the connection pool
    if (pool != null && !pool.isClosed()) {
      pool.close();
      this.isEnabled = false;
      this.plugin.getLogger().info("Redis connection pool has been shut down.");
      return true;
    }
    return false;
  }

  /**
   * Checks if Redis is currently enabled and connected.
   *
   * @return true if Redis is enabled and connected, false otherwise
   */
  @Override
  public boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Get a single field value.
   *
   * @param query The query to retrieve the field from
   * @return byte[] type value if the field value exists, null otherwise
   */
  @Override
  public byte[] getField(@NotNull DatabaseQuery query) {
    return this.execute(jedis -> jedis.get(query.value().getBytes()));
  }

  /**
   * Get a single field value.
   *
   * @param query The query to retrieve the field from
   * @return String type value if the field value exists, null otherwise
   */
  @Nullable
  public String getFieldString(@NotNull DatabaseQuery query) {
    return this.execute(jedis -> jedis.get(query.value()));
  }

  /**
   * Get the map of all fields associated with the given key.
   *
   * @param query The query to retrieve the key from
   * @return Map with all field values if the key exists, empty map otherwise
   */
  @Override @Nullable
  public Map<byte[], byte[]> getKeys(@NotNull DatabaseQuery query) {
    return this.execute(jedis -> jedis.hgetAll(query.value().getBytes()));
  }

  /**
   * Get the map of all fields associated with the given key.
   *
   * @param query The query to retrieve the key from
   * @return Map with all field values if the key exists, empty map otherwise
   */
  @Nullable
  public Map<String, String> getKeysAsString(@NotNull DatabaseQuery query) {
    return this.execute(jedis -> jedis.hgetAll(query.value()));
  }

  /**
   * Checks if a key exists in the database.
   *
   * @param query The query to check for the key
   * @return true if the key exists, false if it doesn't exist, null if database error occurred
   */
  @Override @Nullable
  public Boolean exists(@NotNull DatabaseQuery query) {
    return this.execute(jedis -> jedis.exists(query.value()));
  }

  /**
   * Set the given key with the given value
   *
   * @param query  The query to set the key to
   * @param value  The value to insert
   * @throws RejectedExecutionException when the execution fails
   */
  @Override
  public void setField(@NotNull DatabaseQuery query, byte[] value) throws RejectedExecutionException {
    Boolean result = this.execute(jedis -> {
      jedis.set(query.value().getBytes(), value);
      return true;
    });

    // execute() returns null if an exception occurred or the connection failed
    if (result == null || !result) {
      throw new RejectedExecutionException("Failed to set field");
    }
  }

  /**
   * Set the given key with the given value
   *
   * @param query  The query to set the key to
   * @param value  The value to insert
   * @throws RejectedExecutionException when the execution fails
   */
  public void setField(@NotNull DatabaseQuery query, @NotNull String value) throws RejectedExecutionException {
    this.setField(query, value.getBytes());
  }

  /**
   * Set multiple fields for the given key
   *
   * @param query           The query to set the key to
   * @param fieldsAndValues Map containing the fields to set with the specified value
   * @throws RejectedExecutionException when the execution fails
   */
  @Override
  public void setFields(
          @NotNull DatabaseQuery query,
          @NotNull Map<byte[], byte[]> fieldsAndValues
  ) throws RejectedExecutionException {
    // Set the fields in a single transaction
    Boolean result = execute(jedis -> {
      // Set all hash fields at once
      String response = jedis.hmset(query.value().getBytes(), fieldsAndValues);
      return "OK".equals(response);
    });

    if (result == null || !result) throw new RejectedExecutionException("Failed to set hash fields");
  }

  /**
   * Delete the given key from the database
   *
   * @param query The key to delete
   */
  @Override
  public void deleteField(@NotNull DatabaseQuery query) {
    this.execute(jedis -> {
      jedis.del(query.value());
      return true;
    });
  }

  /**
   * Establishes a connection to the Redis server using the current settings.
   *
   * @return true if the connection was successful, false otherwise
   */
  private boolean connect() {
    if (this.pool != null && !this.pool.isClosed()) this.pool.close();

    // Configure the Jedis connection pool
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(this.maxConnections);
    poolConfig.setMaxIdle(this.maxConnections / 2);
    poolConfig.setMinIdle(1);
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setBlockWhenExhausted(true);

    // Create the Jedis connection pool
    this.pool = new JedisPool(poolConfig, this.host, this.port, 5000, this.password, this.database);

    // Test the connection
    try (Jedis jedis = this.pool.getResource()) {
      jedis.ping();
      this.isEnabled = true;
      this.plugin.getLogger().info("Successfully connected to Redis server at " + this.host + ":" + this.port);
      return true;
    } catch (Exception e) {
      this.isEnabled = false;
      this.plugin.getLogger().severe("SEVERE - Failed to connect to Redis server: " + e);
      return false;
    }
  }

  /**
   * Executes a Redis command that returns a result.
   *
   * @param <T>      The return type of the Redis operation
   * @param function The function to execute with a Jedis resource
   * @return The result of the function, or null if an error occurred
   */
  private <T> @Nullable T execute(Function<Jedis, T> function) {
    if (!this.isEnabled || this.pool == null || this.pool.isClosed()) {
      this.plugin.getLogger().info("Redis is not enabled or connection pool is closed.");
      return null;
    }
    // Execute the function with a Jedis resource
    try (Jedis jedis = pool.getResource()) {
      return function.apply(jedis);
    } catch (JedisConnectionException e) {
      this.plugin.getLogger().warning("WARNING - Redis connection lost, attempting to reconnect... " + e);
      if (connect()) {
        try (Jedis jedis = this.pool.getResource()) {
          return function.apply(jedis);
        } catch (Exception innerEx) {
          this.plugin.getLogger().severe("SEVERE - Failed to execute Redis command after reconnect: " + innerEx);
        }
      }
      return null;
    } catch (Exception e) {
      this.plugin.getLogger().severe("SEVERE - Error executing Redis command: " + e);
      return null;
    }
  }

}
