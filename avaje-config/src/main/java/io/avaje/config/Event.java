package io.avaje.config;

import java.util.Set;
import java.util.function.Consumer;

/**
 * The event that occurs on configuration changes. Register to listen for these events
 * via {@link Configuration#onChange(Consumer, String...)}.
 *
 * @see Configuration#eventBuilder(String)
 * @see Configuration#onChange(Consumer, String...)
 */
public interface Event {

  /**
   * Return the name of the event (e.g "reload").
   */
  String name();

  /**
   * Return the updated configuration.
   */
  Configuration configuration();

  /**
   * Return the set of keys where the properties where modified.
   */
  Set<String> modifiedKeys();
}
