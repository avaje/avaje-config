package io.avaje.config;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The event that occurs on configuration changes. Register to listen for these events
 * via {@link Configuration#onChange(Consumer, String...)}.
 *
 * @see Configuration#eventBuilder(String)
 * @see Configuration#onChange(Consumer, String...)
 */
public interface ModificationEvent {

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

  /**
   * Build and publish modifications to the configuration.
   * <pre>{@code
   *
   *   configuration.eventBuilder("MyChanges")
   *     .put("someKey", "val0")
   *     .put("someOther.key", "42")
   *     .remove("foo")
   *     .publish();
   *
   * }</pre>
   *
   * @see Configuration#eventBuilder(String)
   * @see Configuration#onChange(Consumer, String...)
   */
  interface Builder {

    /**
     * Set a property value.
     *
     * @param key   The property key
     * @param value The new value of the property
     */
    Builder put(String key, String value);

    /**
     * Set all the properties from the map.
     */
    Builder putAll(Map<String, ?> map);

    /**
     * Remove a property from the configuration.
     */
    Builder remove(String key);

    /**
     * Publish the changes. Listeners registered via {@link Configuration#onChange(Consumer, String...)}
     * will be notified on the changes.
     */
    void publish();

  }
}
