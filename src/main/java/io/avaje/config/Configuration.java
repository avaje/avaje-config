package io.avaje.config;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Configuration API for accessing property values and registering onChange listeners.
 */
public interface Configuration {

  /**
   * Return the loaded properties as standard Properties map.
   */
  Properties asProperties();

  /**
   * Return a required configuration value as String.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  String get(String key);

  /**
   * Return a configuration string value with a given default.
   *
   * @param key          The configuration key
   * @param defaultValue The default value used
   * @return The configured or default value
   */
  String get(String key, String defaultValue);

  /**
   * Return a configuration value that might not exist.
   *
   * @param key The configuration key
   * @return The configured value wrapped as optional
   */
  Optional<String> getOptional(String key);

  /**
   * Return a required boolean configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  boolean getBool(String key);

  /**
   * Return a configuration value as boolean given a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value used
   * @return The configured or default value
   */
  boolean getBool(String key, boolean defaultValue);

  /**
   * Return a required int configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  int getInt(String key);

  /**
   * Return a configuration value as int given a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value used
   * @return The configured or default value
   */
  int getInt(String key, int defaultValue);

  /**
   * Return a required long configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  long getLong(String key);

  /**
   * Return a configuration value as long given a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value used
   * @return The configured or default value
   */
  long getLong(String key, long defaultValue);

  /**
   * Set a configuration value.
   * <p>
   * This will fire an configuration callback listeners that are registered
   * for this key.
   * </p>
   */
  void setProperty(String key, String value);

  /**
   * Register a callback for a change to the given configuration key.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  void onChange(String key, Consumer<String> callback);

  /**
   * Register a callback for a change to the given configuration key as an Int value.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  void onChangeInt(String key, Consumer<Integer> callback);

  /**
   * Register a callback for a change to the given configuration key as an Long value.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  void onChangeLong(String key, Consumer<Long> callback);

  /**
   * Register a callback for a change to the given configuration key as an Boolean value.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  void onChangeBool(String key, Consumer<Boolean> callback);

  /**
   * Put the loaded properties into System properties.
   */
  void loadIntoSystemProperties();

  /**
   * Return a copy of the properties with 'eval' run on all the values.
   */
  Properties eval(Properties properties);

  /**
   * Expression evaluation.
   */
  interface ExpressionEval {

    /**
     * Evaluate a configuration expression.
     */
    String eval(String expression);
  }
}
