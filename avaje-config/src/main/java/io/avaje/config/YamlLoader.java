package io.avaje.config;

import io.avaje.lang.NonNullApi;

import java.io.InputStream;
import java.util.Map;

/**
 * Load Yaml config into a flattened map.
 */
@NonNullApi
interface YamlLoader extends ConfigParser {

  @Override
  default String[] supportedExtensions() {
    return new String[]{"yml", "yaml"};
  }

  @Override
  Map<String, String> load(InputStream is);
}
