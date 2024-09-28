package io.avaje.config;

import java.net.URI;
import java.util.Map;

/** Custom URI configuration loader. Used when a matching uri scheme is found in load.properties */
public interface URIConfigLoader extends ConfigExtension {

  /** URI Scheme Supported by this loader */
  String supportedScheme();

  /**
   * @param uri uri from which to load data
   * @param parsers map of {@link ConfigParser} available to assist in parsing data, keyed by {@link
   *     ConfigParser#supportedExtensions()}
   * @return key/value map of loaded properties
   */
  Map<String, String> load(URI uri, Map<String, ConfigParser> parsers);
}
