package com.bestudios.fulcrum.system;

import com.bestudios.fulcrum.api.gui.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

  public static final MenuListener INSTANCE = new MenuListener();
  private MenuListener() {}

  @EventHandler
  public void onMenuClicked(InventoryClickEvent event) {
    if (event.getInventory().getHolder() instanceof Menu) {
      event.setCancelled(true);
      ((Menu) event.getInventory().getHolder()).click((Player) event.getWhoClicked(), event.getSlot());
    }
  }
}
