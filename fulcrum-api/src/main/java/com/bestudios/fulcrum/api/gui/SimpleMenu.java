package com.bestudios.fulcrum.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Abstract implementation of the Menu interface providing common functionality for GUI menus.
 * <p></p>
 * SimpleMenu provides a standard implementation for creating customizable and interactive menus.
 * It manages the storage of items and their associated actions.
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 * @see Menu
 */
public abstract class SimpleMenu implements Menu {

  protected static final ItemStack PLACEHOLDER_ITEM;
  static {
    PLACEHOLDER_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    final ItemMeta meta = PLACEHOLDER_ITEM.getItemMeta();
    meta.displayName(Component.space());
    PLACEHOLDER_ITEM.setItemMeta(meta);
  }

  private final Inventory inventory;
  private final Consumer<Player>[] actions;
  private final boolean placeholders;

  /**
   * Constructor for SimpleMenu
   *
   * @param rows The number of rows in the menu
   * @param title The title of the menu
   * @param placeholders Whether to use placeholder items for empty slots
   */
  @SuppressWarnings("unchecked")
  public SimpleMenu(@NotNull Rows rows, Component title, boolean placeholders) {
    // Initialize the arrays
    this.actions = (Consumer<Player>[]) new Consumer[rows.getSize()];
    // Create the inventory
    this.inventory = Bukkit.createInventory(this, rows.getSize(), title);
    // Fill the inventory with empty items
    if (placeholders)
      IntStream.range(0, rows.getSize()).forEach(slot -> setItem(slot, PLACEHOLDER_ITEM));
    // Set the menu properties
    this.placeholders = placeholders;
  }

  public SimpleMenu(@NotNull Rows rows, Component title) {
    this(rows, title, false);
  }

  public SimpleMenu(@NotNull Rows rows) {
    this(rows, Component.empty(), false);
  }

  /**
   * Handles click events within the menu inventory.
   * <p></p>
   * Executes the action associated with the clicked slot, if one exists.
   *
   * @param player The player who clicked
   * @param slot The slot that was clicked
   */
  @Override
  public void click(Player player, int slot) {
    final Consumer<Player> action = actions[slot];
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

    getInventory().setItem(slot, item == null && usePlaceholders() ? PLACEHOLDER_ITEM : item);
    actions[slot] = action;
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

  /**
   * Removes an item from the specified slot. It's a convenient alternative to setting the item to null.
   *
   * @param slot The inventory slot to remove the item from
   */
  public void removeItem(int slot) {
    setItem(slot, null);
  }

  /**
   * Abstract method to be implemented by subclasses to set up menu items.
   * <p></p>
   * This method is called before the menu is shown to a player.
   */
  @Override
  public abstract SimpleMenu compose();

  /**
   * Updates multiple items in the menu.
   * @param updates The list of MenuUpdates to apply
   * @return The updated menu
   */
  @Override
  public SimpleMenu update(MenuUpdate[] updates) {
    for (MenuUpdate update : updates) setItem(update);
    return this;
  }

  /**
   * Checks whether placeholders are enabled for empty slots.
   *
   * @return True if placeholders are enabled, false otherwise
   */
  @Override
  public boolean usePlaceholders() {
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
   * Gets the actions in this menu indexed by slot.
   *
   * @return Array of slots to click actions
   */
  @Override
  public Consumer<Player>[] getActions() {
    return actions;
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
   * Enum representing possible menu sizes.
   */
  public enum Rows {
    ONE(9),
    TWO(18),
    THREE(27),
    FOUR(36),
    FIVE(45),
    SIX(54);

    private final int size;

    Rows(int size) {
      this.size = size;
    }

    public int getSize() {
      return size;
    }
  }

}
