package com.bestudios.fulcrum.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of MultipageMenuWorker that handles the async lifecycle.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see LinkedMenu
 * @see AsyncMenuWorker
 */
public abstract class AsyncLinkedWorker extends AsyncMenuWorker implements MultipageMenuWorker {

  // The Registry: Map<PageID, MenuData>
  private final Map<String, MenuData> pages = new ConcurrentHashMap<>();

  public AsyncLinkedWorker(JavaPlugin plugin, MenuData defaultData) {
    super(plugin, defaultData);
    // Register the default data as the "starting" page
    pages.put("starting", defaultData);
  }

  @Override
  public @NotNull Map<String, MenuData> getPages() {
    return pages;
  }

  /**
   * Updates a specific page's data safely.
   * @param pageId The ID of the page to update.
   * @param action The update logic.
   */
  public void updatePage(String pageId, Runnable action) {
    MenuData pageData = getPages().get(pageId);
    if (pageData == null) return;

    // Lock the specific page
    pageData.markAsBusy();

    CompletableFuture
    .runAsync(action)
        .exceptionally(ex -> {
          plugin.getLogger().severe("Error updating page " + pageId + ": " + ex.getMessage());
          ex.printStackTrace();
          return null;
        })
    .thenRun(pageData::markAsReady);
  }

  /**
   * Helper to create a new page and register it.
   */
  protected MenuData createPage(String pageId, Component title) {
    MenuData newData = MenuData.create(getData().provider(), title);
    pages.put(pageId, newData);
    return newData;
  }

}