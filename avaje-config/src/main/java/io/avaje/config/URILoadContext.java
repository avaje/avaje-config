package io.avaje.config;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface URILoadContext {

  Optional<ConfigParser> parser(String extension);

  Optional<String> getProperty(String key);
}

class DURILoadContext implements URILoadContext {

  Map<String, ConfigParser> parsersMap;

  UnaryOperator<String> getProperty;

  DURILoadContext(Map<String, ConfigParser> parsersMap, UnaryOperator<String> getPropertyFunction) {
    this.parsersMap = parsersMap;
    this.getProperty = getPropertyFunction;
  }

  @Override
  public Optional<ConfigParser> parser(String extension) {

    return Optional.ofNullable(parsersMap.get(extension));
  }

  @Override
  public Optional<String> getProperty(String key) {

    return Optional.ofNullable(getProperty.apply(key));
  }
}
