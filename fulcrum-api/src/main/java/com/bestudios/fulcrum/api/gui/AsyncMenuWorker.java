package com.bestudios.fulcrum.api.gui;

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
    // Run the populate logic in the common ForkJoinPool (Standard Async)
    // FINISH: Mark data as ready so waiting Menus can open
    CompletableFuture.runAsync(this::populate)
            .exceptionally(ex -> {
              plugin.getLogger().severe("Error populating menu data: " + ex.getMessage());
              ex.printStackTrace();
              return null;
            })
            .thenRun(data::markAsReady);
  }

  /**
   * The logic to generate blueprints and add them to the data.
   * <br>
   * <b>WARNING:</b> This runs on an ASYNC thread. Do not call Bukkit API methods
   * (like creating inventories or spawning entities) here.
   * Use {@link MenuElement} to prepare your items safely.
   */
  protected abstract void populate();
}