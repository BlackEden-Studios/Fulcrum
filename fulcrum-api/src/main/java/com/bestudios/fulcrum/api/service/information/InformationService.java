package com.bestudios.fulcrum.api.service.information;

import com.bestudios.fulcrum.api.service.Service;
import org.jetbrains.annotations.NotNull;

/**
 * Service interface for retrieving information about the server.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see Service
 */
public interface InformationService extends Service {

  /**
   * Gets the name of the network this server is a part of.
   * @return the network name
   */
  @NotNull
  String getNetworkName();

  /**
   * Gets the name of the server.
   * @return the server name
   */
  @NotNull
  String getServerName();
}
