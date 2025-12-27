package com.bestudios.fulcrum.api.gui;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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
  protected final MultipageMenuWorker linkedWorker;

  /**
   * Constructor for LinkedMenu.
   * @param rows   Rows to use.
   * @param worker The worker to use.
   */
  public LinkedMenu(@NotNull Rows rows, @NotNull MultipageMenuWorker worker) {
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
    // 2. If yes -> Render new elements
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

}