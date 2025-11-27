package com.bestudios.fulcrum.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract implementation of the Menu interface providing common functionality for GUI menus.
 * <p>
 * SimpleMenu provides a standard implementation for creating customizable and interactive menus.
 * It manages the disposal of items and their associated actionsMap.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Menu
 */
public abstract class SimpleMenu implements Menu {

  /** Placeholder item used for empty slots */
  protected static final ItemStack PLACEHOLDER_ITEM;

  /* Initialize the placeholder item */
  static {
    PLACEHOLDER_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    final ItemMeta meta = PLACEHOLDER_ITEM.getItemMeta();
    meta.displayName(Component.space());
    PLACEHOLDER_ITEM.setItemMeta(meta);
  }

  /** The inventory backing this menu */
  protected Inventory inventory;
  /** The actionsMap associated with each slot */
  protected List<Consumer<Player>> actions;
  /** Whether to use placeholder items for empty slots */
  protected final boolean placeholders;

  /**
   * Constructor for SimpleMenu with a specified number of rows, the specified title and placeholders enabled
   *
   * @param rows            The number of rows in the menu
   * @param title           The title of the menu
   * @param usePlaceholders Whether to use placeholder items for empty slots
   */
  public SimpleMenu(@NotNull Rows rows, Component title, boolean usePlaceholders) {
    this.inventory = Bukkit.createInventory(this, rows.getSize(), title);
    this.actions = new ArrayList<>(rows.getSize());
    for (int i = 0; i < rows.getSize(); i++) this.actions.add(null);
    this.placeholders = usePlaceholders;

    // Fill the inventory with empty items
    if (usePlaceholders) {
      ItemStack[] items = new ItemStack[rows.getSize()];
      Arrays.fill(items, PLACEHOLDER_ITEM);
      this.inventory.setContents(items);
    }
  }

  /**
   * Constructor for SimpleMenu with a specified number of rows and the specified title
   *
   * @param rows  The number of rows in the menu
   * @param title The title of the menu
   */
  public SimpleMenu(@NotNull Rows rows, Component title) {
    this(rows, title, false);
  }

  /**
   * Constructor for SimpleMenu with a specified number of rows
   *
   * @param rows The number of rows in the menu
   */
  public SimpleMenu(@NotNull Rows rows) {
    this(rows, Component.empty(), false);
  }

  /**
   * Handles click events within the menu inventory.
   * <p>
   * Executes the action associated with the clicked slot, if one exists.
   *
   * @param slot The slot that was clicked
   * @param player The player who clicked
   */
  @Override
  public void click(int slot, Player player) {
    if (!isValidSlot(slot)) return;
    final Consumer<Player> action = actions.get(slot);
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
    if (isValidSlot(slot))
      throw new IllegalArgumentException("Inventory Slot " + slot + " is out of bounds this menu's size");

    getInventory().setItem(slot, item == null && usesPlaceholders() ? PLACEHOLDER_ITEM : item);
    actions.set(slot, action);
  }

  /**
   * Abstract method to be implemented by subclasses to set up menu items.
   * <p>
   * This method is called before the menu is shown to a player.
   */
  @Override
  public abstract SimpleMenu compose();

  /**
   * Checks whether placeholders are enabled for empty slots.
   *
   * @return True if placeholders are enabled, false otherwise
   */
  @Override
  public boolean usesPlaceholders() {
    return placeholders;
  }

  /**
   * Gets the items in this menu.
   *
   * @return The array of items
   */
  @Override
  public ItemStack[] getItems() {
    return inventory.getStorageContents();
  }

  /**
   * Gets the actionsMap in this menu indexed by slot.
   *
   * @return Array of slots to click actionsMap
   */
  @SuppressWarnings("unchecked")
  @Override
  public Consumer<Player>[] getActions() {
    return actions.toArray(new Consumer[0]);
  }

  /**
   * Gets the inventory associated with this menu.
   *
   * @return The Bukkit inventory
   */
  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }

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
