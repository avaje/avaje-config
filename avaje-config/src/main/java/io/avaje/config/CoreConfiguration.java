package io.avaje.config;

import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;

import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import static java.util.Objects.requireNonNull;

/**
 * Core implementation of Configuration.
 */
@NonNullApi
final class CoreConfiguration implements Configuration {

  private final EventLog log;
  private final ModifyAwareProperties properties;
  private final Map<String, OnChangeListener> callbacks = new ConcurrentHashMap<>();
  private final CoreListValue listValue;
  private final CoreSetValue setValue;
  private boolean loadedSystemProperties;
  private FileWatch watcher;
  private Timer timer;
  private final String pathPrefix;

  CoreConfiguration(EventLog log, Properties source) {
    this(log, CoreEntry.newMap(source), "");
  }

  CoreConfiguration(EventLog log, CoreEntry.Map source, String prefix) {
    this.log = log;
    this.properties = new ModifyAwareProperties(this, source);
    this.listValue = new CoreListValue(this);
    this.setValue = new CoreSetValue(this);
    this.pathPrefix = prefix;
  }

  /**
   * Initialise the configuration which loads all the property sources.
   */
  static Configuration initialise() {
    EventLog log = ServiceLoader.load(EventLog.class).findFirst().orElseGet(DefaultEventLog::new);
    log.preInitialisation();
    final InitialLoader loader = new InitialLoader(log);
    CoreConfiguration configuration = new CoreConfiguration(log, loader.load());
    configuration.loadSources();
    loader.initWatcher(configuration);
    configuration.initSystemProperties();
    configuration.logMessage(loader);
    log.postInitialisation();
    return configuration;
  }

  EventLog log() {
    return log;
  }

  private void logMessage(InitialLoader loader) {
    String watchMsg = watcher == null ? "" : watcher.toString();
    String intoMsg = loadedSystemProperties ? " into System properties" : "";
    log.log(Level.INFO, "Loaded properties from {0}{1} {2}", loader.loadedFrom(), intoMsg, watchMsg);
  }

  void initSystemProperties() {
    if (getBool("config.load.systemProperties", false)) {
      loadIntoSystemProperties();
    }
  }

  private void loadSources() {
    for (ConfigurationSource source : ServiceLoader.load(ConfigurationSource.class)) {
      source.load(this);
    }
  }

  void setWatcher(FileWatch watcher) {
    this.watcher = watcher;
  }

  @Override
  public String toString() {
    return "watcher:" + watcher + " properties:" + properties;
  }

  @Override
  public int size() {
    return properties.size();
  }

  @Override
  public void schedule(long delayMillis, long periodMillis, Runnable runnable) {
    synchronized (this) {
      if (timer == null) {
        timer = new Timer("ConfigTimer", true);
      }
      timer.schedule(new Task(log, runnable), delayMillis, periodMillis);
    }
  }

  @Override
  public Properties eval(Properties source) {
    final ExpressionEval exprEval = InitialLoader.evalFor(source);
    Properties dest = new Properties();
    Enumeration<?> names = source.propertyNames();
    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();
      dest.setProperty(name, exprEval.eval(source.getProperty(name)));
    }
    return dest;
  }

  @Override
  public void evalModify(Properties properties) {
    final ExpressionEval exprEval = InitialLoader.evalFor(properties);
    Enumeration<?> names = properties.propertyNames();
    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();
      String origValue = properties.getProperty(name);
      String newValue = exprEval.eval(origValue);
      if (!Objects.equals(newValue, origValue)) {
        properties.setProperty(name, newValue);
      }
    }
  }

  @Override
  public void loadIntoSystemProperties() {
    properties.loadIntoSystemProperties(set().of("system.excluded.properties"));
    loadedSystemProperties = true;
  }

  @Override
  public Properties asProperties() {
    return properties.asProperties();
  }

  @Override
  public Configuration forPath(String pathPrefix) {
    final var dotPrefix = pathPrefix + '.';
    final var dotLength = dotPrefix.length();
    final var newProps = CoreEntry.newMap();
    properties.entries.forEach((key, entry) -> {
      if (key.startsWith(dotPrefix)) {
        newProps.put(key.substring(dotLength), entry);
      } else if (key.equals(pathPrefix)) {
        newProps.put("", entry);
      }
    });
    return new CoreConfiguration(log, newProps, dotPrefix);
  }

  @Override
  public ListValue list() {
    return listValue;
  }

  @Override
  public SetValue set() {
    return setValue;
  }

  @Nullable
  String value(String key) {
    return properties.entry(key).value();
  }

  private String required(String key) {
    String value = value(key);
    if (value == null) {
      throw new IllegalStateException("Missing required configuration parameter [" + pathPrefix + key + "]");
    }
    return value;
  }

  @Override
  public String get(String key) {
    return required(key);
  }

  @Override
  public String get(String key, String defaultValue) {
    requireNonNull(key, "key is required");
    requireNonNull(defaultValue, "defaultValue is required, use getOptional() instead");
    return properties.entry(key, defaultValue).value();
  }

  @Override
  public Optional<String> getOptional(String key) {
    return Optional.ofNullable(value(key));
  }

  @Override
  public boolean getBool(String key) {
    return Boolean.parseBoolean(required(key));
  }

  @Override
  public boolean getBool(String key, boolean defaultValue) {
    return properties.getBool(key, defaultValue);
  }

  @Override
  public int getInt(String key) {
    return Integer.parseInt(required(key));
  }

  @Override
  public int getInt(String key, int defaultValue) {
    final String val = value(key);
    return (val == null) ? defaultValue : Integer.parseInt(val);
  }

  @Override
  public long getLong(String key) {
    return Long.parseLong(required(key));
  }

  @Override
  public long getLong(String key, long defaultValue) {
    final String val = value(key);
    return (val == null) ? defaultValue : Long.parseLong(val);
  }

  @Override
  public BigDecimal getDecimal(String key) {
    return new BigDecimal(get(key));
  }

  @Override
  public BigDecimal getDecimal(String key, String defaultValue) {
    return new BigDecimal(get(key, defaultValue));
  }

  @Override
  public URL getURL(String key) {
    try {
      return new URL(get(key));
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid url for " + pathPrefix + key, e);
    }
  }

  @Override
  public URL getURL(String key, String defaultValue) {
    try {
      return new URL(get(key, defaultValue));
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid url for " + pathPrefix + key, e);
    }
  }

  @Override
  public URI getURI(String key) {
    return URI.create(get(key));
  }

  @Override
  public URI getURI(String key, String defaultValue) {
    return URI.create(get(key, defaultValue));
  }

  @Override
  public Duration getDuration(String key) {
    return Duration.parse(get(key));
  }

  @Override
  public Duration getDuration(String key, String defaultValue) {
    return Duration.parse(get(key, defaultValue));
  }

  @Override
  public <T extends Enum<T>> T getEnum(Class<T> cls, String key) {
    requireNonNull(cls, "Enum class is required");
    return Enum.valueOf(cls, get(key));
  }

  @Override
  public <T extends Enum<T>> T getEnum(Class<T> cls, String key, T defaultValue) {
    requireNonNull(cls, "Enum class is required");
    return Enum.valueOf(cls, get(key, defaultValue.name()));
  }

  private OnChangeListener onChange(String key) {
    requireNonNull(key, "key is required");
    return callbacks.computeIfAbsent(key, s -> new OnChangeListener());
  }

  @Override
  public void onChange(String key, Consumer<String> callback) {
    onChange(key).register(callback);
  }

  @Override
  public void onChangeInt(String key, IntConsumer callback) {
    onChange(key).register(newValue -> callback.accept(Integer.parseInt(newValue)));
  }

  @Override
  public void onChangeLong(String key, LongConsumer callback) {
    onChange(key).register(newValue -> callback.accept(Long.parseLong(newValue)));
  }

  @Override
  public void onChangeBool(String key, Consumer<Boolean> callback) {
    onChange(key).register(newValue -> callback.accept(Boolean.parseBoolean(newValue)));
  }

  private void fireOnChange(String key, String value) {
    OnChangeListener listener = callbacks.get(key);
    if (listener != null) {
      listener.fireOnChange(value);
    }
  }

  @Override
  public void setProperty(String key, String newValue) {
    requireNonNull(key, "key is required");
    requireNonNull(newValue, "newValue is required, use clearProperty()");
    properties.setValue(key, newValue);
  }

  @Override
  public void clearProperty(String key) {
    requireNonNull(key, "key is required");
    properties.clearValue(key);
  }

  private static class OnChangeListener {

    private final List<Consumer<String>> callbacks = new ArrayList<>();

    void register(Consumer<String> callback) {
      callbacks.add(callback);
    }

    void fireOnChange(String value) {
      for (Consumer<String> callback : callbacks) {
        callback.accept(value);
      }
    }
  }

  private static class ModifyAwareProperties {

    private final CoreEntry.Map entries;
    private final Configuration.ExpressionEval eval;
    private final CoreConfiguration config;

    ModifyAwareProperties(CoreConfiguration config, CoreEntry.Map entries) {
      this.config = config;
      this.entries = entries;
      this.eval = new CoreExpressionEval(entries);
    }

    @Override
    public String toString() {
      return entries.toString();
    }

    int size() {
      return entries.size();
    }

    /**
     * Set a property with expression evaluation.
     */
    void setValue(String key, String newValue) {
      newValue = eval.eval(newValue);
      final CoreEntry oldEntry = entries.put(key, newValue);
      if (oldEntry == null || !Objects.equals(newValue, oldEntry.value())) {
        config.fireOnChange(key, newValue);
      }
    }

    void clearValue(String key) {
      final CoreEntry oldValue = entries.remove(key);
      if (oldValue != null) {
        config.fireOnChange(key, null);
      }
    }

    /**
     * Get boolean property with caching to take into account misses/default values
     * and parseBoolean(). As getBool is expected to be used in a dynamic feature toggle
     * with very high concurrent use.
     */
    boolean getBool(String key, boolean defaultValue) {
      return entry(key, String.valueOf(defaultValue)).boolValue();
    }

    CoreEntry entry(String key) {
      return _entry(key, null);
    }

    CoreEntry entry(String key, String defaultValue) {
      return _entry(key, defaultValue);
    }

    /**
     * Get property with caching taking into account defaultValue and "null".
     */
    private CoreEntry _entry(String key, @Nullable String defaultValue) {
      CoreEntry value = entries.get(key);
      if (value == null) {
        // defining property at runtime with System property backing
        String systemValue = System.getProperty(key);
        value = systemValue != null ? CoreEntry.of(systemValue) : defaultValue != null ? CoreEntry.of(defaultValue) : CoreEntry.NULL_ENTRY;
        entries.put(key, value);
      } else if (value.isNull() && defaultValue != null) {
        value = CoreEntry.of(defaultValue);
        entries.put(key, value);
      }
      return value;
    }

    void loadIntoSystemProperties(Set<String> excludedSet) {
      entries.loadIntoSystemProperties(excludedSet);
    }

    Properties asProperties() {
      return entries.asProperties();
    }
  }

  private static class Task extends TimerTask {

    private final EventLog log;
    private final Runnable runnable;

    private Task(EventLog log, Runnable runnable) {
      this.log = log;
      this.runnable = runnable;
    }

    @Override
    public void run() {
      try {
        runnable.run();
      } catch (Exception e) {
        log.log(Level.ERROR, "Error executing timer task", e);
      }
    }
  }

}
