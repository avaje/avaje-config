package io.avaje.config;

import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

/**
 * Configuration entry.
 */
@NonNullApi
final class CoreEntry {

  /**
   * Entry used to represent no entry / null.
   */
  static final CoreEntry NULL_ENTRY = new CoreEntry();

  private final String value;
  private final boolean boolValue;

  /**
   * Return a new empty map for entries.
   */
  static Map newMap() {
    return new CoreEntry.Map();
  }

  /**
   * Return a new map populated from the given Properties.
   */
  static Map newMap(Properties source) {
    return new CoreEntry.Map(source);
  }

  /**
   * Return an entry for the given value.
   */
  static CoreEntry of(@Nullable String val) {
    return val == null ? NULL_ENTRY : new CoreEntry(val);
  }

  /**
   * Construct for our special NULL entry.
   */
  private CoreEntry() {
    this.value = null;
    this.boolValue = false;
  }

  private CoreEntry(String value) {
    requireNonNull(value);
    this.value = value;
    this.boolValue = Boolean.parseBoolean(value);
  }

  String value() {
    return value;
  }

  boolean boolValue() {
    return boolValue;
  }

  boolean isNull() {
    return value == null;
  }

  /**
   * A map like container of CoreEntry entries.
   */
  static class Map {

    private final java.util.Map<String, CoreEntry> map = new ConcurrentHashMap<>();

    Map() {
    }

    Map(Properties source) {
      source.forEach((key, value) -> {
        if (value != null) {
          map.put(key.toString(), CoreEntry.of(value.toString()));
        }
      });
    }

    int size() {
      return map.size();
    }

    @Nullable
    CoreEntry get(String key) {
      return map.get(key);
    }

    void put(String key, CoreEntry value) {
      map.put(key, value);
    }

    @Nullable
    CoreEntry put(String key, String value) {
      return map.put(key, CoreEntry.of(value));
    }

    @Nullable
    CoreEntry remove(String key) {
      return map.remove(key);
    }

    @Nullable
    String raw(String key) {
      final CoreEntry entry = map.get(key);
      return entry == null ? null : entry.value();
    }

    Properties asProperties() {
      Properties props = new Properties();
      map.forEach((key, entry) -> {
        if (!entry.isNull()) {
          props.setProperty(key, entry.value());
        }
      });
      return props;
    }

    void loadIntoSystemProperties(Set<String> excludedSet) {
      map.forEach((key, entry) -> {
        if (!excludedSet.contains(key) && !entry.isNull()) {
          System.setProperty(key, entry.value());
        }
      });
    }

    void forEach(BiConsumer<String, CoreEntry> consumer) {
      map.forEach(consumer);
    }
  }
}
