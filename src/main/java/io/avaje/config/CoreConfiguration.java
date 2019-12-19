package io.avaje.config;

import io.avaje.config.load.Loader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Core implementation of Configuration.
 */
class CoreConfiguration implements Configuration {

  private final ModifyAwareProperties properties;

  private final Map<String, OnChangeListener> callbacks = new ConcurrentHashMap<>();

  static Configuration load() {
    return new CoreConfiguration(new Loader().load());
  }

  CoreConfiguration(Properties source) {
    this.properties = new ModifyAwareProperties();
    this.properties.loadAll(source);
    this.properties.registerListener(this);
  }

  @Override
  public Properties eval(Properties properties) {

    final ExpressionEval exprEval = Loader.evalFor(properties);

    Properties evalCopy = new Properties();
    Enumeration<?> names = properties.propertyNames();
    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();
      evalCopy.setProperty(name, exprEval.eval(properties.getProperty(name)));
    }
    return evalCopy;
  }

  @Override
  public void loadIntoSystemProperties() {
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      System.setProperty((String) entry.getKey(), (String) entry.getValue());
    }
  }

  @Override
  public Properties asProperties() {
    return properties;
  }

  private String getProperty(String key) {
    String val = properties.getProperty(key);
    return (val != null) ? val : System.getProperty(key);
  }

  private String getRequired(String key) {
    String value = getProperty(key);
    if (value == null) {
      throw new IllegalStateException("Missing required configuration parameter [" + key + "]");
    }
    return value;
  }

  @Override
  public String get(String key) {
    return getRequired(key);
  }

  @Override
  public String get(String key, String defaultValue) {
    final String value = getProperty(key);
    return (value != null) ? value : defaultValue;
  }

  @Override
  public Optional<String> getOptional(String key) {
    return Optional.ofNullable(getProperty(key));
  }

  @Override
  public boolean getBool(String key) {
    return Boolean.parseBoolean(getRequired(key));
  }

  @Override
  public boolean getBool(String key, boolean defaultValue) {
    String val = getProperty(key);
    return (val == null) ? defaultValue : Boolean.parseBoolean(val);
  }

  @Override
  public int getInt(String key) {
    return Integer.parseInt(getRequired(key));
  }

  @Override
  public int getInt(String key, int defaultValue) {
    String val = getProperty(key);
    return (val == null) ? defaultValue : Integer.parseInt(val);
  }

  @Override
  public long getLong(String key) {
    return Long.parseLong(getRequired(key));
  }

  @Override
  public long getLong(String key, long defaultValue) {
    String val = getProperty(key);
    return (val == null) ? defaultValue : Long.parseLong(val);
  }

  @Override
  public void onChange(String key, Consumer<String> callback) {
    onChangeRegister(DataType.STRING, key, callback);
  }

  @Override
  public void onChangeInt(String key, Consumer<Integer> callback) {
    onChangeRegister(DataType.INT, key, callback);
  }

  @Override
  public void onChangeLong(String key, Consumer<Long> callback) {
    onChangeRegister(DataType.LONG, key, callback);
  }

  @Override
  public void onChangeBool(String key, Consumer<Boolean> callback) {
    onChangeRegister(DataType.BOOL, key, callback);
  }

  private void fireOnChange(String key, String value) {
    OnChangeListener listener = callbacks.get(key);
    if (listener != null) {
      listener.fireOnChange(value);
    }
  }

  private void onChangeRegister(DataType type, String key, Consumer<?> callback) {
    callbacks.computeIfAbsent(key, s -> new OnChangeListener()).register(new Callback(type, callback));
  }

  @Override
  public void setProperty(String key, String newValue) {
    properties.setProperty(key, newValue);
  }

  private static class OnChangeListener {

    private final List<Callback> callbacks = new ArrayList<>();

    void register(Callback callback) {
      callbacks.add(callback);
    }

    void fireOnChange(String value) {
      for (Callback callback : callbacks) {
        callback.fireOnChange(value);
      }
    }
  }

  private enum DataType {
    INT,
    LONG,
    BOOL,
    STRING
  }

  @SuppressWarnings("rawtypes")
  private static class Callback {

    private final DataType type;

    private final Consumer consumer;

    Callback(DataType type, Consumer consumer) {
      this.type = type;
      this.consumer = consumer;
    }

    @SuppressWarnings("unchecked")
    void fireOnChange(String value) {
      consumer.accept(convert(value));
    }

    private Object convert(String value) {
      switch (type) {
        case INT:
          return Integer.valueOf(value);
        case LONG:
          return Long.valueOf(value);
        case BOOL:
          return Boolean.valueOf(value);
        default:
          return value;
      }
    }
  }

  private static class ModifyAwareProperties extends Properties {

    private CoreConfiguration data;

    ModifyAwareProperties() {
      super();
    }

    void registerListener(CoreConfiguration data) {
      this.data = data;
    }

    void loadAll(Properties properties) {
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        super.put(entry.getKey(), entry.getValue());
      }
    }

    @Override
    public synchronized Object setProperty(String key, String newValue) {
      Object oldValue;
      if (newValue == null) {
        oldValue = super.remove(key);
      } else {
        oldValue = super.setProperty(key, newValue);
      }
      if (data != null && !Objects.equals(newValue, oldValue)) {
        data.fireOnChange(key, newValue);
      }
      return oldValue;
    }
  }

}
