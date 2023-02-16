package io.avaje.config;

import io.avaje.lang.NonNullApi;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * Provides application Configuration based on loading properties and yaml files
 * as well as plugins that supply properties (like dynamic configuration loaded from a db).
 * <p>
 * The application can register onChange listeners to handle changes to configuration
 * properties at runtime. Plugins or code can dynamically load and change properties and
 * this can fire any registered callback handlers.
 * </p>
 *
 * <h3>Examples</h3>
 * <pre>{@code
 *
 *  int port = Config.getInt("app.port", 8090);
 *
 *  String topicName = Config.get("app.topic.name");
 *
 *  List<Integer> codes = Config.getList().ofInt("my.codes", 42, 54);
 *
 * }</pre>
 */
@NonNullApi
public class Config {

  private static final Configuration data = CoreConfiguration.initialise();

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
   * Return the configuration for a path.
   *
   * <h3>Examples</h3>
   *
   * <p>Say you have a set of properties like this <br>
   * <br>
   * example.path.prefix1=1<br>
   * example.path.prefix2=2<br>
   *
   * <pre>{@code
   * Configuration config = Config.forPath("example.path");
   * Configuration config2 = Config.forPath("example").forPath("path");
   * // will output "1"
   * int number = config.getInt("prefix1");
   * // will output "2"
   * number = config2.getInt("prefix2");
   * }</pre>
   */
  public static Configuration forPath(String pathPrefix) {
    return data.forPath(pathPrefix);
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
   * Return boolean configuration value with the given default value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * <pre>{@code
   *
   *   if (Config.enabled("feature.cleanup")) {
   *     ...
   *   }
   *
   * }</pre>
   *
   * @param key The configuration key
   * @return True when configuration value is true
   */
  public static boolean enabled(String key) {
    return getBool(key);
  }

  /**
   * Return boolean configuration value with the given default value.
   *
   * <pre>{@code
   *
   *   if (Config.enabled("feature.cleanup", true)) {
   *     ...
   *   }
   *
   * }</pre>
   *
   * @param key The configuration key
   * @return True when configuration value is true
   */
  public static boolean enabled(String key, boolean enabledDefault) {
    return getBool(key, enabledDefault);
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
   * Return a decimal configuration value.
   *
   * @param key The configuration key
   * @return The configured value
   */
  public static BigDecimal getDecimal(String key) {
    return data.getDecimal(key);
  }

  /**
   * Return a decimal configuration value with a default value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key          The configuration key
   * @param defaultValue The default value
   * @return The configured value
   */
  public static BigDecimal getDecimal(String key, String defaultValue) {
    return data.getDecimal(key, defaultValue);
  }

  /**
   * Return a URL configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  public static URL getURL(String key) {
    return data.getURL(key);
  }

  /**
   * Return a URL configuration value with a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value
   * @return The configured value
   */
  public static URL getURL(String key, String defaultValue) {
    return data.getURL(key, defaultValue);
  }

  /**
   * Return a URI configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  public static URI getURI(String key) {
    return data.getURI(key);
  }

  /**
   * Return a URI configuration value with a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value
   * @return The configured value
   */
  public static URI getURI(String key, String defaultValue) {
    return data.getURI(key, defaultValue);
  }


  /**
   * Return a Duration configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  public static Duration getDuration(String key) {
    return data.getDuration(key);
  }

  /**
   * Return a Duration configuration value with a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value
   * @return The configured value
   */
  public static Duration getDuration(String key, String defaultValue) {
    return data.getDuration(key, defaultValue);
  }


  /**
   * Return the enum configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param type The enum type
   * @param key  The configuration key
   * @return The configured value
   */
  public static <T extends Enum<T>> T getEnum(Class<T> type, String key) {
    return data.getEnum(type, key);
  }

  /**
   * Return the enum configuration value with a default value.
   *
   * @param type         The enum type
   * @param key          The configuration key
   * @param defaultValue The default value
   * @return The configured value
   */
  public static <T extends Enum<T>> T getEnum(Class<T> type, String key, T defaultValue) {
    return data.getEnum(type, key, defaultValue);
  }

  /**
   * Return a List of values configured.
   *
   * <pre>{@code
   *
   *  List<Integer> codes = Config.list().ofInt("my.codes", 97, 45);
   *
   * }</pre>
   */
  public static Configuration.ListValue list() {
    return data.list();
  }

  /**
   * Deprecated migrate to list().
   */
  @Deprecated
  public static Configuration.ListValue getList() {
    return list();
  }

  /**
   * Return a Set of values configured.
   *
   * <pre>{@code
   *
   *  Set<String> operations = Config.set().of("my.operations", "put","delete");
   *
   * }</pre>
   */
  public static Configuration.SetValue set() {
    return data.set();
  }

  /**
   * Deprecated migrate to set().
   */
  @Deprecated
  public static Configuration.SetValue getSet() {
    return set();
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
   * Clear the value for the given key.
   *
   * @param key The configuration key we want to clear
   */
  public static void clearProperty(String key) {
    data.clearProperty(key);
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
   * Register a callback for a change to the any configuration key.
   *
   * @param callback The callback handling to fire when a configuration changes.
   */
  public static void onAnyChange(Consumer<String> callback) {
    data.onAnyChange(callback);
  }

  /**
   * Register a callback for a change to the given configuration key as an Int value.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  public static void onChangeInt(String key, IntConsumer callback) {
    data.onChangeInt(key, callback);
  }

  /**
   * Register a callback for a change to the given configuration key as an Long value.
   *
   * @param key      The configuration key we want to detect changes to
   * @param callback The callback handling to fire when the configuration changes.
   */
  public static void onChangeLong(String key, LongConsumer callback) {
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
