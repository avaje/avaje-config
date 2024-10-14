package io.avaje.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

public interface URILoadContext {

  Optional<ConfigParser> configParser(String extension);

  Optional<String> getProperty(String key);

  default Map<String, List<String>> splitQueryParams(URI uri) {
    final Map<String, List<String>> queryPairs = new LinkedHashMap<>();
    final String[] pairs = uri.getQuery().split("&");
    for (String pair : pairs) {
      final int idx = pair.indexOf("=");
      final String key =
          idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;

      final String value =
          idx > 0 && pair.length() > idx + 1
              ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
              : null;
      queryPairs.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }
    return queryPairs;
  }
}

class DURILoadContext implements URILoadContext {

  Map<String, ConfigParser> parsersMap;

  Function<String, @Nullable String> getProperty;

  DURILoadContext(
      Map<String, ConfigParser> parsersMap,
      Function<String, @Nullable String> getPropertyFunction) {
    this.parsersMap = parsersMap;
    this.getProperty = getPropertyFunction;
  }

  @Override
  public Optional<ConfigParser> configParser(String extension) {

    return Optional.ofNullable(parsersMap.get(extension));
  }

  @Override
  public Optional<String> getProperty(String key) {

    return Optional.ofNullable(getProperty.apply(key));
  }
}