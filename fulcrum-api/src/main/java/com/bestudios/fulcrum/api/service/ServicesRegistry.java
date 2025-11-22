package com.bestudios.fulcrum.api.service;

import java.util.Optional;

/**
 * Registry for plugin integration through services.
 * <p>
 * This interface defines methods for registering and retrieving integration services.
 * Implementations should provide thread-safe access to the services.
 * </p>
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 * @see Service
 */
public interface ServicesRegistry {

  /**
   * Registers a service
   *
   * @param serviceClass the class of the service to register
   * @param service the service implementation to register
   * @param <T> the type of the service
   * @return true if the service was registered successfully, false otherwise
   */
  <T extends Service> boolean registerService(Class<T> serviceClass, T service);

  /**
   * Retrieves a registered service.
   *
   * @param service the class of the service to retrieve
   * @param <T> the type of the service
   * @return an {@link Optional} containing the service if found, or empty if not registered
   */
  <T extends Service> Optional<T> getService(Class<T> service);

  /**
   * Checks if a service is registered.
   *
   * @param service the class of the service to check
   * @return true if the service is registered, false otherwise
   */
  default boolean isRegistered(Class<? extends Service> service) {
    return getService(service).isPresent();
  }

  /**
   * Registers all services provided by the implementing class.
   */
  void registerServices();
}
