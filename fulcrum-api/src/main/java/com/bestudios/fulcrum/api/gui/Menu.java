package com.bestudios.fulcrum.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Interface for creating interactive menu inventoriesMap.
 * <p>
 * The Menu interface extends InventoryHolder to provide a standardized way
 * to create and manage interactive GUIs with clickable items. It handles
 * item placement, clicks actionsMap, and menu updates.
 * </p>
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see SimpleMenu
 */
public interface Menu extends InventoryHolder {
  /**
   * Handles click events within the menu inventory.
   *
   * @param slot The inventory slot that was clicked
   * @param player The player who clicked
   */
  void click(int slot, Player player);

  /**
   * Sets an item at the specified slot with an associated click action.
   *
   * @param slot The inventory slot to place the item in
   * @param item The ItemStack to place in the slot
   * @param action The Consumer that will be called when the item is clicked
   */
  void setItem(int slot, ItemStack item, Consumer<Player> action);

  /**
   * Sets an item at the specified slot with no click action.
   *
   * @param slot The inventory slot to place the item in
   * @param item The ItemStack to place in the slot
   */
  default void setItem(int slot, ItemStack item) {
    setItem(slot, item, null);
  }

  /**
   * Sets an item at the specified slot with an associated click action.
   *
   * @param update The MenuUpdate to apply
   */
  default void setItem(@NotNull MenuUpdate update) {
    Objects.requireNonNull(update, "MenuUpdate cannot be null");
    setItem(update.slot(), update.item(), update.action());
  }

  /**
   * Removes an item from the specified slot. It's a convenient alternative to setting the item to null.
   *
   * @param slot The inventory slot to remove the item from
   */
  default void removeItem(int slot) {
    setItem(slot, null, null);
  }

  /**
   * Populates the menu with items.
   * <p>
   * This method is called before the menu is shown to a player.
   * Implementations should use this to set up all menu items.
   * </p>
   */
  Menu compose();

  /**
   * Updates multiple items in the menu.
   *
   * @param updates The list of MenuUpdates to apply
   * @return The updated menu
  */
  default Menu update(MenuUpdate @NotNull [] updates) {
    for (MenuUpdate update : updates) setItem(update);
    return this;
  }

  /**
   * Determines whether placeholder items should be used.
   *
   * @return true if the menu uses placeholders, false otherwise
   */
  boolean usesPlaceholders();

  /**
   * Checks if the specified slot is valid for this menu.
   *
   * @param slot The slot to check
   * @return true if the slot is valid, false otherwise
   */
  default boolean isValidSlot(int slot) {
    return slot >= 0 && slot < getInventory().getSize();
  }

  /**
   * Gets a map of all items in the menu indexed by slot number.
   *
   * @return An array of ItemStacks
   */
  @NotNull ItemStack[] getItems();

  /**
   * Gets a map of all click actionsMap in the menu indexed by slot number.
   *
   * @return An array of click action Consumers
   */
  @NotNull Consumer<Player>[] getActions();

  /**
   * Opens this menu for a player.
   * <p>
   * This default implementation handles the common steps for opening menus:
   * setting placeholders if needed, populating items, and opening the inventory.
   * </p>
   *
   * @param player The player to show the menu to
   */
  default void open(Player player) {
    if (getInventory().isEmpty()) compose();

    player.openInventory(getInventory());
  }

  /**
   * Closes this menu for a player.
   *
   * @param player The player to close the menu for
   */
  default void close(Player player) {
    player.closeInventory();
  }

  /**
   * Represents a single update to a menu item.
   * @param slot    Slot to update
   * @param item    Item to set
   * @param action  Action to set
   */
  record MenuUpdate(int slot, @Nullable ItemStack item, @Nullable Consumer<Player> action) {}
}