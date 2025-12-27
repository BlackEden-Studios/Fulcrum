package com.bestudios.fulcrum.api.gui;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
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
    if (getCurrentData().isReady()) {
      // 1. Data is ready: Render it immediately
      renderAndShow(player);
    } else {
      // 2. Data is NOT ready: Schedule a check
      player.sendMessage(Component.text("§7Loading menu...")); // Optional UX feedback

      new BukkitRunnable() {
        @Override
        public void run() {
          if (player.isOnline()) {
            // Recursive call: will check isReady() again
            open(player);
          }
        }
      }.runTaskLater(worker.getPlugin(), 10L); // 10 Ticks = Half a second
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
   * Internal method to translate Blueprints -> Inventory
   */
  private void renderAndShow(Player player) {
    // Clear previous state (optional, but good for safety)
    this.inventory.clear();

    // Apply Blueprints
    for (MenuElement bp : this.getCurrentData().elements().values()) {
      if (!isValidSlot(bp.slot())) continue;

      // 1. Set Visual Item
      this.inventory.setItem(bp.slot(), bp.toItemStack());
    }

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
    Consumer<Player> action = this.getCurrentData().elements().get(slot).action();
    if (action != null) action.accept(player);
  }

  // --- Boilerplate Implementations ---

  @Override
  public void setItem(int slot, ItemStack item, Consumer<Player> action) {
    this.inventory.setItem(slot, item);
    this.getCurrentData().elements().put(slot, MenuElement.of(slot, item, action));
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