package com.bestudios.fulcrum.api.util;

import org.bukkit.potion.PotionEffectType;

import java.util.AbstractMap;
import java.util.Map;

/**
 * EffectsLibrary provides a centralized repository for accessing Minecraft potion effects.
 * It maps string identifiers to PotionEffectType objects for easy conversion and lookup.
 * This utility class is useful for parsing configuration files, commands, or other string inputs
 * that need to be translated into actual potion effect types.
 *
 * @author Bestialus
 * @version 1.0
 * @since 1.0
 */
public class EffectsLibrary {

  /**
   * Complete list of string identifiers for all Minecraft potion effects.
   * These strings use the standard Minecraft naming convention with underscores.
   */
  private static final String[] minecraftEffects = new String[] {
          "absorption", "bad_omen", "blindness", "conduit_power", "darkness",
          "dolphins_grace", "fire_resistance", "glowing", "haste", "health_boost",
          "hero_of_the_village", "hunger", "instant_damage", "instant_health", "invisibility",
          "jump_boost", "levitation", "luck", "mining_fatigue", "nausea", "night_vision",
          "poison", "regeneration", "resistance", "saturation", "slow_falling", "slowness",
          "speed", "strength", "unluck", "water_breathing", "weakness", "wither"
  };

  /**
   * Returns an array of string identifiers for all implemented potion effects.
   *
   * @return String array containing all available potion effect identifiers
   */
  public static String[] implementedEffects() { return minecraftEffects; }

  /**
   * Mapping between string identifiers and their corresponding PotionEffectType objects.
   * This map allows for fast lookups when converting from string identifiers to actual effect types.
   */
  private static final Map<String, PotionEffectType> potionEffectTypeMap = Map.ofEntries(
          new AbstractMap.SimpleEntry<>("absorption", PotionEffectType.ABSORPTION),
          new AbstractMap.SimpleEntry<>("bad_omen", PotionEffectType.BAD_OMEN),
          new AbstractMap.SimpleEntry<>("blindness", PotionEffectType.BLINDNESS),
          new AbstractMap.SimpleEntry<>("conduit_power", PotionEffectType.CONDUIT_POWER),
          new AbstractMap.SimpleEntry<>("darkness", PotionEffectType.DARKNESS),
          new AbstractMap.SimpleEntry<>("dolphins_grace", PotionEffectType.DOLPHINS_GRACE),
          new AbstractMap.SimpleEntry<>("fire_resistance", PotionEffectType.FIRE_RESISTANCE),
          new AbstractMap.SimpleEntry<>("glowing", PotionEffectType.GLOWING),
          new AbstractMap.SimpleEntry<>("haste", PotionEffectType.HASTE),
          new AbstractMap.SimpleEntry<>("health_boost", PotionEffectType.HEALTH_BOOST),
          new AbstractMap.SimpleEntry<>("hero_of_the_village", PotionEffectType.HERO_OF_THE_VILLAGE),
          new AbstractMap.SimpleEntry<>("hunger", PotionEffectType.HUNGER),
          new AbstractMap.SimpleEntry<>("instant_damage", PotionEffectType.INSTANT_DAMAGE),
          new AbstractMap.SimpleEntry<>("instant_health", PotionEffectType.INSTANT_HEALTH),
          new AbstractMap.SimpleEntry<>("invisibility", PotionEffectType.INVISIBILITY),
          new AbstractMap.SimpleEntry<>("jump_boost", PotionEffectType.JUMP_BOOST),
          new AbstractMap.SimpleEntry<>("levitation", PotionEffectType.LEVITATION),
          new AbstractMap.SimpleEntry<>("luck", PotionEffectType.LUCK),
          new AbstractMap.SimpleEntry<>("mining_fatigue", PotionEffectType.MINING_FATIGUE),
          new AbstractMap.SimpleEntry<>("nausea", PotionEffectType.NAUSEA),
          new AbstractMap.SimpleEntry<>("night_vision", PotionEffectType.NIGHT_VISION),
          new AbstractMap.SimpleEntry<>("poison", PotionEffectType.POISON),
          new AbstractMap.SimpleEntry<>("regeneration", PotionEffectType.REGENERATION),
          new AbstractMap.SimpleEntry<>("resistance", PotionEffectType.RESISTANCE),
          new AbstractMap.SimpleEntry<>("saturation", PotionEffectType.SATURATION),
          new AbstractMap.SimpleEntry<>("slow_falling", PotionEffectType.SLOW_FALLING),
          new AbstractMap.SimpleEntry<>("slowness", PotionEffectType.SLOWNESS),
          new AbstractMap.SimpleEntry<>("speed", PotionEffectType.SPEED),
          new AbstractMap.SimpleEntry<>("strength", PotionEffectType.STRENGTH),
          new AbstractMap.SimpleEntry<>("unluck", PotionEffectType.UNLUCK),
          new AbstractMap.SimpleEntry<>("water_breathing", PotionEffectType.WATER_BREATHING),
          new AbstractMap.SimpleEntry<>("weakness", PotionEffectType.WEAKNESS),
          new AbstractMap.SimpleEntry<>("wither", PotionEffectType.WITHER)
  );

  /**
   * Retrieves a PotionEffectType object by its string identifier.
   *
   * @param name The string identifier of the potion effect (e.g., "speed", "fire_resistance")
   * @return The corresponding PotionEffectType, or null if no match is found
   */
  public static PotionEffectType getEffectByName(String name) { return potionEffectTypeMap.get(name); }
}
