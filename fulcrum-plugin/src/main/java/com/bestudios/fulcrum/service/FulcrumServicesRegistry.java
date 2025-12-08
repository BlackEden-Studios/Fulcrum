package com.bestudios.fulcrum.service;

import com.bestudios.fulcrum.Fulcrum;
import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.database.DatabaseGateway;
import com.bestudios.fulcrum.api.service.ServicesRegistry;
import com.bestudios.fulcrum.api.service.Service;
import com.bestudios.fulcrum.api.service.claim.ClaimsService;
import com.bestudios.fulcrum.api.service.customitem.CustomItemsService;
import com.bestudios.fulcrum.api.service.economy.EconomyService;
import com.bestudios.fulcrum.api.service.messaging.MessageService;
import com.bestudios.fulcrum.api.service.permission.PermissionsService;
import com.bestudios.fulcrum.api.service.team.TeamsService;
import com.bestudios.fulcrum.database.RedisDatabaseGateway;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.Optional;

/**
 * Default implementation of {@link ServicesRegistry} that acts as a registry pattern for plugin integration services.
 * Handles registration and retrieval of services for various plugins.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see ServicesRegistry
 * @see Service
 */
public final class FulcrumServicesRegistry implements ServicesRegistry {

  /** The plugin instance */
  private final FulcrumPlugin plugin;

  /**
   * Constructs a new FulcrumServicesRegistry with the given Fulcrum plugin instance.
   * @param pluginRef the Fulcrum plugin instance
   */
  public FulcrumServicesRegistry(Fulcrum pluginRef) {
    this.plugin = pluginRef;
  }

  @Override
  public <T extends Service> boolean registerService(Class<T> serviceClass, T service) {
    this.plugin.getServer().getServicesManager().register(serviceClass, service, this.plugin, service.getPriority());
    return true;
  }

  @Override
  public <T extends Service> Optional<T> getService(Class<T> service) {
    RegisteredServiceProvider<T> registration = this.plugin.getServer()
                                                           .getServicesManager()
                                                           .getRegistration(service);

    return Optional.ofNullable(registration).map(RegisteredServiceProvider::getProvider);
  }

  @Override
  public void registerServices() {
    // Economy
    this.registerService(EconomyService.class, new TheNewEconomyBridge(this.plugin, ServicePriority.Highest));
    // Permissions
    this.registerService(PermissionsService.class, new LuckPermsBridge(this.plugin, ServicePriority.Highest));
    // Claims and teams
    LandsBridge lands = new LandsBridge(this.plugin, ServicePriority.Highest);
    this.registerService(ClaimsService.class, lands);
    this.registerService(TeamsService.class, lands);
    // Custom items
    this.registerService(CustomItemsService.class, new ItemsAdderBridge(this.plugin, ServicePriority.Highest));
    // Database
    this.registerService(DatabaseGateway.class, plugin.getDatabaseGateway());
    // Messaging
    this.registerService(
            MessageService.class,
            new FulcrumMessageService(this.plugin, ServicePriority.Highest, plugin.getDatabaseGateway())
    );
  }

}

