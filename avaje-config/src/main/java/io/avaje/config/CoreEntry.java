package io.avaje.config;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import io.avaje.lang.NonNullApi;
import io.avaje.lang.Nullable;

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
  private final String source;

  /**
   * Return a new empty entryMap for entries.
   */
  static CoreMap newMap() {
    return new CoreEntry.CoreMap();
  }

  /**
   * Return a new entryMap populated from the given Properties.
   * @param propSource where these properties came from
   */
  static CoreMap newMap(Properties source, String propSource) {
    return new CoreEntry.CoreMap(source, propSource);
  }

  /**
   * Return an entry for the given value.
   */
  static CoreEntry of(@Nullable String val, String source) {
    return val == null ? NULL_ENTRY : new CoreEntry(val, source);
  }

  /**
   * Construct for our special NULL entry.
   */
  private CoreEntry() {
    this.value = null;
    this.boolValue = false;
    this.source = null;
  }

  private CoreEntry(String value, String source) {
    requireNonNull(value);
    this.value = value;
    this.boolValue = Boolean.parseBoolean(value);
    this.source = source;
  }

  String value() {
    return value;
  }

  boolean boolValue() {
    return boolValue;
  }

  String source() {
    return source;
  }

  boolean isNull() {
    return value == null;
  }


  @Override
  public String toString() {
    return "CoreEntry [value=" + value + ", boolValue=" + boolValue + ", source=" + source + "]";
  }

  /**
   * A entryMap like container of CoreEntry entries.
   */
  static class CoreMap {

    private final Map<String, CoreEntry> entryMap = new ConcurrentHashMap<>();

    CoreMap() {
    }

    CoreMap(Properties source, String propSource) {
      source.forEach((key, value) -> {
        if (value != null) {
          entryMap.put(key.toString(), CoreEntry.of(value.toString(), propSource));
        }
      });
    }

    int size() {
      return entryMap.size();
    }

    @Nullable
    CoreEntry get(String key) {
      return entryMap.get(key);
    }

    void put(String key, CoreEntry value) {
      entryMap.put(key, value);
    }

    @Nullable
    CoreEntry put(String key, String value, String source) {
      return entryMap.put(key, CoreEntry.of(value, source));
    }

    @Nullable
    CoreEntry remove(String key) {
      return entryMap.remove(key);
    }

    @Nullable
    String raw(String key) {
      final var entry = entryMap.get(key);
      return entry == null ? null : entry.value();
    }

    void forEach(BiConsumer<String, CoreEntry> consumer) {
      entryMap.forEach(consumer);
    }
  }
}
