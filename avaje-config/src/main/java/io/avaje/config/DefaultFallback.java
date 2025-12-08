package io.avaje.config;

import org.jspecify.annotations.Nullable;

/**
 * Provides a default means of maybe obtaining a fallback value for a configuration key in the
 * case that there is no value explicitly configured.
 * <p>
 * Uses System Properties and Environment variables.
 */
public final class DefaultFallback implements ConfigurationFallback {

  static String oldEnvKey(String key) {
    //TODO: Consider replacing this with toEnvKey()
    return key.replace('.', '_').toUpperCase();
  }

  /**
   * Return the key as an environment variable name using the standard conventions.
   * <p>
   * Period char replaced by underscore and Hyphen char removed.
   */
  public static String toEnvKey(String key) {
    return key.replace('.', '_')
      .replace("-", "")
      .toUpperCase();
  }

  /**
   * Return an Entry overriding first by environment variable and then by system property.
   * <p>
   * If the key is not overridden then it is returned as the given value and source.
   *
   * @param key    The key of the entry
   * @param value  The value of entry that could be overridden
   * @param source The source of the entry that can be overridden
   * @return The entry to be added (that might have been its value and source overridden).
   */
  public static Configuration.Entry toEnvOverrideValue(String key, String value, String source) {
    String envValue = System.getenv(toEnvKey(key));
    if (envValue != null) {
      // overridden by an environment variable
      return Configuration.Entry.of(envValue, Constants.ENV_VARIABLES);
    }
    String propertyValue = System.getProperty(key);
    if (propertyValue != null) {
      // overridden by a system property
      return Configuration.Entry.of(propertyValue, Constants.SYSTEM_PROPS);
    }
    // not overridden, return as given
    return Configuration.Entry.of(value, source);
  }

  @Override
  public @Nullable String fallbackValue(String key) {
    final String val = System.getProperty(key, System.getenv(key));
    return val != null ? val : System.getenv(oldEnvKey(key));
  }
}
