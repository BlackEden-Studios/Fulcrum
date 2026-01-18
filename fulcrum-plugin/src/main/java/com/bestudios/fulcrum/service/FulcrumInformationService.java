package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.service.information.InformationService;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link InformationService} for Fulcrum.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see InformationService
 */
public class FulcrumInformationService implements InformationService {

  /** The plugin instance */
  private final FulcrumPlugin plugin;
  /** The service priority */
  private final ServicePriority priority;

  /**
   * Constructs a new FulcrumInformationService.
   *
   * @param plugin   The Fulcrum plugin instance
   * @param priority The service priority
   */
  public FulcrumInformationService(FulcrumPlugin plugin, ServicePriority priority) {
    this.plugin = plugin;
    this.priority = priority;
  }

  @Override
  public @NotNull String getNetworkName() {
    return plugin.getConfig().getString("network.name", "unknown");
  }

  @Override
  public @NotNull String getServerName() {
    return plugin.getConfig().getString("network.server", "unknown");
  }

  @Override
  public ServicePriority getPriority() {
    return this.priority;
  }

  @Override
  public String getPluginName() {
    return "Fulcrum";
  }

  @Override
  public boolean isAvailable() {
    return true;
  }
}
