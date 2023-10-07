package io.avaje.config;

import java.io.InputStream;
import java.util.Map;

/** Load Yaml config into a flattened map. */
interface YamlLoader extends ConfigParser {

  @Override
  default String[] supportedExtensions() {
    return new String[] {"yml", "yaml"};
  }

  @Override
  Map<String, String> load(InputStream is);
}
