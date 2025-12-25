package com.bestudios.fulcrum.api.gui;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A menu implementation that links multiple page states together.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see SimpleMenu
 */
public class LinkedMenu extends SimpleMenu {

  /** The current page ID */
  protected String currentPageId;
  /** The worker that manages the pages */
  protected final LinkedMenuWorker linkedWorker;

  /**
   * Constructor for LinkedMenu.
   * @param rows   Rows to use.
   * @param worker The worker to use.
   */
  public LinkedMenu(@NotNull Rows rows, @NotNull LinkedMenuWorker worker) {
    super(rows, worker);
    this.linkedWorker = worker;
    this.currentPageId = "starting"; // Default entry point
  }

  // --- Core Logic Swapping ---

  @Override
  protected @NotNull MenuData getCurrentData() {
    MenuData pageData = linkedWorker.getPageData(currentPageId);
    if (pageData == null) {
      // Fallback or explicit creation if the page is missing
      throw new IllegalStateException("Page '" + currentPageId + "' does not exist in the worker!");
    }
    return pageData;
  }

  // --- Page Navigation ---

  /**
   * Switches the view to a new page.
   *
   * @param pageId The ID of the page to open.
   * @param viewer The player (needed to refresh the inventory).
   */
  @CanIgnoreReturnValue
  public LinkedMenu changePage(@NotNull String pageId, @NotNull Player viewer) {
    this.currentPageId = pageId;

    // Trigger the standard open() logic.
    // Because we overrode getCurrentData(), open() will now:
    // 1. Check if the NEW page isReady()
    // 2. If yes -> Render new blueprints
    // 3. If not -> Show loading screen loop until that specific page is ready
    this.open(viewer);

    return this;
  }

  /**
   * Gets all available page IDs.
   * @return A set of all available page IDs.
   */
  public Set<String> getPageIds() {
    return linkedWorker.getPages().keySet();
  }

  /**
   * Gets the current page ID.
   * @return The current page ID.
   */
  public String getCurrentPageId() {
    return currentPageId;
  }

  /**
   * A worker that manages multiple pages.
   *
   * @author Bestialus
   * @version 1.0
   * @since   1.0
   * @see MenuWorker
   */
  public interface LinkedMenuWorker extends MenuWorker {

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
}