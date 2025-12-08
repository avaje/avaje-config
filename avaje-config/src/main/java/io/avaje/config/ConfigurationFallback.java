package io.avaje.config;

import io.avaje.config.Configuration.Entry;

import java.util.Optional;

/**
 * Implementations of this class are able to define a fallback value when there is no
 * value available for a configuration key.
 * <p>
 * The default implementation uses System Properties and Environment variables.
 */
public interface ConfigurationFallback extends ConfigExtension {

  /**
   * Provides the ability to override the value that is going to be set.
   * <p>
   * By default, this just returns the value that is passed in and does not
   * override the value.
   *
   * @param key    The property key
   * @param value  The value that can be overridden by the returned value
   * @param source The source of the key value pair
   */
  default Entry overrideValue(String key, String value, String source) {
    return Entry.of(value, source);
  }

  /**
   * Return a value for the supplied key or {@code null} if there is no value.
   *
   * @param key The configuration key to get a fallback value for.
   */
  default Optional<Entry> fallbackValue(String key) {
    return Optional.empty();
  }

}
