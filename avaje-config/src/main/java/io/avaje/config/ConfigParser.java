package io.avaje.config;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import org.jspecify.annotations.NullMarked;

/**
 * Load a config file into a flattened map.
 */
@NullMarked
public interface ConfigParser extends ConfigExtension {

  /**
   * File extensions Supported by this parser
   */
  String[] supportedExtensions();

  /**
   * Parse content into key value pairs.
   *
   * @param reader configuration contents
   * @return Key-Value pairs of all the configs
   */
  Map<String, String> load(Reader reader);

  /**
   * Parse content into key value pairs.
   *
   * @param is configuration contents
   * @return Key-Value pairs of all the configs
   */
  Map<String, String> load(InputStream is);
}
