package io.avaje.config;

import org.jspecify.annotations.Nullable;

/**
 * Implementations of this class are able to define a fallback value when there is no
 * value available for a configuration key.
 * <p>
 * The default implementation uses System Properties and Environment variables.
 */
public interface ConfigurationFallback extends ConfigExtension {

  /**
   * Return a value for the supplied key or {@code null} if there is no value.
   * @param key The configuration key to get a fallback value for.
   */
  @Nullable
  String get(String key);

}
