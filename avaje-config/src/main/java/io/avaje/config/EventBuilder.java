package io.avaje.config;

import java.util.function.Consumer;

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
public interface EventBuilder {

  /**
   * Set a property value.
   *
   * @param key The property key
   * @param value The new value of the property
   */
  EventBuilder put(String key, String value);

  /**
   * Remove a property from the configuration.
   */
  EventBuilder remove(String key);

  /**
   * Publish the changes. Listeners registered via {@link Configuration#onChange(Consumer, String...)}
   * will be notified on the changes.
   */
  void publish();

}
