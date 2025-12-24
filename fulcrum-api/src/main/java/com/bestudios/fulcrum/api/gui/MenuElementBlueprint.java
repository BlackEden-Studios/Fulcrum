package com.bestudios.fulcrum.api.gui;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A generic, thread-safe blueprint for any GUI element.
 * Supports both Valued components (e.g. Enchants) and Non-Valued flags (e.g. Unbreakable).
 */
public record MenuElementBlueprint(
        int slot,
        @NotNull Material material,
        @Nullable Component displayName,
        @Nullable ItemLore lore,
        @NotNull Map<DataComponentType, Object> dataComponents
) {

  public MenuElementBlueprint {
    dataComponents = Map.copyOf(dataComponents); // Ensure immutability
  }

  public @NotNull ItemStack toItemStack() {
    ItemStack item = new ItemStack(material);

    // 1. Apply Standard Visuals
    if (displayName != null) {
      item.setData(DataComponentTypes.CUSTOM_NAME, displayName);
    }
    if (lore != null) {
      item.setData(DataComponentTypes.LORE, lore);
    }

    // 2. Apply Generic Data Components
    for (Map.Entry<DataComponentType, Object> entry : dataComponents.entrySet()) {
      applyData(item, entry.getKey(), entry.getValue());
    }

    return item;
  }

  /**
   * Helper to distinguish between Valued and Non-Valued components.
   */
  private void applyData(ItemStack item, DataComponentType type, Object value) {
    // Check if the component expects a value (e.g., Integer, Boolean, List)
    if (type instanceof DataComponentType.Valued<?> valuedType) {
      applyValued(item, valuedType, value);
    } else if (type instanceof DataComponentType.NonValued nonValuedType){
      // It is a Non-Valued flag (e.g., Unbreakable), just setting it enables it
      item.setData(nonValuedType);
    }
  }

  /**
   * Captures the wildcard <T> so we can call setData safely.
   */
  @SuppressWarnings("unchecked")
  private <T> void applyValued(ItemStack item, DataComponentType.Valued<T> type, Object value) {
    try {
      // We cast 'value' to T. This assumes the blueprint creator put the correct value type in the map.
      item.setData(type, (T) value);
    } catch (ClassCastException e) {
      System.err.println("[VJobs] Type mismatch for component " + type.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }
}
