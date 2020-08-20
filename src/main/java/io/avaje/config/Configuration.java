package io.avaje.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

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
 *  List<Integer> codes = Config.getList().ofInt("my.codes", 42, 54);
 *
 * }</pre>
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
   *  List<Integer> codes = Config.getList().ofInt("my.codes", 97, 45);
   *
   * }</pre>
   */
  ListValue getList();

  /**
   * Return a Set of values configured.
   *
   * <pre>{@code
   *
   *  Set<String> operations = Config.getSet().of("my.operations", "put","delete");
   *
   * }</pre>
   */
  SetValue getSet();

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
   *  List<Integer> codes = Config.getList().ofInt("my.codes", 42, 54);
   *
   * }</pre>
   */
  interface ListValue {

    /**
     * Return the list of values for the key returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    List<String> of(String key);

    /**
     * Return the list of values for the key returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    List<String> of(String key, String... defaultValues);

    /**
     * Return the list of integer values for the key returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    List<Integer> ofInt(String key);

    /**
     * Return the list of integer values for the key returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    List<Integer> ofInt(String key, int... defaultValues);

    /**
     * Return the list of long values for the key returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    List<Long> ofLong(String key);

    /**
     * Return the long values for the key returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    List<Long> ofLong(String key, long... defaultValues);
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
     * Return the Set of values for the key returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty Set if not defined
     */
    Set<String> of(String key);

    /**
     * Return the Set of values for the key returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    Set<String> of(String key, String... defaultValues);

    /**
     * Return the list of integer values for the key returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    Set<Integer> ofInt(String key);

    /**
     * Return the list of integer values for the key returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    Set<Integer> ofInt(String key, int... defaultValues);

    /**
     * Return the list of long values for the key returning an empty
     * collection if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or an empty list if not defined
     */
    Set<Long> ofLong(String key);

    /**
     * Return the long values for the key returning the default values
     * if the configuration is not defined.
     *
     * @param key The configuration key
     * @return The configured values or default values
     */
    Set<Long> ofLong(String key, long... defaultValues);
  }
}
