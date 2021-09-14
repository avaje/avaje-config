package io.avaje.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class CoreSetValue implements Configuration.SetValue {

  private final CoreConfiguration config;

  CoreSetValue(CoreConfiguration config) {
    this.config = config;
  }

  @Override
  public Set<String> of(String key) {
    final String val = config.get(key, null);
    if (val == null) {
      return Collections.emptySet();
    }
    return split(val);
  }

  @Override
  public Set<String> of(String key, String... defaultValues) {
    final String val = config.get(key, null);
    if (val == null) {
      Set<String> values = new LinkedHashSet<>();
      Collections.addAll(values, defaultValues);
      return values;
    }
    return split(val);
  }

  @Override
  public Set<Integer> ofInt(String key) {
    final String val = config.get(key, null);
    if (val == null) {
      return Collections.emptySet();
    }
    return splitInt(val);
  }

  @Override
  public Set<Integer> ofInt(String key, int... defaultValues) {
    final String val = config.get(key, null);
    if (val == null) {
      Set<Integer> ints = new LinkedHashSet<>();
      for (int defaultVal : defaultValues) {
        ints.add(defaultVal);
      }
      return ints;
    }
    return splitInt(val);
  }

  @Override
  public Set<Long> ofLong(String key) {
    final String val = config.get(key, null);
    if (val == null) {
      return Collections.emptySet();
    }
    return splitLong(val);
  }

  @Override
  public Set<Long> ofLong(String key, long... defaultValues) {
    final String val = config.get(key, null);
    if (val == null) {
      Set<Long> ints = new LinkedHashSet<>();
      for (long defaultVal : defaultValues) {
        ints.add(defaultVal);
      }
      return ints;
    }
    return splitLong(val);
  }

  Set<String> split(String allValues) {
    final Set<String> set = new LinkedHashSet<>();
    Collections.addAll(set, allValues.split(","));
    return set;
  }

  Set<Integer> splitInt(String allValues) {
    return split(allValues).stream()
      .map(Integer::parseInt)
      .collect(Collectors.toSet());
  }

  Set<Long> splitLong(String allValues) {
    return split(allValues).stream()
      .map(Long::parseLong)
      .collect(Collectors.toSet());
  }
}
