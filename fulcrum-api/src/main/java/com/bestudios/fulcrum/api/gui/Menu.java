package com.bestudios.fulcrum.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interface for creating interactive menu inventories.
 * <br>
 * The Menu interface extends InventoryHolder to provide a standardized way
 * to create and manage interactive GUIs with clickable items. It handles
 * item placement, clicks actionsMap, and menu updates.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
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
   * Removes an item from the specified slot. It's a convenient alternative to setting the item to null.
   *
   * @param slot The inventory slot to remove the item from
   */
  default void removeItem(int slot) {
    setItem(slot, null, null);
  }

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
   *
   * @param player The player to show the menu to
   */
  default void open(@NotNull Player player) {
    player.openInventory(getInventory());
  }

  /**
   * Closes this menu for a player.
   *
   * @param player The player to close the menu for
   */
  default void close(@NotNull Player player) {
    player.closeInventory();
  }

  /**
   * A list of all slots in the player's inventory.
   * <br>
   * It lists hotbar slots, the main inventory slots, and the offhand slot.
   */
  ArrayList<Integer> playerInventorySlotList = new ArrayList<>(List.of(
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
          28, 29, 30, 31, 32, 33, 34, 35, 40
  ));

  /**
   * Enum representing possible menu sizes (Chest rows).
   */
  public enum Rows {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6);

    /** The number of slots in the inventory */
    private final int size;

    /**
     * The number of rows in the inventory (1-6)
     * @param rows The number of rows
     */
    @Contract(pure = true)
    Rows(int rows) {
      // Calculate the inventory size (MUST be a multiple of 9)
      this.size = rows * 9;
    }

    /**
     * @return The total slot count required for Bukkit.createInventory
     */
    @Contract(pure = true)
    public int getSize() {
      return size;
    }

    /**
     * @return The number of rows (1-6)
     */
    @Contract(pure = true)
    public int getRowCount() {
      return size/9;
    }

    /**
     * Utility to safely get an enum from a slot count (e.g. from inventory size).
     * @param slots The number of slots in the inventory
     * @throws IllegalArgumentException if slots are invalid
     */
    public static Rows fromSlotCount(int slots) {
      return fromInt(slots/9);
    }

    /**
     * Utility to safely get an enum from a raw integer (e.g. from config).
     * @param rows The number of rows (1-6)
     * @throws IllegalArgumentException if rows are invalid
     */
    public static Rows fromInt(int rows) {
      if (rows >= 1 && rows <= 6) return Rows.values()[rows-1];
      throw new IllegalArgumentException("Invalid row count: " + rows);
    }
  }
}