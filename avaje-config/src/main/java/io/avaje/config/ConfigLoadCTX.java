package io.avaje.config;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface ConfigLoadCTX {
  Optional<ConfigParser> configParser(String extension);

  Optional<String> getProperty(String key);
}

class DConfigLoadCTX implements ConfigLoadCTX {

  Map<String, ConfigParser> parsersMap;

  Function<String, Optional<String>> getProperty;

  DConfigLoadCTX(
      Map<String, ConfigParser> parsersMap,
      Function<String, Optional<String>> getPropertyFunction) {
    this.parsersMap = parsersMap;
    this.getProperty = getPropertyFunction;
  }

  @Override
  public Optional<ConfigParser> configParser(String extension) {

    return Optional.ofNullable(parsersMap.get(extension));
  }

  @Override
  public Optional<String> getProperty(String key) {

    return getProperty.apply(key);
  }
}
