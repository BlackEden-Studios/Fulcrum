package com.bestudios.fulcrum.api.cache;

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
 * @since 1.0
 * @see SessionCache
 */
public class SmartCache<T> {
  protected final ConcurrentHashMap<UUID, T> cache;
  protected final Plugin plugin;

  /**
   * Creates a new SmartCache with a specified maximum number of entries.
   *
   * @param maxEntries The maximum number of entries expected in the cache
   * @param plugin The plugin instance
   */
  public SmartCache(int maxEntries, @NotNull Plugin plugin) {
    this.cache = new ConcurrentHashMap<>(maxEntries);
    this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
    SmartCacheListener listener = new SmartCacheListener();

    plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    plugin.getLogger().config("Registered " + this.getClass().getSimpleName() +
            " with capacity for " + maxEntries + " entries");
  }

  /**
   * Creates a new SmartCache sized to match the server's configured maximum player count.
   *
   * @param plugin The plugin instance
   */
  public SmartCache(@NotNull Plugin plugin) {
    this(plugin.getServer().getMaxPlayers(), plugin);
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
      if (cache.containsKey(playerID)) {
        plugin.getLogger().config("Saving data for player: " + playerID);
        savePlayerData(playerID);
        remove(playerID);
      }
    }
  }

  /**
   * Default error message for null player IDs.
   */
  protected final String NULL_ID_ERROR = "Player ID cannot be null";
}
