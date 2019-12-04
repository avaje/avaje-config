package io.avaje.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class ConfigurationData {

  private final ModifyAwareProperties properties;

  private final Map<String, OnChangeListener> callbacks = new ConcurrentHashMap<>();

  ConfigurationData(Properties source) {
    this.properties = new ModifyAwareProperties();
    this.properties.loadAll(source);
    this.properties.registerListener(this);
  }

  Properties asProperties() {
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

  String get(String key) {
    return getRequired(key);
  }

  String get(String key, String defaultValue) {
    final String value = getProperty(key);
    return (value != null) ? value : defaultValue;
  }

  boolean getBool(String key) {
    return Boolean.parseBoolean(getRequired(key));
  }

  boolean getBool(String key, boolean defaultValue) {
    String val = getProperty(key);
    return (val == null) ? defaultValue : Boolean.parseBoolean(val);
  }

  int getInt(String key) {
    return Integer.parseInt(getRequired(key));
  }

  int getInt(String key, int defaultValue) {
    String val = getProperty(key);
    return (val == null) ? defaultValue : Integer.parseInt(val);
  }

  long getLong(String key) {
    return Long.parseLong(getRequired(key));
  }

  long getLong(String key, long defaultValue) {
    String val = getProperty(key);
    return (val == null) ? defaultValue : Long.parseLong(val);
  }

  void onChange(String key, Consumer<String> callback) {
    onChangeRegister(DataType.STRING, key, callback);
  }

  void onChangeInt(String key, Consumer<Integer> callback) {
    onChangeRegister(DataType.INT, key, callback);
  }

  void onChangeLong(String key, Consumer<Long> callback) {
    onChangeRegister(DataType.LONG, key, callback);
  }

  void onChangeBool(String key, Consumer<Boolean> callback) {
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

  void setProperty(String key, String newValue) {
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

    private ConfigurationData data;

    ModifyAwareProperties() {
      super();
    }

    void registerListener(ConfigurationData data) {
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
