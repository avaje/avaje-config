package io.avaje.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
      List<Integer> ints = new ArrayList<>(defaultValues.length);
      for (int defaultVal : defaultValues) {
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
      List<Long> ints = new ArrayList<>(defaultValues.length);
      for (long defaultVal : defaultValues) {
        ints.add(defaultVal);
      }
      return ints;
    }
    return splitLong(val);
  }

  List<String> split(String allValues) {
    return Arrays.asList(allValues.split(","));
  }

  List<Integer> splitInt(String allValues) {
    final List<String> values = split(allValues);
    final List<Integer> ints = new ArrayList<>(values.size());
    for (String val : values) {
      ints.add(Integer.parseInt(val.trim()));
    }
    return ints;
  }


  List<Long> splitLong(String allValues) {
    final List<String> values = split(allValues);
    final List<Long> longs = new ArrayList<>(values.size());
    for (String val : values) {
      longs.add(Long.parseLong(val.trim()));
    }
    return longs;
  }
}
