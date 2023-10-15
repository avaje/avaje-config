package io.avaje.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

final class CoreEventBuilder implements ModificationEvent.Builder {

  private final String name;
  private final CoreConfiguration origin;
  private final CoreEntry.CoreMap snapshot;
  private final Map<String, String> changes = new LinkedHashMap<>();


  CoreEventBuilder(String name, CoreConfiguration origin, CoreEntry.CoreMap snapshot) {
    this.name = name;
    this.origin = origin;
    this.snapshot = snapshot; // at the moment we don't mutate the snapshot so could just use the original map
  }

  @Override
  public ModificationEvent.Builder putAll(Map<String, ?> map) {
    map.forEach((key, value) -> {
      requireNonNull(value);
      put(key, value.toString());
    });
    return this;
  }

  @Override
  public ModificationEvent.Builder put(String key, String value) {
    requireNonNull(key);
    requireNonNull(value);
    value = origin.eval(value);
    if (snapshot.isChanged(key, value)) {
      changes.put(key, value);
    }
    return this;
  }

  @Override
  public ModificationEvent.Builder remove(String key) {
    requireNonNull(key);
    if (snapshot.containsKey(key)) {
      changes.put(key, null);
    }
    return this;
  }

  @Override
  public void publish() {
    origin.publishEvent(this);
  }

  boolean hasChanges() {
    return !changes.isEmpty();
  }

  void forEachPut(BiConsumer<String, String> consumer) {
    changes.forEach(consumer);
  }

  String name() {
    return name;
  }
}
