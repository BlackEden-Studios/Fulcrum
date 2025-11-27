package com.bestudios.fulcrum.api.gui;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A menu implementation that links multiple page states together.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see SimpleMenu
 */
public abstract class LinkedMenu extends SimpleMenu {

  /** The ID of the current page */
  protected String currentPageId;

  /**
   * Backing storage for items.
   * We store ItemStack[] arrays, not Inventory objects, to allow fast switching
   * of contents without closing the player's window.
   */
  protected final Map<String, ItemStack[]> contentsMap;

  /**
   * Backing storage for actions.
   * Stores Lists to be compatible with SimpleMenu's internal structure.
   */
  protected final Map<String, List<Consumer<Player>>> actionsMap;

  // -- Constructors --

  public LinkedMenu(@NotNull Rows rows, Component title, boolean usePlaceholders) {
    super(rows, title, usePlaceholders);

    // Initialize storage
    this.contentsMap = new ConcurrentHashMap<>();
    this.actionsMap = new ConcurrentHashMap<>();

    // Initialize the starting page immediately so the menu is never in an invalid state
    this.currentPageId = "starting";
    createPage(currentPageId);

    // Force the swap to bind 'this.actions' to the starting page
    changePage(currentPageId);
  }

  public LinkedMenu(@NotNull Rows rows, Component title) {
    this(rows, title, false);
  }

  public LinkedMenu(@NotNull Rows rows) {
    this(rows, null, false);
  }

  /**
   * Switches the menu view to a specific page.
   * <p>
   * This method performs the Reference Swap:
   * <ol>
   *   <li>Updates the visual inventory contents.</li>
   *   <li>Points the menu actions to the new page's action list.</li>
   * </ol>
   *
   * @param pageID The ID of the page to switch to
   * @return this instance
   */
  @CanIgnoreReturnValue
  public LinkedMenu changePage(@NotNull String pageID) {
    // 1. Ensure page exists
    createPage(pageID);

    // 2. Update state tracker
    this.currentPageId = pageID;

    // 3. Action update
    this.actions = actionsMap.get(pageID);

    // 4. Visual update
    ItemStack[] pageContents = contentsMap.get(pageID);
    getInventory().setContents(pageContents);

    return this;
  }

  /**
   * Creates a new page if it doesn't exist.
   * Initializes the backing arrays and lists.
   *
   * @param pageID The ID of the page to create
   * @return this instance
   */
  @CanIgnoreReturnValue
  public LinkedMenu createPage(@NotNull String pageID) {
    if (contentsMap.containsKey(pageID) && actionsMap.containsKey(pageID)) return this;

    int size = getInventory().getSize();

    // 1. Initialize Items
    ItemStack[] items = new ItemStack[size];
    if (usesPlaceholders())
      Arrays.fill(items, PLACEHOLDER_ITEM);
    contentsMap.put(pageID, items);

    // 2. Initialize Actions
    List<Consumer<Player>> actionList = new ArrayList<>(size);
    for (int i = 0; i < getInventory().getSize(); i++) actionList.add(null);
    actionsMap.put(pageID, actionList);

    return this;
  }

  // -- Item Setting Logic --

  @Override
  public void setItem(int slot, @Nullable ItemStack item, @Nullable Consumer<Player> action) {
    // Update the item in the backing inventory
    super.setItem(slot, item, action);
    // Save the reference in the backing map
    contentsMap.get(currentPageId)[slot] = getInventory().getItem(slot);
  }

  /**
   * Changes an item on a specific page.
   *
   * @param slot   The slot to modify
   * @param item   The item to set
   * @param action The action to set
   * @param pageId The ID of the page to modify
   */
  public void setItem(int slot, @Nullable ItemStack item, @Nullable Consumer<Player> action, @NotNull String pageId) {
    if (slot < 0 || slot >= getInventory().getSize())
      throw new IllegalArgumentException("Slot " + slot + " out of bounds");

    // 2. If we modify the current page, we must update the live view immediately
    if (pageId.equals(currentPageId)) {
      setItem(slot, item, action);
      return;
    }

    createPage(pageId);
    // Update Storage
    contentsMap.get(pageId)[slot] = (item == null && usesPlaceholders()) ? PLACEHOLDER_ITEM : item;
    actionsMap.get(pageId).set(slot, action);


  }

  // -- Convenience Overloads --

  /**
   * Changes an item on a specific page.
   *
   * @param slot   The slot to modify
   * @param item   The item to set
   * @param pageId The ID of the page to modify
   */
  public void setItem(int slot, ItemStack item, String pageId) {
    setItem(slot, item, null, pageId);
  }

  /**
   * Changes an item on a specific page.
   *
   * @param update The update to apply
   * @param pageId The ID of the page to modify
   */
  public void setItem(@NotNull MenuUpdate update, String pageId) {
    setItem(update.slot(), update.item(), update.action(), pageId);
  }

  /**
   * Removes an item from a specific page.
   *
   * @param slot   The slot in which to remove the item
   * @param pageId The ID of the page to modify
   */
  public void removeItem(int slot, String pageId) {
    setItem(slot, null, null, pageId);
  }

  /**
   * Bulk update for a specific page
   *
   * @param updates The updates to apply
   * @param pageId  The ID of the page to modify
   * @return this instance
   */
  public LinkedMenu update(MenuUpdate @NotNull [] updates, String pageId) {
    for (MenuUpdate update : updates) setItem(update, pageId);
    return this;
  }

  @Override
  public abstract LinkedMenu compose();

  /**
   * Gets the ID of the current page.
   * @return The current page ID
   */
  public String getCurrentPageId() {
    return currentPageId;
  }

  /**
  * Gets the IDs of all pages in this menu.
  * @return The IDs of all pages
  */
  public Set<String> getPageIds() {
    return contentsMap.keySet();
  }

  /**
   * Gets the stored items for a specific page.
   *
   * @param pageId The ID of the page to retrieve items for
   * @return The items stored in the specified page
   */
  public ItemStack[] getItems(String pageId) {
    return contentsMap.get(pageId);
  }

  /**
   * Gets the stored actions for a specific page.
   *
   * @param pageId The ID of the page to retrieve actions for
   * @return The actions stored in the specified page
   */
  @SuppressWarnings("unchecked")
  public Consumer<Player>[] getActions(String pageId) {
    List<Consumer<Player>> list = actionsMap.get(pageId);
    return list == null ? new Consumer[0] : list.toArray(new Consumer[0]);
  }
}