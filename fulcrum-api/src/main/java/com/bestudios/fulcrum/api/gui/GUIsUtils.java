package com.bestudios.fulcrum.api.gui;

import com.bestudios.fulcrum.api.service.customitem.CustomItemsService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GUIsUtils {

  /**
   * Attempts to process a transaction for a player, checking if they have all required materials.
   * <br>
   * If it is true, removes them and returns success. If not, returns failure.
   *
   * @param player       The player performing the transaction.
   * @param requirements The materials required for the transaction.
   * @return true if the transaction was successful, false otherwise.
   */
  public static boolean inventoryTransaction(
          @NotNull Player player,
          @NotNull Map<String, Integer> requirements,
          @NotNull CustomItemsService service
  ) {
    Inventory inv = player.getInventory();
    Map<ItemStack, Integer> items = new ConcurrentHashMap<>();

    // 1. Validation Phase: Check if the player has all items
    for (int slot: playerInventorySlotList) {
      ItemStack item = inv.getItem(slot);
      // If the slot is empty, skip it
      if (item == null || item.getType().isAir()) continue;

      String ID = service.getItemNamespaceID(item);
      // If the item is not in the requirements, skip it
      if (!requirements.containsKey(ID)) continue;
      // Check if the player has enough items, else update the requirements and try to find another stack
      if (requirements.get(ID) - item.getAmount() < 0) {
        items.put(item, requirements.get(ID));
        requirements.remove(ID);
      } else {
        items.put(item, item.getAmount());
        requirements.put(ID, requirements.get(ID) - item.getAmount());
      }
    }
    // If the requirements' map is not empty, the player lacks some items, so the transaction failed
    if (!requirements.isEmpty()) return false;

    // 2. Transaction Phase: Remove items
    for (Map.Entry<ItemStack, Integer> entry : items.entrySet())
      entry.getKey().setAmount(entry.getValue());

    return true;
  }

  public static final ArrayList<Integer> playerInventorySlotList = new ArrayList<>(List.of(
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
          28, 29, 30, 31, 32, 33, 34, 35, 40
  ));
}
