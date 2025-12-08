package io.avaje.config;

import io.avaje.config.Configuration.Entry;

import java.util.Optional;

/**
 * Override and fallback values.
 */
final class DefaultValues {

  /**
   * Return the key as an environment variable name using the standard conventions.
   */
  static String toEnvKey(String key) {
    return key.replace('.', '_').replace("-", "").toUpperCase();
  }

  /**
   * Return an Entry overriding first by system property and then by environment variable.
   * <p>
   * If the key is not overridden then it is returned as the given value and source.
   */
  static Entry overrideValue(String key, String value, String source) {
    String propertyValue = System.getProperty(key);
    if (propertyValue != null) {
      // overridden by a system property
      return Entry.of(propertyValue, Constants.SYSTEM_PROPS);
    }
    String envValue = System.getenv(toEnvKey(key));
    if (envValue != null) {
      // overridden by an environment variable
      return Entry.of(envValue, Constants.ENV_VARIABLES);
    }
    // not overridden, return as given
    return Entry.of(value, source);
  }

  static Optional<Entry> fallbackValue(String key) {
    String propertyValue = System.getProperty(key);
    if (propertyValue != null) {
      // overridden by a system property
      return Optional.of(Entry.of(propertyValue, Constants.SYSTEM_PROPS));
    }
    String envValue = System.getenv(toEnvKey(key));
    if (envValue != null) {
      // overridden by an environment variable
      return Optional.of(Entry.of(envValue, Constants.ENV_VARIABLES));
    }
    return Optional.empty();
  }
}
