package io.avaje.config;

import org.jspecify.annotations.Nullable;

/**
 * Provides a default means of maybe obtaining a fallback value for a configuration key in the
 * case that there is no value explicitly configured.
 * <p>
 * Uses System Properties and Environment variables.
 */
public final class DefaultFallback implements ConfigurationFallback {

  @Override
  public @Nullable String fallbackValue(String key) {
    final String val = System.getProperty(key, System.getenv(key));
    return val != null ? val : System.getenv(toEnvKey(key));
  }

  static String toEnvKey(String key) {
    return key.replace('.', '_').toUpperCase();
  }
}
