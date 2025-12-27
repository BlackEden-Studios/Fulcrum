package com.bestudios.fulcrum.api.gui;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract implementation of MenuWorker that handles the async lifecycle.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see MenuWorker
 * @see Menu
 * @see MenuData
 */
public abstract class AsyncMenuWorker implements MenuWorker {

  protected final JavaPlugin plugin;
  protected final MenuData data;

  /**
   * @param plugin The plugin instance (injected).
   * @param data   The data container to populate (injected).
   */
  public AsyncMenuWorker(@NotNull JavaPlugin plugin, @NotNull MenuData data) {
    this.plugin = plugin;
    this.data = data;
  }

  @Override
  public @NotNull JavaPlugin getPlugin() {
    return plugin;
  }

  @Override
  public @NotNull MenuData getData() {
    return data;
  }

  /**
   * Trigger the worker logic.
   * Runs {@link #populate()} on a background thread and automatically marks
   * the data as ready when finished.
   */
  @Override
  public void start() {
    update(this::populate); // Reuse update logic for initial start
  }

  /**
   * Updates the data asynchronously.
   * <br>
   * Enforces the Ready/Busy lifecycle.
   */
  @Override
  public void update(Runnable action) {
    // 1. Mark as Busy (Sync) - UI updates to "Loading..."
    data.markAsBusy();

    // 2. Run Logic (Async)
    CompletableFuture
    .runAsync(action)
        .exceptionally(ex -> {
          plugin.getLogger().severe("Error updating menu data: " + ex.getMessage());
          ex.printStackTrace();
          return null;
        })
    // 3. Mark as Ready (Sync/Async safe) - UI updates to Content
    .thenRunAsync(data::markAsReady, Bukkit.getScheduler().getMainThreadExecutor(plugin));
  }

  /**
   * The logic to generate elements and add them to the data.
   * <br>
   * <b>WARNING:</b> This runs on an ASYNC thread. Do not call Bukkit API methods
   * (like creating inventories or spawning entities) here.
   * Use {@link MenuElement} to prepare your items safely.
   */
  protected abstract void populate();
}