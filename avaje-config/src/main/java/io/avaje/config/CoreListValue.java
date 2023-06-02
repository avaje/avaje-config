package io.avaje.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

final class CoreListValue implements Configuration.ListValue {

  private final CoreConfiguration config;

  public CoreListValue(CoreConfiguration config) {
    this.config = config;
  }

  @Override
  public List<String> of(String key) {
    final String val = config.value(key);
    if (val == null) {
      return Collections.emptyList();
    }
    return split(val);
  }

  @Override
  public List<String> of(String key, String... defaultValues) {
    final String val = config.value(key);
    if (val == null) {
      return Arrays.asList(defaultValues);
    }
    return split(val);
  }

  @Override
  public List<Integer> ofInt(String key) {
    final String val = config.value(key);
    if (val == null) {
      return Collections.emptyList();
    }
    return splitInt(val);
  }

  @Override
  public List<Integer> ofInt(String key, int... defaultValues) {
    final String val = config.value(key);
    if (val == null) {
      final List<Integer> ints = new ArrayList<>(defaultValues.length);
      for (final int defaultVal : defaultValues) {
        ints.add(defaultVal);
      }
      return ints;
    }
    return splitInt(val);
  }

  @Override
  public List<Long> ofLong(String key) {
    final String val = config.value(key);
    if (val == null) {
      return Collections.emptyList();
    }
    return splitLong(val);
  }

  @Override
  public List<Long> ofLong(String key, long... defaultValues) {
    final String val = config.value(key);
    if (val == null) {
      final List<Long> ints = new ArrayList<>(defaultValues.length);
      for (final long defaultVal : defaultValues) {
        ints.add(defaultVal);
      }
      return ints;
    }
    return splitLong(val);
  }

  @Override
  public <T> List<T> ofType(String key, Function<String, T> function) {
    final String val = config.value(key);
    try {
      return splitAs(val, function);
    } catch (final Exception e) {
      throw new IllegalStateException(
          "Failed to convert key: " + key + " with the provided function", e);
    }
  }

  List<String> split(String allValues) {
    return Arrays.asList(allValues.split(","));
  }

  List<Integer> splitInt(String allValues) {
    return splitAs(allValues, Integer::parseInt);
  }

  List<Long> splitLong(String allValues) {
    return splitAs(allValues, Long::parseLong);
  }

  <T> List<T> splitAs(String allValues, Function<String, T> function) {

    final List<T> list = new ArrayList<>();

    for (final var value : allValues.split(",")) {
      list.add(function.apply(value));
    }

    return list;
  }
}
