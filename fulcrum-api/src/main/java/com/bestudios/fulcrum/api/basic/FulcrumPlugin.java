package com.bestudios.fulcrum.api.basic;

import com.bestudios.fulcrum.api.command.CommandsRegistry;
import com.bestudios.fulcrum.api.configuration.ConfigurationsRegistry;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Core API interface for Fulcrum-based plugins.
 * Defines the contract that all Fulcrum plugins must adhere to.
 */
public interface FulcrumPlugin {
  /**
   * Gets the CommandsRegistry instance for this plugin.
   *
   * @return The CommandsRegistry instance
   */
  CommandsRegistry<?> getCommandsRegistry();
  /**
   * Gets the ConfigurationsRegistry instance for this plugin.
   * @return The ConfigurationsRegistry instance
   */
  ConfigurationsRegistry getConfigurationsRegistry();
  /**
   * Gets the language configuration for this plugin.
   * @return The language configuration
   */
  FileConfiguration getLanguageConfiguration();
  /**
   * Sets the debug mode for this plugin.
   * @param debugMode true to enable debug mode, false to disable
   */
  void setDebugMode(boolean debugMode);
  /**
   * Toggles debug mode for this plugin.
   */
  void toggleDebugMode();
}
