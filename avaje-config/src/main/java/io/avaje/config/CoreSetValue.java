package io.avaje.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

final class CoreSetValue implements Configuration.SetValue {

  private final CoreConfiguration config;

  CoreSetValue(CoreConfiguration config) {
    this.config = config;
  }

  @Override
  public Set<String> of(String key) {
    final String val = config.value(key);
    if (val == null) {
      return Collections.emptySet();
    }
    return split(val);
  }

  @Override
  public Set<String> of(String key, String... defaultValues) {
    final String val = config.value(key);
    if (val == null) {
      final Set<String> values = new LinkedHashSet<>();
      Collections.addAll(values, defaultValues);
      return values;
    }
    return split(val);
  }

  @Override
  public Set<Integer> ofInt(String key) {
    final String val = config.value(key);
    if (val == null) {
      return Collections.emptySet();
    }
    return splitInt(val);
  }

  @Override
  public Set<Integer> ofInt(String key, int... defaultValues) {
    final String val = config.value(key);
    if (val == null) {
      final Set<Integer> ints = new LinkedHashSet<>();
      for (final int defaultVal : defaultValues) {
        ints.add(defaultVal);
      }
      return ints;
    }
    return splitInt(val);
  }

  @Override
  public Set<Long> ofLong(String key) {
    final String val = config.value(key);
    if (val == null) {
      return Collections.emptySet();
    }
    return splitLong(val);
  }

  @Override
  public Set<Long> ofLong(String key, long... defaultValues) {
    final String val = config.value(key);
    if (val == null) {
      final Set<Long> ints = new LinkedHashSet<>();
      for (final long defaultVal : defaultValues) {
        ints.add(defaultVal);
      }
      return ints;
    }
    return splitLong(val);
  }

  @Override
  public <T> Set<T> ofType(String key, Function<String, T> function) {
    final String val = config.value(key);
    try {
      return splitAs(val, function);
    } catch (final Exception e) {
      throw new IllegalStateException(
          "Failed to convert key: " + key + " with the provided function", e);
    }
  }

  Set<String> split(String allValues) {
    final Set<String> set = new LinkedHashSet<>();
    Collections.addAll(set, allValues.split(","));
    return set;
  }

  Set<Integer> splitInt(String allValues) {
    return splitAs(allValues, Integer::parseInt);
  }

  Set<Long> splitLong(String allValues) {
    return splitAs(allValues, Long::parseLong);
  }

  <T> Set<T> splitAs(String allValues, Function<String, T> function) {

    final Set<T> set = new LinkedHashSet<>();
    for (final var value : allValues.split(",")) {

      set.add(function.apply(value));
    }

    return set;
  }
}
