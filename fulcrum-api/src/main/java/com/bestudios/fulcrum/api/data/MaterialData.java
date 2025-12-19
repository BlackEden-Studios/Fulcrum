package com.bestudios.fulcrum.api.data;

import org.bukkit.Material;

import java.util.List;

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
        List<String> sources,
        int bundleAmount,
        double bundleValue
) { }
