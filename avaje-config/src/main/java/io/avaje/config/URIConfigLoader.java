package io.avaje.config;

import java.net.URI;
import java.util.Map;

/** Custom URI configuration parser for reading load.properties properties that use a URI. */
public interface URIConfigLoader extends ConfigExtension {

  /** redact any sensitive information in the URI when displayed by logging */
  default String redact(URI uri) {

    return uri.toString();
  }

  /** URI Scheme Supported by this loader */
  String[] supportedSchemes();

  /**
   * @param uri uri from which to load data
   * @param parsers map of {@link ConfigParser} available to assist in parsing data, keyed by {@link
   *     ConfigParser#supportedExtensions()}
   * @return key/value map of loaded properties
   */
  Map<String, String> load(URI uri, URILoadContext ctx);
}
