package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.service.claim.Position;
import com.bestudios.fulcrum.api.service.claim.Region;
import com.bestudios.fulcrum.api.service.claim.RegionService;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default thread-safe implementation of the RegionService.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 */
public class FulcrumRegionService implements RegionService {

  private final FulcrumPlugin plugin;
  /**
   * The registry.
   * Key: Region ID
   * Value: The Region instance
   */
  private final Map<String, Region<?>> registry = new ConcurrentHashMap<>();

  public FulcrumRegionService(FulcrumPlugin plugin) {
    this.plugin = plugin;
  }

  // --- Service Contract ---

  @Override
  public ServicePriority getPriority() {
    return ServicePriority.Normal;
  }

  @Override
  public String getPluginName() {
    return "Fulcrum";
  }

  @Override
  public boolean isAvailable() {
    return plugin.isEnabled();
  }

  @Override
  public String getPluginVersion() {
    return plugin.getDescription().getVersion();
  }

  // --- Region Logic ---

  @Override
  public void register(@NotNull Region<?> region) {
    this.registry.put(region.getID(), region);
  }

  @Override
  public void unregister(@NotNull String regionId) {
    this.registry.remove(regionId);
  }

  @Override
  public Optional<Region<?>> getRegion(@NotNull String regionId) {
    return Optional.ofNullable(this.registry.get(regionId));
  }

  @Override
  public @NotNull Collection<Region<?>> getRegions() {
    return Collections.unmodifiableCollection(this.registry.values());
  }

  @Override
  public @NotNull Collection<Region<?>> getRegionsAt(@NotNull Position position) {
    // Spatial Query: Filter all regions where .contains() returns true
    return this.registry.values()
                        .stream()
                        .filter(region -> region.contains(position))
                        .toList();
  }
}