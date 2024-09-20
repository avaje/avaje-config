package io.avaje.config;

import static java.util.stream.Collectors.toMap;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

final class EnvLoader implements URIConfigLoader {

  private static final Set<Entry<String, String>> envVariables = System.getenv().entrySet();

  @Override
  public String supportedScheme() {

    return "env";
  }

  @Override
  public Map<String, String> load(URI uri, ConfigParsers __) {

    return envVariables.stream()
        .filter(e -> e.getKey().contains(uri.getPath().substring(1)))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }
}
