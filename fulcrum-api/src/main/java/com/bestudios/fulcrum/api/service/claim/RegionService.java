package com.bestudios.fulcrum.api.service.claim;

import com.bestudios.fulcrum.api.region.Position;
import com.bestudios.fulcrum.api.region.Region;
import com.bestudios.fulcrum.api.service.Service;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * The central registry service for managing custom Regions.
 * <p>
 * Plugins can register their specialized regions here, allowing other plugins
 * to query them spatially or by ID without knowing the implementation details.
 * </p>
 */
public interface RegionService extends Service {

  /**
   * Registers a new region into the global registry.
   * If a region with the same ID exists, it will be overwritten.
   *
   * @param region The region to register.
   */
  void register(@NotNull Region<?> region);

  /**
   * Unregisters a region by its ID.
   *
   * @param regionId The ID of the region to remove.
   */
  void unregister(@NotNull String regionId);

  /**
   * Retrieves a region by its unique ID.
   *
   * @param regionId The ID to look for.
   * @return An Optional containing the region if found.
   */
  Optional<Region<?>> getRegion(@NotNull String regionId);

  /**
   * Retrieves all registered regions.
   *
   * @return An unmodifiable collection of all regions.
   */
  @NotNull Collection<Region<?>> getRegions();

  /**
   * Finds all regions that contain the specified position.
   * <p>
   * This iterates through all registered regions and calls {@link Region#contains(Position)}.
   * </p>
   *
   * @param position The position to check.
   * @return A collection of regions containing the position.
   */
  @NotNull Collection<Region<?>> getRegionsAt(@NotNull Position position);

}