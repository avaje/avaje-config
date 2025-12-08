package io.avaje.config;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import io.avaje.config.Configuration.Entry;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Configuration entry.
 */
@NullMarked
final class CoreEntry implements Entry {

  /**
   * Entry used to represent no entry / null.
   */
  static final CoreEntry NULL_ENTRY = new CoreEntry();

  private @Nullable final String value;
  private final boolean boolValue;
  private @Nullable final String source;

  /**
   * Return a new empty entryMap for entries.
   */
  static CoreMap newMap() {
    return new CoreEntry.CoreMap();
  }

  /**
   * Return a copy of the entryMap given the source.
   */
  static CoreMap newMap(CoreMap source) {
    return new CoreEntry.CoreMap(source);
  }

  /**
   * Return a new entryMap populated from the given Properties.
   *
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

  @Override
  public String toString() {
    return '{' + value + " source:" + source + '}';
  }

  boolean needsEvaluation() {
    return value != null && value.contains("${");
  }

  @Override
  @Nullable
  public String value() {
    return value;
  }

  boolean boolValue() {
    return boolValue;
  }

  @Override
  @Nullable
  public String source() {
    return source;
  }

  boolean isNull() {
    return value == null;
  }

  /**
   * A entryMap like container of CoreEntry entries.
   */
  static class CoreMap {

    private final Map<String, CoreEntry> entryMap = new ConcurrentHashMap<>();

    CoreMap() {
    }

    CoreMap(CoreMap source) {
      entryMap.putAll(source.entryMap);
    }

    CoreMap(Properties source, String sourceName) {
      source.forEach((key, value) -> {
        if (value != null) {
          entryMap.put(key.toString(), CoreEntry.of(value.toString(), sourceName));
        }
      });
    }

    /**
     * Add all the entries from another source.
     */
    void addAll(CoreMap source) {
      entryMap.putAll(source.entryMap);
    }

    int size() {
      return entryMap.size();
    }

    @Nullable
    CoreEntry get(String key) {
      return entryMap.get(key);
    }

    /**
     * Apply changes returning the set of modified keys.
     */
    Set<String> applyChanges(CoreEventBuilder eventBuilder) {
      Set<String> modifiedKeys = new HashSet<>();
      final var sourceName = "event:" + eventBuilder.name();
      eventBuilder.forEachPut((key, value) -> {
        if (value == null) {
          if (entryMap.remove(key) != null) {
            modifiedKeys.add(key);
          }
        } else if (putIfChanged(key, value, sourceName)) {
          modifiedKeys.add(key);
        }
      });
      return modifiedKeys;
    }

    /**
     * Return true if this is a change in value.
     */
    boolean isChanged(String key, String value) {
      final Entry entry = entryMap.get(key);
      return entry == null || !Objects.equals(entry.value(), value);
    }

    /**
     * Return true if this put resulted in a modification.
     */
    private boolean putIfChanged(String key, String value, String source) {
      final Entry entry = entryMap.get(key);
      if (entry == null) {
        entryMap.put(key, CoreEntry.of(value, source));
        return true;
      } else if (!Objects.equals(entry.value(), value)) {
        entryMap.put(key, CoreEntry.of(value, source + " <- " + entry.source()));
        return true;
      }
      return false;
    }

    Set<String> keys() {
      return entryMap.keySet();
    }

    boolean containsKey(String key) {
      return entryMap.containsKey(key);
    }

    void put(String key, CoreEntry value) {
      entryMap.put(key, value);
    }

    void put(String key, @Nullable String value, String source) {
      entryMap.put(key, CoreEntry.of(value, source));
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
