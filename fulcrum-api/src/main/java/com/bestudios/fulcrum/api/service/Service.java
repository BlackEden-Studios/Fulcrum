package com.bestudios.fulcrum.api.service;

import org.bukkit.plugin.ServicePriority;

/**
 * Base interface for all plugin integration facades.
 * Provides common lifecycle and identification methods for plugin integration services.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see ServicesRegistry
 * @see ServicePriority
 */
public interface Service {

  /**
   * Gets the priority of this service.
   * @return the service priority
   */
  ServicePriority getPriority();
  /**
   * Gets the name of the plugin this facade integrates with.
   * @return the plugin name (e.g., "Vault", "LuckPerms")
   */
  String getPluginName();

  /**
   * Checks if the underlying plugin is currently enabled and available.
   * @return true if the plugin is ready for use
   */
  boolean isAvailable();

  /**
   * Gets the version of the integrated plugin.
   * @return the plugin version string
   */
  default String getPluginVersion() {
    return "unknown";
  }
}

