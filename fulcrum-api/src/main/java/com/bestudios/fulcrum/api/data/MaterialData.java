package com.bestudios.fulcrum.api.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds data for a specific valid material.
 *
 * @param namespaceID   The namespaced key string (e.g., "minecraft:wheat")
 * @param displayName   The display name of the material
 * @param type          The Bukkit Material type (Use AIR for custom materials)
 * @param tier          The tier this material belongs to (0 for no tier)
 * @param sources       List of namespace IDS of source blocks/items
 * @param bundleAmount  Amount required for a bundle of this material (1 for no bundle)
 * @param bundleValue   Value of this material's bundle (single item value would be bundleValue / bundleAmount)
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 */
public record MaterialData(
        String namespaceID,
        String displayName,
        Material type,
        int tier,
        Set<String> sources,
        int bundleAmount,
        double bundleValue
) {

  /**
   * Helper to parse a specific Material section.
   *
   * @param tier The tier of the material
   * @param displayName The display name of the material
   * @param config The configuration section to parse
   * @return A new MaterialData object parsed from the configuration
   */
  public static @NotNull MaterialData parseMaterialData(
          int tier,
          @NotNull String displayName,
          @NotNull ConfigurationSection config
  ) {
    // Safe Material parsing
    String typeStr = config.getString("type", "AIR");

    return new MaterialData(
      config.getString("namespace", ""),
      displayName.isEmpty() ? typeStr : displayName,
      Material.getMaterial(typeStr) != null ? Material.getMaterial(typeStr) : Material.AIR,
      tier,
      config.getStringList("sources").stream().collect(Collectors.toUnmodifiableSet()),
      config.getInt("bundle.amount", 1),
      config.getDouble("bundle.value", 0.0)
    );
  }
}
