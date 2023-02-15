package io.avaje.config;

import java.io.InputStream;
import java.util.Map;

/**
 * Load Yaml config into a flattened map.
 */
interface YamlLoader {

  /**
   * Load the yaml into a flat map of key value pairs.
   */
  Map<String, String> load(InputStream is);

}
