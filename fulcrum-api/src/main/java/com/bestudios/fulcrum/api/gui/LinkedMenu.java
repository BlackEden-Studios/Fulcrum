package com.bestudios.fulcrum.api.gui;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * A menu implementation that links multiple inventories together, allowing navigation between them.
 *
 * <p>LinkedMenu enables creating multi-page menus or menu systems where click actions
 * can transition players between different inventory views seamlessly.
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see SimpleMenu
 */
public abstract class LinkedMenu extends SimpleMenu {

  private String currentPageId;
  private final Map<String, ItemStack[]> items;
  private final Map<String, Consumer<Player>[]> actions;

  public LinkedMenu(@NotNull Rows rows, Component title, boolean placeholders) {
    super(rows, title, placeholders);
    // Initialize the maps
    items = new ConcurrentHashMap<>();
    actions = new ConcurrentHashMap<>();
    // Create the default page
    currentPageId = "default";
    createPage(currentPageId);
    // Fill the inventory with empty items
  }

  public LinkedMenu(@NotNull Rows rows, Component title) {
    this(rows, title,false);
  }

  public LinkedMenu(@NotNull Rows rows) {
    this(rows, null, false);
  }

  @Override
  public void click(Player player, int slot) {
    final Consumer<Player> action = actions.get(currentPageId)[slot];
    if (action != null) action.accept(player);
  }

  /**
   * Sets an item at the specified slot with an associated click action.
   *
   * @param slot The inventory slot to place the item in
   * @param item The ItemStack to place in the slot
   * @param action The action to execute when the item is clicked
   */
  @Override
  public void setItem(int slot, ItemStack item, Consumer<Player> action) {
    if (slot < 0 || slot >= getInventory().getSize())
      throw new IllegalArgumentException("Slot " + slot + " is out of bounds for inventory size " + getInventory().getSize());
    // Assure the page exists
    createPage(currentPageId);

    getInventory().setItem(slot, item == null && usePlaceholders() ? PLACEHOLDER_ITEM : item);
    items.get(currentPageId)[slot] = item == null && usePlaceholders() ? PLACEHOLDER_ITEM : item;
    actions.get(currentPageId)[slot] = action;
  }

  /**
   * Sets an item at the specified slot with no click action.
   *
   * @param slot The inventory slot to place the item in
   * @param item The ItemStack to place in the slot
   */
  @Override
  public void setItem(int slot, ItemStack item) {
    setItem(slot, item, player -> {});
  }

  /**
   * Sets an item at the specified slot with an associated click action.
   *
   * @param update The MenuUpdate to apply
   */
  @Override
  public void setItem(MenuUpdate update) {
    setItem(update.slot(), update.item(), update.action());
  }

  public void setItem(int slot, ItemStack item, Consumer<Player> action, String pageId) {
    if (slot < 0 || slot >= getInventory().getSize())
      throw new IllegalArgumentException("Slot " + slot + " is out of bounds for inventory size " + getInventory().getSize());
    // Assure the page exists
    createPage(pageId);

    items.get(pageId)[slot] = item == null && usePlaceholders() ? PLACEHOLDER_ITEM : item;
    actions.get(pageId)[slot] = action;
  }

  public void setItem(int slot, ItemStack item, String pageId) {
    setItem(slot, item, player -> {}, pageId);
  }

  public void setItem(MenuUpdate update, String pageId) {
    setItem(update.slot(), update.item(), update.action(), pageId);
  }

  /**
   * Removes an item from the specified slot. It's a convenient alternative to setting the item to null.
   *
   * @param slot The inventory slot to remove the item from
   */
  public void removeItem(int slot, String pageId) {
    setItem(slot, null, pageId);
  }

  /**
   * Abstract method to be implemented by subclasses to set up menu items.
   * <p></p>
   * This method is called before the menu is shown to a player.
   */
  @Override
  public abstract LinkedMenu compose();

  /**
   * Updates multiple items in the menu.
   * @param updates The list of MenuUpdates to apply
   * @return The updated menu
   */
  @Override
  public LinkedMenu update(MenuUpdate[] updates) {
    for (MenuUpdate update : updates) setItem(update);
    return this;
  }

  public LinkedMenu update(MenuUpdate[] updates, String pageId) {
    for (MenuUpdate update : updates) setItem(update, pageId);
    return this;
  }

  /**
   * Gets the items in this menu.
   *
   * @return The array of items
   */
  public ItemStack[] getItems(String pageId) {
    return items.get(pageId);
  }

  /**
   * Gets the actions in this menu indexed by slot.
   *
   * @return Array of slots to click actions
   */
  public Consumer<Player>[] getActions(String pageId) {
    return actions.get(pageId);
  }

  public LinkedMenu changePage(String pageId) {
    createPage(pageId);
    this.currentPageId = pageId;
    IntStream.range(0, getInventory().getSize()).forEach(
            slot -> {
              ItemStack item = items.get(this.currentPageId)[slot];
              getInventory().setItem(slot, item == null && usePlaceholders() ? PLACEHOLDER_ITEM : item);
            }
    );
    return this;
  }

  @CanIgnoreReturnValue
  @SuppressWarnings({"unchecked"})
  public LinkedMenu createPage(String id) {
    if (!items.containsKey(id) || !actions.containsKey(id)) {
      items.put(id, new ItemStack[getInventory().getSize()]);
      actions.put(id, new Consumer[getInventory().getSize()]);
    }
    return this;
  }

  public String getCurrentPageId() {
    return currentPageId;
  }

  public void setCurrentPageId(String currentPageId) {
    this.currentPageId = currentPageId;
  }


}
