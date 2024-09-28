package io.avaje.config;

import java.net.URI;
import java.util.Map;

/** URI based source to load and update configuration based on the provided URI scheme. */
public interface URIConfigLoader extends ConfigExtension {

  /** URI Scheme Supported by this loader */
  String supportedScheme();

  /**
   * @param uri uri from which to load data
   * @param parsers config parses available to assist in parsing data
   * @return key/value map of loaded properties
   */
  Map<String, String> load(URI uri, Map<String, ConfigParser> parsers);
}
