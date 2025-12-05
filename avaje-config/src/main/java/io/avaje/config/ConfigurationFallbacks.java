package io.avaje.config;

import org.jspecify.annotations.Nullable;

/**
 * Implementations of this class are able to define which fallbacks are used when there is no
 * value available for a configuration key.
 */

public interface ConfigurationFallbacks extends ConfigExtension {

  /**
   * @param key The configuration key to get a fallback value for.
   * @return a value for the supplied key or {@code null} if there is no value.
   */

  @Nullable
  String get(String key);

}
