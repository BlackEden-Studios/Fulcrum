package com.bestudios.fulcrum.database;

import com.bestudios.fulcrum.api.basic.FulcrumPlugin;
import com.bestudios.fulcrum.api.database.DatabaseGateway;
import com.bestudios.fulcrum.api.database.DatabaseQuery;

/**
 * Represents a Redis database query.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see DatabaseQuery
 * @see DatabaseGateway
 */
public class RedisQuery implements DatabaseQuery {

  /** The constructed Redis query string. */
  private final String query;

  /**
   * Constructs a RedisQuery using the provided service and path.
   *
   * @param service The FulcrumPlugin instance.
   * @param path   The specific path for the query.
   */
  public RedisQuery(String service, String path) {
    query = service + ":" + path;
  }
  @Override
  public String value() {
    return query;
  }
}
