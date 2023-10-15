package io.avaje.config;

import java.io.InputStream;
import java.util.Map;

/**
 * Load a config file into a flattened map.
 */
public interface ConfigParser {

  /**
   * File extensions Supported by this parser
   */
  String[] supportedExtensions();

  /**
   * Loads a file into a flat map of key value pairs.
   *
   * @param is stream of file contents
   * @return Key-Value pairs of all the configs
   */
  Map<String, String> load(InputStream is);
}
