package io.avaje.config;

import static io.avaje.config.Constants.SYSTEM_PROPS;
import static io.avaje.config.Constants.USER_PROVIDED_DEFAULT;
import static java.lang.System.Logger.Level.ERROR;
import static java.util.Objects.requireNonNull;

import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Core implementation of Configuration.
 */
@NullMarked
final class CoreConfiguration implements Configuration {

  private final Parsers parsers;
  private final ConfigurationLog log;
  private final ModifyAwareProperties properties;
  private final ReentrantLock lock = new ReentrantLock();
  private final List<CoreListener> listeners = new CopyOnWriteArrayList<>();
  private final Map<String, OnChangeListener> callbacks = new ConcurrentHashMap<>();
  private final CoreListValue listValue;
  private final CoreSetValue setValue;
  private final ModificationEventRunner eventRunner;
  private final List<ConfigurationSource> sources;
  private final List<ConfigurationPlugin> plugins;

  private boolean loadedSystemProperties;
  private @Nullable FileWatch watcher;
  private @Nullable Timer timer;
  private final String pathPrefix;

  CoreConfiguration(CoreComponents components, CoreEntry.CoreMap entries) {
    this.parsers = components.parsers();
    this.eventRunner = components.runner();
    this.log = components.log();
    this.sources = components.sources();
    this.plugins = components.plugins();
    this.properties = new ModifyAwareProperties(entries);
    this.listValue = new CoreListValue(this);
    this.setValue = new CoreSetValue(this);
    this.pathPrefix = "";
  }

  CoreConfiguration(CoreConfiguration parent, CoreEntry.CoreMap entries, String prefix) {
    this.parsers = parent.parsers;
    this.eventRunner = parent.eventRunner;
    this.log = parent.log;
    this.sources = parent.sources;
    this.plugins = parent.plugins;
    this.properties = new ModifyAwareProperties(entries);
    this.listValue = new CoreListValue(this);
    this.setValue = new CoreSetValue(this);
    this.pathPrefix = prefix;
  }

  /**
   * For testing purposes.
   */
  CoreConfiguration(CoreEntry.CoreMap entries) {
    this(new CoreComponents(), entries);
  }

  /**
   * Initialise the configuration which loads all the property sources.
   */
  static Configuration initialise() {
    return new CoreConfigurationBuilder()
      .includeResourceLoading()
      .build();
  }

  CoreConfiguration postLoad(@Nullable InitialLoader loader) {
    if (loader != null) {
      loadSources(loader.loadedFrom());
      loader.initWatcher(this);
    }
    initSystemProperties();
    if (loader != null) {
      logMessage(loader);
      applyPlugins();
    }
    log.postInitialisation();
    return this;
  }

  private void applyPlugins() {
    for (ConfigurationPlugin plugin : plugins) {
      plugin.apply(this);
    }
  }

  ConfigurationLog log() {
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

  private void loadSources(Set<String> names) {
    for (ConfigurationSource source : sources) {
      source.load(this);
      names.add("ConfigurationSource:" + source.getClass().getSimpleName());
    }
  }

  void setWatcher(FileWatch watcher) {
    this.watcher = watcher;
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
  public Optional<ConfigParser> parser(String extension) {
    return Optional.ofNullable(parsers.get(extension));
  }

  String eval(String value) {
    return properties.eval(value);
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
  public void reloadSources() {
    for (ConfigurationSource source : sources) {
      source.reload();
    }
  }

  @Override
  public Properties asProperties() {
    return properties.asProperties();
  }

  @Override
  public Configuration forPath(String pathPrefix) {
    final var dotPrefix = pathPrefix + '.';
    final var dotLength = dotPrefix.length();
    final var newEntryMap = CoreEntry.newMap();
    properties.entries.forEach((key, entry) -> {
      if (key.startsWith(dotPrefix)) {
        newEntryMap.put(key.substring(dotLength), entry);
      } else if (key.equals(pathPrefix)) {
        newEntryMap.put("", entry);
      }
    });
    return new CoreConfiguration(this, newEntryMap, dotPrefix);
  }

  @Override
  public Set<String> keys() {
    return properties.entries.keys();
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
  public Optional<Configuration.Entry> entry(String key) {
    return properties.optionalEntry(key);
  }

  @Override
  public String get(String key) {
    return required(key);
  }

  @Override
  @Nullable
  public String getNullable(String key) {
    return value(key);
  }

  @Override
  @Nullable
  public String getNullable(String key, @Nullable String defaultValue) {
    requireNonNull(key, "key is required");
    return properties.entry(key, defaultValue).value();
  }

  @Override
  public String get(String key, String defaultValue) {
    requireNonNull(key, "key is required");
    requireNonNull(defaultValue, "defaultValue is required, use getOptional() instead");
    return requireNonNull(properties.entry(key, defaultValue).value());
  }

  @Override
  public Optional<String> getOptional(String key) {
    return Optional.ofNullable(value(key));
  }

  @Override
  public Optional<String> getOptional(String key, @Nullable String defaultValue) {
    return Optional.ofNullable(getNullable(key, defaultValue));
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

  @Override
  public <T> T getAs(String key, Function<String, T> mappingFunction) {
    requireNonNull(key, "key is required");
    requireNonNull(mappingFunction,"mappingFunction is required");
    final var entry = required(key);
    try {
      return mappingFunction.apply(entry);
    } catch (final Exception e) {
      throw new IllegalStateException("Failed to convert key: " + key + " sourced from: " + properties.entry(key).source() + " with the provided function", e);
    }
  }

  @Override
  public <T> Optional<T> getAsOptional(String key, Function<String, T> mappingFunction) {
    requireNonNull(key, "key is required");
    requireNonNull(mappingFunction, "mappingFunction is required");
    try {
      return Optional.ofNullable(value(key)).map(mappingFunction);
    } catch (final Exception e) {
      throw new IllegalStateException("Failed to convert key: " + key + " sourced from: " + properties.entry(key).source() + " with the provided function", e);
    }
  }

  @Override
  public ModificationEvent.Builder eventBuilder(String name) {
    requireNonNull(name);
    return new CoreEventBuilder(name, this, properties.entryMap());
  }

  void publishEvent(CoreEventBuilder eventBuilder) {
    if (eventBuilder.hasChanges()) {
      lock.lock();
      try {
        eventRunner.run(() -> applyChangesAndPublish(eventBuilder));
      } finally {
        lock.unlock();
      }
    }
  }

  private void applyChangesAndPublish(CoreEventBuilder eventBuilder) {
    Set<String> modifiedKeys = properties.applyChanges(eventBuilder);
    if (!modifiedKeys.isEmpty()) {
      final var event = new CoreModificationEvent(eventBuilder.name(), modifiedKeys, this);
      for (CoreListener listener : listeners) {
        listener.accept(event);
      }
    }
    // legacy per-key listeners
    for (String modifiedKey : modifiedKeys) {
      OnChangeListener listener = callbacks.get(modifiedKey);
      if (listener != null) {
        final String value = properties.valueOrNull(modifiedKey);
        listener.fireOnChange(value);
      }
    }
  }

  @Override
  public void onChange(Consumer<ModificationEvent> eventListener, String... keys) {
    listeners.add(new CoreListener(log, eventListener, keys));
  }

  private OnChangeListener onChange(String key) {
    requireNonNull(key, "key is required");
    return callbacks.computeIfAbsent(key, s -> new OnChangeListener(log));
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

  @Override
  public void setProperty(String key, String newValue) {
    requireNonNull(key, "key is required");
    requireNonNull(newValue, "newValue is required, use clearProperty() to remove a property");
    eventBuilder("SetProperty").put(key, newValue).publish();
  }

  @Override
  public void putAll(Map<String, ?> map) {
    requireNonNull(map, "map cannot be null");
    eventBuilder("PutAll").putAll(map).publish();
  }

  @Override
  public void clearProperty(String key) {
    requireNonNull(key, "key is required");
    eventBuilder("ClearProperty").remove(key).publish();
  }

  private static class OnChangeListener {

    private final ConfigurationLog log;
    private final List<Consumer<String>> callbacks = new ArrayList<>();

    OnChangeListener(ConfigurationLog log) {
      this.log = log;
    }

    void register(Consumer<String> callback) {
      callbacks.add(callback);
    }

    void fireOnChange(String value) {
      for (Consumer<String> callback : callbacks) {
        try {
          callback.accept(value);
        } catch (Exception e) {
          log.log(ERROR, "Error during onChange notification", e);
        }
      }
    }
  }

  static String toEnvKey(String key) {
    return key.replace('.', '_').toUpperCase();
  }

  private static class ModifyAwareProperties {

    private final CoreEntry.CoreMap entries;
    private final Configuration.ExpressionEval eval;

    ModifyAwareProperties(CoreEntry.CoreMap entries) {
      this.entries = entries;
      this.eval = new CoreExpressionEval(entries);
    }

    int size() {
      return entries.size();
    }

    @Nullable
    String eval(String value) {
      return eval.eval(value);
    }

    @Nullable
    String valueOrNull(String key) {
      CoreEntry entry = entries.get(key);
      return entry == null ? null : entry.value();
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

    CoreEntry entry(String key, @Nullable String defaultValue) {
      return _entry(key, defaultValue);
    }

    /**
     * Return the underlying entry for a given key WITHOUT creating it if it does not exist.
     * This also excludes entries that represent a null value.
     */
    Optional<Entry> optionalEntry(String key) {
      return Optional.ofNullable(entries.get(key))
        .filter(entry -> !entry.isNull())
        .map(entry -> entry);
    }

    /**
     * Get property with caching taking into account defaultValue and "null".
     */
    private CoreEntry _entry(String key, @Nullable String defaultValue) {
      CoreEntry value = entries.get(key);
      if (value == null) {
        // defining property at runtime with System property/ENV backing
        value = defaultEntry(defaultValue, systemValue(key));
        entries.put(key, value);
      } else if (value.isNull() && defaultValue != null) {
        value = CoreEntry.of(defaultValue, USER_PROVIDED_DEFAULT);
        entries.put(key, value);
      }
      return value;
    }

    private static CoreEntry defaultEntry(@Nullable String defaultValue, @Nullable String systemValue) {
      if (systemValue != null) {
        return CoreEntry.of(systemValue, SYSTEM_PROPS);
      } else if (defaultValue != null) {
        return CoreEntry.of(defaultValue, USER_PROVIDED_DEFAULT);
      } else {
        return CoreEntry.NULL_ENTRY;
      }
    }

    @Nullable
    private static String systemValue(String key) {
      final String val = System.getProperty(key, System.getenv(key));
      return val != null ? val : System.getenv(toEnvKey(key));
    }

    void loadIntoSystemProperties(Set<String> excludedSet) {
      entries.forEach((key, entry) -> {
        if (!excludedSet.contains(key) && !entry.isNull()) {
          System.setProperty(key, requireNonNull(entry.value()));
        }
      });
    }

    Properties asProperties() {
      Properties props = new Properties();
      entries.forEach((key, entry) -> {
        if (!entry.isNull()) {
          props.setProperty(key, entry.value());
        }
      });
      return props;
    }

    CoreEntry.CoreMap entryMap() {
      return entries;
    }

    Set<String> applyChanges(CoreEventBuilder eventBuilder) {
      return entries.applyChanges(eventBuilder);
    }
  }

  private static class Task extends TimerTask {

    private final ConfigurationLog log;
    private final Runnable runnable;

    private Task(ConfigurationLog log, Runnable runnable) {
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

  /**
   * Run the event listener notifications using the current thread that is publishing the modification.
   */
  static final class ForegroundEventRunner implements ModificationEventRunner {

    @Override
    public void run(Runnable eventListenersNotifyTask) {
      eventListenersNotifyTask.run();
    }
  }
}
