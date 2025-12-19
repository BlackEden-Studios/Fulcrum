package com.bestudios.fulcrum.api.util;

import com.bestudios.fulcrum.api.gui.Menu;
import com.bestudios.fulcrum.api.service.customitem.CustomItemsService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class Commons {

  /**
   * Attempts to process a transaction for a player, checking if they have all required materials.
   * <br>
   * If it is true, removes them and returns success. If not, returns failure.
   *
   * @param player The player performing the transaction.
   * @param quest  The transaction quest to fulfill
   * @return true if the transaction was successful, false otherwise.
   */
  public static boolean inventoryTransaction(
          @NotNull Player player,
          @NotNull Map<String, Integer> quest,
          List<Predicate<ItemStack>> rules,
          @NotNull CustomItemsService service
  ) {
    Inventory inv = player.getInventory();
    Map<ItemStack, Integer> items = new ConcurrentHashMap<>();

    // 1. Validation Phase: Check if the player has all items
    for (int slot: Menu.playerInventorySlotList) {
      ItemStack item = inv.getItem(slot);
      // If the slot is empty, skip it
      if (item == null || item.getType().isAir()) continue;

      String ID = service.getItemNamespaceID(item);
      // If the item is not in the quest, skip it
      if (!quest.containsKey(ID)) continue;
      // If the item does not satisfy all rules, skip it
      if (!rules.stream().allMatch(rule -> rule.test(item))) continue;
      // Check if the player has enough items, else update the quest and try to find another stack
      if (quest.get(ID) - item.getAmount() < 0) {
        items.put(item, quest.get(ID));
        quest.remove(ID);
      } else {
        items.put(item, item.getAmount());
        quest.put(ID, quest.get(ID) - item.getAmount());
      }
    }
    // If the quest map is not empty, the player lacks some items, so the transaction failed
    if (!quest.isEmpty()) return false;

    // 2. Transaction Phase: Remove items
    for (Map.Entry<ItemStack, Integer> entry : items.entrySet())
      entry.getKey().setAmount(entry.getValue());

    return true;
  }
}
