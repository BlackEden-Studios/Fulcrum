package com.bestudios.fulcrum.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Interface for creating interactive menu inventories.
 * <p>
 * The Menu interface extends InventoryHolder to provide a standardized way
 * to create and manage interactive GUIs with clickable items. It handles
 * item placement, clicks actions, and menu updates.
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
   * @param player The player who clicked
   * @param slot The inventory slot that was clicked
   */
  void click(Player player, int slot);

  /**
   * Sets an item at the specified slot with no click action.
   *
   * @param slot The inventory slot to place the item in
   * @param item The ItemStack to place in the slot
   */
  void setItem(int slot, ItemStack item);

  /**
   * Sets an item at the specified slot with an associated click action.
   *
   * @param slot The inventory slot to place the item in
   * @param item The ItemStack to place in the slot
   * @param action The Consumer that will be called when the item is clicked
   */
  void setItem(int slot, ItemStack item, Consumer<Player> action);

  /**
   * Sets an item at the specified slot with an associated click action.
   *
   * @param update The MenuUpdate to apply
   */
  void setItem(MenuUpdate update);

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
  Menu update(ArrayList<MenuUpdate> updates);

  Menu update(MenuUpdate[] updates);

  /**
   * Determines whether placeholder items should be used.
   *
   * @return true if the menu should use placeholders, false otherwise
   */
  boolean usePlaceholders();

  /**
   * Gets a map of all items in the menu indexed by slot number.
   *
   * @return An array of ItemStacks
   */
  @NotNull ItemStack[] getItems();

  /**
   * Gets a map of all click actions in the menu indexed by slot number.
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

  record MenuUpdate(int slot, @Nullable ItemStack item, @Nullable Consumer<Player> action) {}
}