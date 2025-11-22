package com.bestudios.fulcrum.api.service.claim;

import com.bestudios.fulcrum.api.service.Service;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Service interface for managing land claims within the game.
 * Implementations should provide methods to check claim status and permissions.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Service
 */
public interface ClaimsService extends Service {

  /**
   * Indicates whether the claim system supports levels or tiers of claims/domains.
   *
   * @return true if levels are supported, false otherwise
   */
  boolean supportsLevels();

  /**
   * Checks if any player or team has claimed a specific location.
   *
   * @param location The location to check
   * @return true if the location is claimed, false if it is a wilderness
   */
  boolean isLocationClaimed(Location location);

  /**
   * Checks if a specific location is considered wilderness (unclaimed).
   *
   * @param location The location to check
   * @return true if the location is a wilderness, false if it is claimed
   */
  default boolean isLocationWilderness(Location location) {
    return !isLocationClaimed(location);
  }

  /**
   * Retrieves the claim level or tier for a given home block location.
   *
   * @param homeBlock The home block location of the claim
   * @return An integer representing the claim level
   */
  int getDomainLevel(@NotNull final Location homeBlock);

  /**
   * Determines if a player has permission to build at a specific location.
   *
   * @param location The location to check
   * @param playerID The unique identifier of the player
   * @return true if the player can build at the location, false otherwise
   */
  boolean canBuildAt(Location location, UUID playerID);

  /**
   * Determines if a player has permission to break blocks at a specific location.
   *
   * @param location The location to check
   * @param playerID The unique identifier of the player
   * @return true if the player can break blocks at the location, false otherwise
   */
  boolean canBreakAt(Location location, UUID playerID);

  /**
   * Determines if PvP combat is allowed between two players at a specific location.
   *
   * @param location   The location to check
   * @param attackerID The unique identifier of the attacking player
   * @param targetID   The unique identifier of the target player
   * @return true if PvP is allowed at the location, false otherwise
   */
  boolean canPVPAt(Location location, UUID attackerID, UUID targetID);
}
