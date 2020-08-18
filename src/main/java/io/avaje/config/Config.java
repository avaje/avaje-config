package io.avaje.config;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Provides application Configuration based on loading properties and yaml files
 * as well as plugins that supply properties (like dynamic configuration loaded from a db).
 * <p>
 * The application can register onChange listeners to handle changes to configuration
 * properties at runtime. Plugins or code can dynamically load and change properties and
 * this can fire any registered callback handlers.
 * </p>
 */
public class Config {

  private static final Configuration data = CoreConfiguration.load();

  /**
   * Hide constructor.
   */
  private Config() {
  }

  /**
   * Return the loaded properties as standard Properties map.
   */
  public static Properties asProperties() {
    return data.asProperties();
  }

  /**
   * Return the underlying configuration.
   */
  public static Configuration asConfiguration() {
    return data;
  }

  /**
   * Put all loaded properties into System properties.
   */
  public static void loadIntoSystemProperties() {
    data.loadIntoSystemProperties();
  }

  /**
   * Return a required configuration value as String.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  public static String get(String key) {
    return data.get(key);
  }

  /**
   * Return a configuration string value with a given default.
   *
   * @param key          The configuration key
   * @param defaultValue The default value used
   * @return The configured or default value
   */
  public static String get(String key, String defaultValue) {
    return data.get(key, defaultValue);
  }

  /**
   * Return a configuration value that might not exist.
   *
   * @param key The configuration key
   * @return The configured value wrapped as optional
   */
  public static Optional<String> getOptional(String key) {
    return data.getOptional(key);
  }

  /**
   * Return a required boolean configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  public static boolean getBool(String key) {
    return data.getBool(key);
  }

  /**
   * Return a configuration value as boolean given a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value used
   * @return The configured or default value
   */
  public static boolean getBool(String key, boolean defaultValue) {
    return data.getBool(key, defaultValue);
  }

  /**
   * Return a required int configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  public static int getInt(String key) {
    return data.getInt(key);
  }

  /**
   * Return a configuration value as int given a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value used
   * @return The configured or default value
   */
  public static int getInt(String key, int defaultValue) {
    return data.getInt(key, defaultValue);
  }

  /**
   * Return a required long configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  public static long getLong(String key) {
    return data.getLong(key);
  }

  /**
   * Return a configuration value as long given a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value used
   * @return The configured or default value
   */
  public static long getLong(String key, long defaultValue) {
    return data.getLong(key, defaultValue);
  }

  /**
   * Set a configuration value.
   * <p>
   * This will fire an configuration callback listeners that are registered
   * for this key.
   * </p>
   */
  public static void setProperty(String key, String value) {
    data.setProperty(key, value);
  }

  /**
   * Register a callback for a change to the given configuration key.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  public static void onChange(String key, Consumer<String> callback) {
    data.onChange(key, callback);
  }

  /**
   * Register a callback for a change to the given configuration key as an Int value.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  public static void onChangeInt(String key, Consumer<Integer> callback) {
    data.onChangeInt(key, callback);
  }

  /**
   * Register a callback for a change to the given configuration key as an Long value.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  public static void onChangeLong(String key, Consumer<Long> callback) {
    data.onChangeLong(key, callback);
  }

  /**
   * Register a callback for a change to the given configuration key as an Boolean value.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  public static void onChangeBool(String key, Consumer<Boolean> callback) {
    data.onChangeBool(key, callback);
  }
}
