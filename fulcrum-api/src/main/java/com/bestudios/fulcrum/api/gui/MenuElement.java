package com.bestudios.fulcrum.api.gui;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A generic, thread-safe blueprint for any GUI element, used to populate {@link MenuData} instances.
 * <br>
 * This class is immutable and thread-safe.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Menu
 * @see MenuData
 */
public record MenuElement (
        int slot,
        @NotNull Material material,
        @Nullable Component displayName,
        @Nullable ItemLore lore,
        @NotNull Map<DataComponentType, Object> dataComponents,
        @Nullable Consumer<Player> action // Added action field
) {

  public MenuElement {
    dataComponents = Map.copyOf(dataComponents);
  }

  /**
   * Converts this blueprint into an ItemStack.
   * @return The resulting ItemStack.
   */
  public @NotNull ItemStack toItemStack() {
    ItemStack item = new ItemStack(material);

    if (displayName != null) {
      item.setData(DataComponentTypes.CUSTOM_NAME, displayName);
    }
    if (lore != null) {
      item.setData(DataComponentTypes.LORE, lore);
    }

    for (Map.Entry<DataComponentType, Object> entry : dataComponents.entrySet()) {
      applyData(item, entry.getKey(), entry.getValue());
    }

    return item;
  }

  /**
   * Static factory method to create a new MenuElement.
   * @param slot   Slot to place the item in.
   * @param item   Item to place.
   * @param action Action to perform when the item is clicked.
   * @return A new MenuElement.
   */
  public static @NotNull MenuElement of(int slot, ItemStack item, Consumer<Player> action) {
    return new MenuElement(
          slot,
          item.getType(),
          item.getItemMeta().hasCustomName() ? item.getItemMeta().customName() : Component.text(item.getType().name()),
          item.getItemMeta().hasLore() ? ItemLore.lore(item.getItemMeta().lore()) : ItemLore.lore().build(),
          new ConcurrentHashMap<>(),
          action
    );
  }

  ///////////////////////////////////////////////////////////////////////////

  private void applyData(ItemStack item, DataComponentType type, Object value) {
    if (type instanceof DataComponentType.Valued<?> valuedType) {
      applyValued(item, valuedType, value);
    } else if (type instanceof DataComponentType.NonValued nonValuedType){
      item.setData(nonValuedType);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void applyValued(ItemStack item, DataComponentType.Valued<T> type, Object value) {
    try {
      item.setData(type, (T) value);
    } catch (ClassCastException e) {
      // Log error
    }
  }
}