package io.avaje.config;

import java.io.InputStream;
import java.util.Map;

import org.jspecify.annotations.NullMarked;

/**
 * Load Yaml config into a flattened map.
 */
@NullMarked
interface YamlLoader extends ConfigParser {

  @Override
  default String[] supportedExtensions() {
    return new String[]{"yml", "yaml"};
  }

  @Override
  Map<String, String> load(InputStream is);
}
