package io.avaje.config;

import io.avaje.config.properties.PropertiesLoader;

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

  private static ConfigurationData data = initData();

  private static ConfigurationData initData() {
    initFromEnvironmentVars();
    Properties properties = PropertiesLoader.load();
    return new ConfigurationData(properties);
  }

  /**
   * If we are in Kubernetes and expose environment variables
   * POD_NAME, POD_NAMESPACE, POD_VERSION, POD_ID we can set these
   * for appInstanceId, appName, appEnvironment, appVersion and appIp.
   */
  private static void initFromEnvironmentVars() {
    initSystemProperty(System.getenv("POD_NAMESPACE"), "appEnvironment");
    initSystemProperty(System.getenv("POD_VERSION"), "appVersion");
    initSystemProperty(System.getenv("POD_IP"), "appIp");

    final String podName = System.getenv("POD_NAME");
    final String podService = podService(podName);
    initSystemProperty(podName, "appInstanceId");
    initSystemProperty(podService, "appName");
  }

  private static void initSystemProperty(String envValue, String key) {
    if (envValue != null && System.getProperty(key) == null) {
      System.setProperty(key, envValue);
    }
  }

  static String podService(String podName) {
    if (podName != null && podName.length() > 16) {
      int p0 = podName.lastIndexOf('-', podName.length() - 16);
      if (p0 > -1) {
        return podName.substring(0, p0);
      }
    }
    return null;
  }

  /**
   * Hide constructor.
   */
  private Config() {
  }

  public static void init() {
    // initialised by class loading
  }

  /**
   * Return the loaded properties as standard Properties map.
   */
  public static Properties asProperties() {
    return data.asProperties();
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
    return Optional.ofNullable(get(key, null));
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
