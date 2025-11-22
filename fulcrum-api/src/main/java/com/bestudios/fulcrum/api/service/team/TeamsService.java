package com.bestudios.fulcrum.api.service.team;

import com.bestudios.fulcrum.api.service.Service;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Service interface for managing teams within the game.
 * Implementations should provide methods to handle team membership and relationships.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Service
 */
public interface TeamsService extends Service {

  /**
   * Retrieves the name of a team given its unique identifier.
   *
   * @param teamID The unique identifier of the team
   * @return The name of the team, or null if not found
   */
  @Nullable String getTeamName(@NotNull final UUID teamID);

  /**
   * Retrieves the unique identifier of a team given its name.
   *
   * @param teamName The name of the team
   * @return The unique identifier of the team, or null if not found
   */
  @Nullable UUID getUniqueId(@NotNull final String teamName);

  /**
   * Adds a player to a team.
   *
   * @param teamID   The unique identifier of the team
   * @param playerID The unique identifier of the player
   * @return true if the player was successfully added, false otherwise
   */
  boolean addPlayer(@NotNull final UUID teamID, @NotNull final UUID playerID);

  /**
   * Removes a player from a team.
   *
   * @param teamID   The unique identifier of the team
   * @param playerID The unique identifier of the player
   * @return true if the player was successfully removed, false otherwise
   */
  boolean removePlayer(@NotNull final UUID teamID, @NotNull final UUID playerID);

  /**
   * Checks if a player is a member of a team.
   *
   * @param teamID   The unique identifier of the team
   * @param playerID The unique identifier of the player
   * @return true if the player is a member of the team, false otherwise
   */
  boolean hasPlayer(@NotNull final UUID teamID, @NotNull final UUID playerID);

  /**
   * Determines if two players are allies (i.e., belong to the same team).
   *
   * @param playerOne The unique identifier of the first player
   * @param playerTwo The unique identifier of the second player
   * @return true if both players are allies, false otherwise
   */
  boolean areAllies(@NotNull final UUID playerOne, @NotNull final UUID playerTwo);

  /**
   * Determines if two players are enemies (i.e., do not belong to the same team).
   *
   * @param playerOne The unique identifier of the first player
   * @param playerTwo The unique identifier of the second player
   * @return true if both players are enemies, false otherwise
   */
  default boolean areEnemies(@NotNull final UUID playerOne, @NotNull final UUID playerTwo) {
    return !areAllies(playerOne, playerTwo);
  }
}
