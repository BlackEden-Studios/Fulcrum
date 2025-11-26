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
 * @see PlayerDataLoader
 * @see PlayerDataSaver
 */
public class SessionCache<T> extends SmartCache<T> {
  /** The PlayerDataLoader responsible for loading and creating player data */
  private final PlayerDataLoader<T> dataLoader;

  /**
   * Creates a new SessionCache with the specified capacity and data loader.
   *
   * @param maxEntries The maximum number of entries expected
   * @param pluginRef The plugin instance
   * @param dataLoaderRef The loader responsible for creating/retrieving player data
   */
  public SessionCache(
          int maxEntries,
          @NotNull Plugin pluginRef,
          @NotNull PlayerDataSaver<T>  dataSaverRef,
          @NotNull PlayerDataLoader<T> dataLoaderRef
  ) {
    super(maxEntries, pluginRef, dataSaverRef);
    dataLoader = Objects.requireNonNull(dataLoaderRef, Utils.messageRequireNonNull("data loader"));

    // Register listener for player join events
    SessionCacheListener sessionListener = new SessionCacheListener();
    this.plugin.getServer().getPluginManager().registerEvents(sessionListener, this.plugin);
  }

  /**
   * Creates a new SessionCache sized for maximum server capacity.
   *
   * @param pluginRef The pluginRef instance
   * @param dataLoaderRef The loader responsible for creating/retrieving player data
   */
  public SessionCache(
          @NotNull Plugin pluginRef,
          @NotNull PlayerDataSaver<T> dataSaverRef,
          @NotNull PlayerDataLoader<T> dataLoaderRef
  ) {
    this(pluginRef.getServer().getMaxPlayers(), pluginRef, dataSaverRef, dataLoaderRef);
  }

  /**
   * Internal listener for handling player join events.
   */
  private class SessionCacheListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
      UUID playerID = event.getPlayer().getUniqueId();

      // Avoid reloading if already cached (e.g., from plugin reload)
      if ( cache.containsKey(playerID)) return;

      // Load and cache player data
      plugin.getLogger().fine("Loading data for player: " + playerID);
      T data = dataLoader.load(playerID);
      cache.put(playerID, data);

    }
  }
}
