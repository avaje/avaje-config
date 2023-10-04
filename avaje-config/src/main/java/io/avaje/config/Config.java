package io.avaje.config;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;

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
 *  List<Integer> codes = Config.list().ofInt("my.codes", 42, 54);
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
   * Return a configuration value that might not exist.
   *
   * @param key          The configuration key
   * @param defaultValue The default value that can be null
   * @return The configured value wrapped as optional
   */
  public static Optional<String> getOptional(String key, @Nullable String defaultValue) {
    return data.getOptional(key, defaultValue);
  }

  /**
   * Return a configuration value as String or null if it is not defined.
   * <p>
   * This is an alternative to {@link #getOptional(String)} for cases where
   * we prefer to work with null values rather than Optional.
   *
   * @param key The configuration key
   * @return The configured value or null if not set
   */
  @Nullable
  public static String getNullable(String key) {
    return data.getNullable(key);
  }

  /**
   * Return a configuration value as String or null if it is not defined.
   * <p>
   * This is an alternative to {@link #getOptional(String)} for cases where
   * we prefer to work with null values rather than Optional.
   *
   * @param key The configuration key
   * @param defaultValue The default value that can be null
   * @return The configured value or null if not set
   */
  @Nullable
  public static String getNullable(String key, @Nullable String defaultValue) {
    return data.getNullable(key, defaultValue);
  }

  /**
   * Return boolean configuration value with the given default value.
   * <p>
   * This is the same as {@link #getBool( String)}.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
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
    return data.enabled(key);
  }

  /**
   * Return boolean configuration value with the given default value.
   * <p>
   * This is the same as {@link #getBool( String, boolean)}.
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
    return data.enabled(key, enabledDefault);
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
   * Apply a mapping function to the value returned.
   *
   * @param key             The configuration key
   * @param mappingFunction the mapping function to execute
   * @return The mapped value
   */
  public static <T> T getAs(String key, Function<String, T> mappingFunction) {
    return data.getAs(key, mappingFunction);
  }

  /**
   * Apply a mapping function to the value returned as an optional.
   *
   * @param key             The configuration key
   * @param mappingFunction the mapping function to execute
   * @return The mapped value
   */
  public static <T> Optional<T> getAsOptional(String key, Function<String, T> mappingFunction) {
    return data.getAsOptional(key, mappingFunction);
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
   * Create an event builder to make changes to the configuration.
   * <pre>{@code
   *
   *   configuration.eventBuilder("MyChanges")
   *     .put("someKey", "val0")
   *     .put("someOther.key", "42")
   *     .remove("foo")
   *     .publish();
   *
   * }</pre>
   *
   * @param name The name of the event which defines the source of the configuration value.
   * @see #onChange(Consumer, String...)
   */
  public static ModificationEvent.Builder eventBuilder(String name) {
    return data.eventBuilder(name);
  }

  /**
   * Set a single configuration value. Note that {@link #eventBuilder(String)} should be
   * used to fluently set multiple configuration values.
   * <p>
   * This will fire configuration callback listeners that are registered.
   */
  public static void setProperty(String key, String value) {
    data.setProperty(key, value);
  }

  /**
   * Add configuration values via a map.
   *
   * <p>This will fire configuration callback listeners that are registered.
   */
  public static void putAll(Map<String, ?> map) {
    data.putAll(map);
  }

  /**
   * Clear the value for the given key. Note that {@link #eventBuilder(String)} should be
   * used when setting multiple configuration values.
   * <p>
   * This will fire configuration callback listeners that are registered.
   *
   * @param key The configuration key we want to clear
   */
  public static void clearProperty(String key) {
    data.clearProperty(key);
  }

  /**
   * Register an event listener that will be notified of configuration changes.
   * <p>
   * If we are only interested in changes to a single property it is easier to use
   * {@link #onChange(String, Consumer)} or the variants for int, long, boolean
   * onChangeInt(), onChangeLong(), onChangeBool().
   * <p>
   * Typically, we use this when we are interested in changes to multiple properties
   * and want to get and act on the values of multiple properties.
   *
   * <pre>{@code
   *  configuration.onChange((modificationEvent) -> {
   *
   *    String newValue = modificationEvent.configuration().get("myFirstKey");
   *    int newInt = modificationEvent.configuration().getInt("myOtherKey");
   *    // do something ...
   *
   *  });
   *
   *  }</pre>
   * <p>
   * When we are only interested if some specific properties have changed then we
   * can define those. The event listener will be invoked if there is a change to
   * any of those keys.
   *
   * <pre>{@code
   *  configuration.onChange((event) -> {
   *
   *    String newValue = event.configuration().get("myFirstInterestingKey");
   *    int newInt = event.configuration().getInt("myOtherInterestingKey");
   *    // do something ...
   *
   *  }, "myFirstInterestingKey", "myOtherInterestingKey");
   *
   *  }</pre>
   *
   * @param bulkChangeEventListener The listener that is called when changes have occurred
   * @param keys                    Optionally specify keys when the listener is only interested
   *                                if changes are made for these specific properties
   */
  public static void onChange(Consumer<ModificationEvent> bulkChangeEventListener, String... keys) {
    data.onChange(bulkChangeEventListener, keys);
  }

  /**
   * Register a callback for a change to the given configuration key.
   * <p>
   * Use this when we are only interested in changes to a single configuration property.
   * If we are interested in multiple properties we should use {@link #onChange(Consumer, String...)}
   *
   * <pre>{@code
   *
   *   configuration.onChange("myKey", (newValue) -> {
   *
   *     // do something with the newValue ...
   *
   *   )};
   *
   * }</pre>
   *
   * @param key                          The configuration key we want to detect changes to
   * @param singlePropertyChangeListener The callback handling to fire when the configuration changes.
   */
  public static void onChange(String key, Consumer<String> singlePropertyChangeListener) {
    data.onChange(key, singlePropertyChangeListener);
  }

  /**
   * Register a callback for a change to the given configuration key as an Int value.
   * <p>
   * Use this when we are only interested in changes to a single configuration property.
   * If we are interested in multiple properties we should use {@link #onChange(Consumer, String...)}
   *
   * @param key                          The configuration key we want to detect changes to
   * @param singlePropertyChangeListener The callback handling to fire when the configuration changes.
   */
  public static void onChangeInt(String key, IntConsumer singlePropertyChangeListener) {
    data.onChangeInt(key, singlePropertyChangeListener);
  }

  /**
   * Register a callback for a change to the given configuration key as a Long value.
   * <p>
   * Use this when we are only interested in changes to a single configuration property.
   * If we are interested in multiple properties we should use {@link #onChange(Consumer, String...)}
   *
   * @param key                          The configuration key we want to detect changes to
   * @param singlePropertyChangeListener The callback handling to fire when the configuration changes.
   */
  public static void onChangeLong(String key, LongConsumer singlePropertyChangeListener) {
    data.onChangeLong(key, singlePropertyChangeListener);
  }

  /**
   * Register a callback for a change to the given configuration key as a Boolean value.
   * <p>
   * Use this when we are only interested in changes to a single configuration property.
   * If we are interested in multiple properties we should use {@link #onChange(Consumer, String...)}
   *
   * @param key                          The configuration key we want to detect changes to
   * @param singlePropertyChangeListener The callback handling to fire when the configuration changes.
   */
  public static void onChangeBool(String key, Consumer<Boolean> singlePropertyChangeListener) {
    data.onChangeBool(key, singlePropertyChangeListener);
  }
}
