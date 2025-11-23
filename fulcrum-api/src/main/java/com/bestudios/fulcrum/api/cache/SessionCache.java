package com.bestudios.fulcrum.api.cache;

import com.bestudios.fulcrum.api.util.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * SessionCache extends SmartCache to automatically load player data on join
 * and save it on quit, maintaining data throughout the player's session.
 *
 * @param <T> The type of player data to be cached
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see SmartCache
 */
public class SessionCache<T> extends SmartCache<T> {
  /**
   * The PlayerDataLoader responsible for loading and creating player data.
   */
  private final PlayerDataLoader<T> dataLoader;

  /**
   * Creates a new SessionCache with the specified capacity and data loader.
   *
   * @param maxEntries The maximum number of entries expected
   * @param plugin The plugin instance
   * @param dataLoader The loader responsible for creating/retrieving player data
   */
  public SessionCache(int maxEntries, @NotNull Plugin plugin, @NotNull PlayerDataLoader<T> dataLoader) {
    super(maxEntries, plugin);
    this.dataLoader = Objects.requireNonNull(dataLoader, Utils.messageRequireNonNull("data loader"));
    SessionCacheListener sessionListener = new SessionCacheListener();

    plugin.getServer().getPluginManager().registerEvents(sessionListener, plugin);
  }

  /**
   * Creates a new SessionCache sized for maximum server capacity.
   *
   * @param plugin The plugin instance
   * @param dataLoader The loader responsible for creating/retrieving player data
   */
  public SessionCache(@NotNull Plugin plugin, @NotNull PlayerDataLoader<T> dataLoader) {
    this(plugin.getServer().getMaxPlayers(), plugin, dataLoader);
  }

  /**
   * Loads player data from persistent storage or creates new default data.
   * This method uses the configured PlayerDataLoader.
   *
   * @param playerID The UUID of the player
   * @return The loaded or newly created player data
   */
  protected T loadPlayerData(@NotNull UUID playerID) {
    return dataLoader.load(playerID);
  }

  /**
   * Internal listener for handling player join events.
   */
  private class SessionCacheListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
      UUID playerID = event.getPlayer().getUniqueId();

      // Avoid reloading if already cached (e.g., from plugin reload)
      if (!cache.containsKey(playerID)) {
        plugin.getLogger().fine("Loading data for player: " + playerID);
        T data = loadPlayerData(playerID);
        cache.put(playerID, data);
      }
    }
  }
}
