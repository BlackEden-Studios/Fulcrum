package com.bestudios.fulcrum.api.data;

import java.util.Map;

/**
 * Immutable Record representing a generated Quest for a player.
 * Holds the specific objectives and rewards determined by the RNG seed.
 *
 * @param provider      The Object instance this quest belongs to.
 * @param seed          The random seed used to generate this specific quest configuration.
 * @param materials     A map of the required Materials {@link MaterialData} and their respective bundle amounts.
 * @param moneyReward   The monetary reward (price) awarded upon completion.
 * @param expReward     The experience points awarded upon completion.

 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 */
public record TradingQuestData(
        Object provider,
        long seed,
        Map<MaterialData, Integer> materials,
        double moneyReward,
        int expReward
) {

  /**
   * Returns true if the material map contains a material with the specified Namespace ID
   * @param namespaceID The namespace ID of the material
   * @return true if it is a material required from the quest, false otherwise
   */
  public boolean requiresMaterial(String namespaceID) {
    for (Map.Entry<MaterialData, Integer> entry : materials.entrySet())
      if (entry.getKey().namespaceID().equals(namespaceID)) return true;
    return false;
  }

}
