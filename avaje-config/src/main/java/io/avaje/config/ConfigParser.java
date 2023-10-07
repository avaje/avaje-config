package io.avaje.config;

import java.io.InputStream;
import java.util.Map;

public interface ConfigParser {

  /** The File Extension Types Supported by this parser */
  String[] supportedExtensions();

  /** Load the yaml into a flat map of key value pairs. */
  Map<String, String> load(InputStream is);
}
