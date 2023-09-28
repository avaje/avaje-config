package io.avaje.config;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;

/**
 * Configuration API for accessing property values and registering onChange listeners.
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
public interface Configuration {

  /**
   * Return the loaded properties as standard Properties map.
   */
  Properties asProperties();

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
  Configuration forPath(String pathPrefix);

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
   * Return a configuration value that might not exist.
   *
   * @param key          The configuration key
   * @param defaultValue The default value that can be null
   * @return The configured value wrapped as optional
   */
  Optional<String> getOptional(String key, @Nullable String defaultValue);

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
  String getNullable(String key);

  /**
   * Return a configuration value as String or null if it is not defined.
   * <p>
   * This is an alternative to {@link #getOptional(String)} for cases where
   * we prefer to work with null values rather than Optional.
   *
   * @param key          The configuration key
   * @param defaultValue The default value that can be null
   * @return The configured value or null if not set
   */
  @Nullable
  String getNullable(String key, @Nullable String defaultValue);

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
   * Return a decimal configuration value.
   *
   * @param key The configuration key
   * @return The configured value
   */
  BigDecimal getDecimal(String key);

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
  BigDecimal getDecimal(String key, String defaultValue);

  /**
   * Return a URI configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  URI getURI(String key);

  /**
   * Return a URI configuration value with a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value
   * @return The configured value
   */
  URI getURI(String key, String defaultValue);

  /**
   * Return a Duration configuration value.
   * <p>
   * IllegalStateException is thrown if the value is not defined in configuration.
   * </p>
   *
   * @param key The configuration key
   * @return The configured value
   */
  Duration getDuration(String key);

  /**
   * Return a Duration configuration value with a default value.
   *
   * @param key          The configuration key
   * @param defaultValue The default value
   * @return The configured value
   */
  Duration getDuration(String key, String defaultValue);

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
  <T extends Enum<T>> T getEnum(Class<T> type, String key);

  /**
   * Apply a mapping function to the value returned.
   *
   * @param key The configuration key
   * @param mappingFunction the mapping function to execute
   * @return The mapped value
   */
  <T> T getAs(String key, Function<String, T> mappingFunction);

  /**
   * Apply a mapping function to the value returned.
   *
   * @param key The configuration key
   * @param mappingFunction the mapping function to execute
   * @return The mapped value
   */
  <T> Optional<T> getAsOptional(String key, Function<String, T> mappingFunction);

  /**
   * Return the enum configuration value with a default value.
   *
   * @param type         The enum type
   * @param key          The configuration key
   * @param defaultValue The default value
   * @return The configured value
   */
  <T extends Enum<T>> T getEnum(Class<T> type, String key, T defaultValue);

  /**
   * Return a List of values configured.
   *
   * <pre>{@code
   *
   *  List<Integer> codes = Config.list().ofInt("my.codes", 97, 45);
   *
   * }</pre>
   */
  ListValue list();

  /**
   * Return a Set of values configured.
   *
   * <pre>{@code
   *
   *  Set<String> operations = Config.getSet().of("my.operations", "put","delete");
   *
   * }</pre>
   */
  SetValue set();

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
  ModificationEvent.Builder eventBuilder(String name);

  /**
   * Set a single configuration value. Note that {@link #eventBuilder(String)} should be
   * used when setting multiple configuration values.
   * <p>
   * This will fire configuration callback listeners that are registered.
   */
  void setProperty(String key, String value);

  /**
   * Add configuration values via a map.
   * <p>
   * This will fire configuration callback listeners that are registered.
   */
  void putAll(Map<String, ?> map);

  /**
   * Clear the value for the given key. Note that {@link #eventBuilder(String)} should be
   * used when setting multiple configuration values.
   * <p>
   * This will fire configuration callback listeners that are registered.
   */
  void clearProperty(String key);

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
  void onChange(Consumer<ModificationEvent> bulkChangeEventListener, String... keys);

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
   * @param key  The configuration key we want to detect changes to
   * @param singlePropertyChangeListener The callback handling to fire when the configuration changes.
   */
  void onChange(String key, Consumer<String> singlePropertyChangeListener);

  /**
   * Register a callback for a change to the given configuration key as an Int value.
   * <p>
   * Use this when we are only interested in changes to a single configuration property.
   * If we are interested in multiple properties we should use {@link #onChange(Consumer, String...)}
   *
   * @param key  The configuration key we want to detect changes to
   * @param singlePropertyChangeListener The callback handling to fire when the configuration changes.
   */
  void onChangeInt(String key, IntConsumer singlePropertyChangeListener);

  /**
   * Register a callback for a change to the given configuration key as a Long value.
   * <p>
   * Use this when we are only interested in changes to a single configuration property.
   * If we are interested in multiple properties we should use {@link #onChange(Consumer, String...)}
   *
   * @param key  The configuration key we want to detect changes to
   * @param singlePropertyChangeListener The callback handling to fire when the configuration changes.
   */
  void onChangeLong(String key, LongConsumer singlePropertyChangeListener);

  /**
   * Register a callback for a change to the given configuration key as a Boolean value.
   * <p>
   * Use this when we are only interested in changes to a single configuration property.
   * If we are interested in multiple properties we should use {@link #onChange(Consumer, String...)}
   *
   * @param key  The configuration key we want to detect changes to
   * @param singlePropertyChangeListener The callback handling to fire when the configuration changes.
   */
  void onChangeBool(String key, Consumer<Boolean> singlePropertyChangeListener);

  /**
   * Put the loaded properties into System properties.
   */
  void loadIntoSystemProperties();

  /**
   * Return the number of configuration properties.
   */
  int size();

  /**
   * Schedule a task to run periodically with a given delay and period.
   *
   * @param delay  delay in milliseconds before task is to be executed.
   * @param period time in milliseconds between successive task executions.
   * @param task   task to be scheduled.
   */
  void schedule(long delay, long period, Runnable task);

  /**
   * Return a copy of the properties with 'eval' run on all the values.
   */
  Properties eval(Properties properties);

  /**
   * Run eval of the given properties modifying the values if changed.
   */
  void evalModify(Properties properties);

  /**
   * Expression evaluation.
   */
  interface ExpressionEval {

    /**
     * Evaluate a configuration expression.
     */
    String eval(String expression);
  }

  /**
   * Return a List of values for a configuration key.
   *
   * <h3>Example</h3>
   * <pre>{@code
   *
   *  List<Integer> codes = Config.list().ofInt("my.codes", 42, 54);
   *
   * }</pre>
   */
  interface ListValue {

    /**
     * Return the list of values for the key, returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    List<String> of(String key);

    /**
     * Return the list of values for the key, returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    List<String> of(String key, String... defaultValues);

    /**
     * Return the list of integer values for the key, returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    List<Integer> ofInt(String key);

    /**
     * Return the list of integer values for the key, returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    List<Integer> ofInt(String key, int... defaultValues);

    /**
     * Return the list of long values for the key, returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    List<Long> ofLong(String key);

    /**
     * Return the long values for the key, returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    List<Long> ofLong(String key, long... defaultValues);

    /**
     * Apply a mapping function to the values for the given key, returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @param mappingFunction the mapping function to execute on each value
     * @return The configured and mapped values
     */
    <T> List<T> ofType(String key, Function<String, T> mappingFunction);
  }

  /**
   * Return a Set of values configured.
   *
   * <h3>Example</h3>
   * <pre>{@code
   *
   *  Set<String> operations = Config.getSet().of("my.operations", "put","delete");
   *
   * }</pre>
   */
  interface SetValue {

    /**
     * Return the Set of values for the key, returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty Set if not defined
     */
    Set<String> of(String key);

    /**
     * Return the Set of values for the key, returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    Set<String> of(String key, String... defaultValues);

    /**
     * Return the list of integer values for the key, returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    Set<Integer> ofInt(String key);

    /**
     * Return the list of integer values for the key, returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    Set<Integer> ofInt(String key, int... defaultValues);

    /**
     * Return the list of long values for the key, returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    Set<Long> ofLong(String key);

    /**
     * Return the long values for the key, returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    Set<Long> ofLong(String key, long... defaultValues);

    /**
     * Apply a mapping function to the values for the given key, returning an empty collection if
     * the configuration is not defined.
     *
     * @param key The configuration key
     * @param mappingFunction the mapping function to execute on each value
     * @return The configured and mapped values
     */
    <T> Set<T> ofType(String key, Function<String, T> mappingFunction);
  }

  /**
   * Return a Builder for Configuration that is loaded manually (not via the normal resource loading).
   */
  static Builder builder() {
    return new CoreConfigurationBuilder();
  }

  /**
   * Build Configuration manually explicitly loading all the configuration as key value pairs.
   * <p>
   * Building configuration this way does NOT automatically load resources like application.properties
   * and also does NOT load ConfigurationSource. ALL configuration is explicitly loaded via calls
   * to {@link Builder#put(String, String)}, {@link Builder#putAll(Map)}.
   */
  interface Builder {

    /**
     * Put an entry into the configuration.
     */
    Builder put(String key, String value);

    /**
     * Put entries into the configuration.
     */
    Builder putAll(Map<String, ?> sourceMap);

    /**
     * Put entries into the configuration from properties.
     */
    Builder putAll(Properties source);

    /**
     * Optionally set the event runner to use . If not specified a foreground runner will be used.
     */
    Builder eventRunner(ModificationEventRunner eventRunner);

    /**
     * Optionally set the log to use. If not specified then a logger using System.Logger will be used.
     */
    Builder log(ConfigurationLog log);

    /**
     * Optionally set the resource loader to use. If not specified then class path based resource loader is used.
     */
    Builder resourceLoader(ResourceLoader resourceLoader);

    /**
     * Specify to include standard resource loading.
     * <p>
     * This includes the loading of application.properties, application.yaml etc.
     */
    Builder includeResourceLoading();

    /**
     * Build and return the Configuration.
     * <p>
     * Performs evaluation of property values that contain expressions (e.g. {@code ${user.home}})
     * and returns the configuration.
     */
    Configuration build();
  }
}
