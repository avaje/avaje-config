package io.avaje.config;

import java.io.InputStream;
import java.util.Map;

public interface ConfigParser {

  default String[] supportedExtensions() {
    return new String[] {};
  }

  /** Load the yaml into a flat map of key value pairs. */
  Map<String, String> load(InputStream is);
}
