package com.bestudios.fulcrum.system;

import com.bestudios.fulcrum.api.gui.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Listens for clicks on menus and handles them accordingly.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see Menu
 */
public class MenuListener implements Listener {

  public static final MenuListener INSTANCE = new MenuListener();
  private MenuListener() {}

  @EventHandler
  public void onMenuClicked(InventoryClickEvent event) {
    if (event.getInventory().getHolder() instanceof Menu) {
      event.setCancelled(true);
      ((Menu) event.getInventory().getHolder()).click(event.getSlot(), (Player) event.getWhoClicked());
    }
  }
}
