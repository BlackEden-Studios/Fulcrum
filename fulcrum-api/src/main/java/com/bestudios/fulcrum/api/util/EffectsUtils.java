package com.bestudios.fulcrum.api.util;

import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * EffectsUtils provides a centralized repository for accessing Minecraft potion effects.
 * <p>
 * Refactored to dynamically load effects from the Server Registry, ensuring
 * support for future Minecraft versions and custom data pack effects automatically.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 */
public final class EffectsUtils {

  /**
   * Dynamic mapping of "effect_name" -> PotionEffectType. <p>
   * Generated automatically at runtime using the server's Registry. <p>
   * This is immutable and thread-safe.
   */
  private static final Map<String, PotionEffectType> POTION_MAP =
          StreamSupport.stream(Registry.POTION_EFFECT_TYPE.spliterator(), false)
                       .collect(Collectors.toUnmodifiableMap(
                          // Key: The path part of the NamespacedKey (e.g., "minecraft:speed" -> "speed")
                          type -> type.getKey().getKey(),
                          // Value: The PotionEffectType object
                          Function.identity()
                       ));

  /**
   * Cached array of identifiers to maintain backward compatibility with
   * implementedEffects().
   */
  private static final String[] EFFECT_NAMES = POTION_MAP.keySet().toArray(String[]::new);

  /**
   * Returns an array of string identifiers for all registered potion effects.
   *
   * @return String array containing all available potion effect identifiers
   */
  public static String[] implementedEffects() { return EFFECT_NAMES; }

  /**
   * Retrieves a PotionEffectType object by its string identifier.
   * Handles case-insensitivity.
   *
   * @param name The string identifier of the potion effect (e.g., "speed", "fire_resistance")
   * @return The corresponding PotionEffectType, or null if no match is found
   */
  @Nullable
  public static PotionEffectType getEffectByName(String name) {
    if (name == null) return null;
    return POTION_MAP.get(name.toLowerCase(Locale.US));
  }

  // Private constructor to prevent instantiation of utility class
  private EffectsUtils() {}
}