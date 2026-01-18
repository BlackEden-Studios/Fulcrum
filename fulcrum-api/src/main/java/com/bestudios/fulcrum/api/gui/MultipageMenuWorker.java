package com.bestudios.fulcrum.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A worker that manages multiple pages.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see MenuWorker
 */
public interface MultipageMenuWorker extends MenuWorker {

  /**
   * Retrieves the registry of all pages.
   * Key: Page ID
   * Value: The self-contained MenuData for that page
   */
  @NotNull Map<String, MenuData> getPages();

  /**
   * Helper to get a specific page's data.
   */
  default MenuData getPageData(String pageId) {
    return getPages().get(pageId);
  }

  /**
   * Convenience generator for a page-switching action.
   * <br>
   * Replaces: <code>(p) -> ((LinkedMenu)p.getOpenInventory().getTopInventory().getHolder()).changePage(...)</code>
   *
   * @param pageId The ID of the page to switch to.
   * @return A consumer action ready to be passed to a Blueprint.
   */
  default @NotNull Consumer<Player> changePage(@NotNull String pageId) {
    return player -> {
      Inventory top = player.getOpenInventory().getTopInventory();
      if (top.getHolder() instanceof LinkedMenu menu) {
        menu.changePage(pageId, player);
      } else {
        // Optional: Log warning if this action was triggered outside a LinkedMenu
        player.closeInventory();
      }
    };
  }
}

