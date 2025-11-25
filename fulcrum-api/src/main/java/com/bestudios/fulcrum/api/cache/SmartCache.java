package com.bestudios.fulcrum.api.cache;

import com.bestudios.fulcrum.api.util.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SmartCache provides an intelligent caching mechanism for player-specific data.
 * It automatically manages player data by caching it during player sessions and
 * handling cleanup when players disconnect from the server.
 *
 * @param <T> The type of objects to be stored in the cache
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see SessionCache
 */
public class SmartCache<T> {

  /** Default error message for null player IDs */
  protected static final String NULL_ID_ERROR = Utils.messageRequireNonNull("player ID");
  /** Default capacity buffer for the cache size */
  protected static final int DEFAULT_CAPACITY_BUFFER = 20;

  /** The cache implemented as a map */
  protected final ConcurrentHashMap<UUID, T> cache;
  /** The plugin instance */
  protected final Plugin plugin;

  /**
   * Creates a new SmartCache with a specified maximum number of entries.
   *
   * @param maxEntries The maximum number of entries expected in the cache
   * @param pluginReference The plugin instance
   */
  public SmartCache(int maxEntries, @NotNull Plugin pluginReference) {
    this.cache = new ConcurrentHashMap<>(maxEntries + DEFAULT_CAPACITY_BUFFER);
    this.plugin = Objects.requireNonNull(pluginReference, Utils.messageRequireNonNull("plugin"));

    // Register listener for player disconnect events
    SmartCacheListener listener = new SmartCacheListener();
    this.plugin.getServer().getPluginManager().registerEvents(listener, pluginReference);
    this.plugin.getLogger().config("Registered " + this.getClass().getSimpleName() +
                                   " with capacity for " + maxEntries + " entries");
  }

  /**
   * Creates a new SmartCache sized to match the server's configured maximum player count.
   *
   * @param pluginReference The plugin instance
   */
  public SmartCache(@NotNull Plugin pluginReference) {
    this(pluginReference.getServer().getMaxPlayers(), pluginReference);
  }

  /**
   * Stores an item in the cache, associated with the specified player UUID.
   *
   * @param playerID The UUID of the player
   * @param item The item to be stored
   */
  public void put(@NotNull UUID playerID, T item) {
    this.cache.put(Objects.requireNonNull(playerID, NULL_ID_ERROR), item);
  }

  /**
   * Retrieves an item from the cache by player UUID.
   *
   * @param playerID The UUID of the player
   * @return The cached item, or null if no item exists
   */
  public T get(@NotNull UUID playerID) {
    return this.cache.get(Objects.requireNonNull(playerID, NULL_ID_ERROR));
  }

  /**
   * Removes an item from the cache by player UUID.
   *
   * @param playerID The UUID of the player
   */
  public void remove(@NotNull UUID playerID) {
    T item = this.cache.remove(Objects.requireNonNull(playerID, NULL_ID_ERROR));
    if (item != null) dispose(item);
  }

  /**
   * Checks if the cache contains an entry for the specified player.
   *
   * @param playerID The UUID of the player
   * @return true if an entry exists, false otherwise
   */
  public boolean containsEntry(@NotNull UUID playerID) {
    return this.cache.containsKey(Objects.requireNonNull(playerID, NULL_ID_ERROR));
  }

  /**
   * Checks if the cache is empty.
   *
   * @return true if the cache contains no entries
   */
  public boolean isEmpty() {
    return this.cache.isEmpty();
  }

  /**
   * Gets the current size of the cache.
   *
   * @return The number of entries in the cache
   */
  public int size() {
    return this.cache.size();
  }

  /**
   * Properly disposes of resources associated with a cached item.
   * Override this method to add custom disposal logic.
   *
   * @param item The item to be disposed of
   */
  protected void dispose(@NotNull T item) {
    try {
      if (item instanceof BukkitRunnable) ((BukkitRunnable) item).cancel();
      // Add more disposal patterns as needed (e.g., AutoCloseable)
      if (item instanceof AutoCloseable) ((AutoCloseable) item).close();
    } catch (Exception e) {
      plugin.getLogger().warning("Error disposing item: " + e.getMessage());
    }
  }

  /**
   * Saves the data for a specific player to persistent storage.
   * Override this method to implement actual persistence logic.
   *
   * @param playerID The UUID of the player
   */
  protected void savePlayerData(@NotNull UUID playerID) {
    // Override in subclasses for actual persistence
  }

  /**
   * Saves all cached data to persistent storage.
   * Override this method to implement batch save logic.
   */
  public void saveAllData() {
    cache.keySet().forEach(this::savePlayerData);
  }

  /**
   * Internal listener class for handling player quit events.
   */
  private class SmartCacheListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
      UUID playerID = event.getPlayer().getUniqueId();

      // Avoid saving data if the player was not in the cache
      if (!cache.containsKey(playerID)) return;

      // Save data and remove from the cache
      plugin.getLogger().config("Saving data for player: " + playerID);
      savePlayerData(playerID);
      remove(playerID);
    }
  }
}
