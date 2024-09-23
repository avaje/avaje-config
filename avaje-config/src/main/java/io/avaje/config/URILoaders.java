package io.avaje.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/** Holds the non-properties ConfigParsers. */
public final class URILoaders {

  private final Map<String, URIConfigLoader> parserMap = new HashMap<>();

  URILoaders(List<URIConfigLoader> loaders) {
    for (var parser : loaders) {

      parserMap.put(parser.supportedScheme(), parser);
    }
  }

  /** Return the extension ConfigParser pairs. */
  public Set<Entry<String, URIConfigLoader>> entrySet() {
    return parserMap.entrySet();
  }

  /** Return the ConfigParser for the given extension. */
  public URIConfigLoader get(String extension) {
    return parserMap.get(extension.toLowerCase());
  }

  /** Return true if the extension has a matching parser. */
  public boolean supportsScheme(String extension) {
    return parserMap.containsKey(extension.toLowerCase());
  }

  /** Return the set of supported extensions. */
  public Set<String> supportedSchemes() {
    return parserMap.keySet();
  }
}
