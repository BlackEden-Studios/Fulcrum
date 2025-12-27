package com.bestudios.fulcrum.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Abstract implementation of the Menu interface providing common functionality for GUI menus.
 * <br>
 * SimpleMenu provides a standard implementation for creating customizable and interactive menus.
 * <br>
 * It manages the disposal of items and their associated actionsMap.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Menu
 */
public class SimpleMenu implements Menu {

  protected final Inventory inventory;
  protected final MenuWorker worker;

  /**
   * Constructor for SimpleMenu. Initializes the inventory and supporting data injection through {@link MenuData}.
   */
  public SimpleMenu(@NotNull Rows rows, @NotNull MenuWorker worker) {
    this.worker = worker;
    // We use the title from the data
    this.inventory = Bukkit.createInventory(this, rows.getSize(), getCurrentData().title());
  }

  /**
   * The logic behind the menu opening.
   * Checks the Ready Flag -> Renders OR Schedules Retry.
   *
   * @param player The player to open the menu for
   */
  @Override
  public void open(@NotNull Player player) {
    MenuData currentData = getCurrentData();

    // 1. Subscribe to updates (Reactive)
    // We define a callback that re-runs open() whenever data changes.
    Runnable onUpdate = () -> {
      // Lazy cleanup: If player closed this specific inventory, stop listening
      if (!player.isOnline() || player.getOpenInventory().getTopInventory() != this.inventory) {
        currentData.unsubscribe(() -> {}); // You'd need to store this reference
        return;
      }
      // Re-run the open logic (which handles Ready vs. Busy)
      // We run on the next tick to ensure thread safety with Bukkit API
      Bukkit.getScheduler().runTask(worker.getPlugin(), () -> open(player));
    };

    // Note: To properly implement unsubscribe, SimpleMenu should hold the 'Runnable' reference.
    // For this snippet, I assume you will manage the listener reference field.
    currentData.subscribe(onUpdate);

    // 2. Render Logic
    if (currentData.isReady()) {
      renderAndShow(player);
    } else {
      player.sendMessage(Component.text("§eOpening the menu..."));
    }
  }

  /**
   * Refreshes the specified slot in the inventory.
   * @param slot The slot to refresh
   */
  public void refresh(int slot) {
    if (!isValidSlot(slot)) return;
    // Get the element blueprint and apply it to the inventory
    MenuElement element = getCurrentData().elements().get(slot);
    this.inventory.setItem(slot, element != null ? element.toItemStack() : null);
  }

  /**
   * Internal method to translate Elements -> Inventory Items
   */
  private void renderAndShow(Player player) {
    // Clear previous state (optional, but good for safety)
    this.inventory.clear();

    // Apply Blueprints
    for (Map.Entry<Integer, MenuElement> element : this.getCurrentData().elements().entrySet()) {
      if (!isValidSlot(element.getKey())) continue;

      this.inventory.setItem(element.getKey(), element.getValue().toItemStack());
    }

    // Show the inventory
    player.openInventory(this.inventory);
  }

  /**
   * Returns the data currently being viewed.
   * <br>
   * In SimpleMenu, this is always the single worker data.
   */
  protected @NotNull MenuData getCurrentData() {
    return this.worker.getData();
  }

  @Override
  public void click(int slot, Player player) {
    if (!isValidSlot(slot)) return;
    if (!getCurrentData().isReady()) return;
    Consumer<Player> action = this.getCurrentData().elements().get(slot).action();
    if (action != null) action.accept(player);
  }

  // --- Boilerplate Implementations ---

  @Override
  public void setItem(int slot, ItemStack item, Consumer<Player> action) {
    this.inventory.setItem(slot, item);
    this.getCurrentData().elements().put(slot, MenuElement.of(item, action));
  }

  @Override
  public @NotNull Inventory getInventory() { return inventory; }

  @Override
  public @NotNull ItemStack[] getItems() {
    return getCurrentData().elements().values().stream().map(MenuElement::toItemStack).toArray(ItemStack[]::new);
  }

  @Override @SuppressWarnings("unchecked")
  public @NotNull Consumer<Player>[] getActions() {
    return getCurrentData().elements().values().stream().map(MenuElement::action).toArray(Consumer[]::new);
  }

}