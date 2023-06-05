package io.avaje.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

final class CoreSetValue implements Configuration.SetValue {

  private final CoreConfiguration config;

  CoreSetValue(CoreConfiguration config) {
    this.config = config;
  }

  @Override
  public Set<String> of(String key) {
    final String val = config.value(key);
    return val == null ? Collections.emptySet() : split(val);
  }

  @Override
  public Set<String> of(String key, String... defaultValues) {
    final String val = config.value(key);
    return val == null ? stringDefaults(defaultValues) : split(val);
  }

  private static Set<String> stringDefaults(String[] defaultValues) {
    final Set<String> values = new LinkedHashSet<>();
    Collections.addAll(values, defaultValues);
    return values;
  }

  @Override
  public Set<Integer> ofInt(String key) {
    return splitInt(config.value(key));
  }

  @Override
  public Set<Integer> ofInt(String key, int... defaultValues) {
    final String val = config.value(key);
    return val == null ? intDefaults(defaultValues) : splitInt(val);
  }

  private static Set<Integer> intDefaults(int[] defaultValues) {
    final Set<Integer> ints = new LinkedHashSet<>();
    for (final int defaultVal : defaultValues) {
      ints.add(defaultVal);
    }
    return ints;
  }

  @Override
  public Set<Long> ofLong(String key) {
    return splitLong(config.value(key));
  }

  @Override
  public Set<Long> ofLong(String key, long... defaultValues) {
    final String val = config.value(key);
    return val == null ? longDefaults(defaultValues) : splitLong(val);
  }

  private static Set<Long> longDefaults(long[] defaultValues) {
    final Set<Long> ints = new LinkedHashSet<>();
    for (final long defaultVal : defaultValues) {
      ints.add(defaultVal);
    }
    return ints;
  }

  @Override
  public <T> Set<T> ofType(String key, Function<String, T> function) {
    final String val = config.value(key);
    try {
      return splitAs(val, function);
    } catch (final Exception e) {
      throw new IllegalStateException("Failed to convert key: " + key + " with the provided function", e);
    }
  }

  Set<String> split(String allValues) {
    return stringDefaults(allValues.split(","));
  }

  Set<Integer> splitInt(String allValues) {
    return splitAs(allValues, Integer::parseInt);
  }

  Set<Long> splitLong(String allValues) {
    return splitAs(allValues, Long::parseLong);
  }

  <T> Set<T> splitAs(String allValues, Function<String, T> function) {
    if (allValues == null) {
      return Collections.emptySet();
    }
    final Set<T> set = new LinkedHashSet<>();
    for (final var value : allValues.split(",")) {
      set.add(function.apply(value));
    }
    return set;
  }
}
