package com.bestudios.fulcrum.api.service.database;

/**
 * Functional interface representing a database query.
 * Provides a method to retrieve the query string.
 *
 * @author Bestialus
 * @version 1.0
 * @since   1.0
 *
 * @see DatabaseService
 */
@FunctionalInterface
public interface DatabaseQuery {

  /**
   * Retrieves the database query string.
   *
   * @return The database query string
   */
  String value();
}
