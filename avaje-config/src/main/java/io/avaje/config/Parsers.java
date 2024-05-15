package io.avaje.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds the non-properties ConfigParsers.
 */
final class Parsers {

  private final Map<String, ConfigParser> parserMap = new HashMap<>();

  Parsers(List<ConfigParser> otherParsers) {
    parserMap.put("properties", new PropertiesParser());
    if (!"true".equals(System.getProperty("skipYaml"))) {
      initYamlParser();
    }
    if (!"true".equals(System.getProperty("skipCustomParsing"))) {
      initParsers(otherParsers);
    }
  }

  private void initYamlParser() {
    YamlLoader yamlLoader;
    try {
      Class.forName("org.yaml.snakeyaml.Yaml");
      yamlLoader = new YamlLoaderSnake();
    } catch (ClassNotFoundException e) {
      yamlLoader = new YamlLoaderSimple();
    }
    parserMap.put("yml", yamlLoader);
    parserMap.put("yaml", yamlLoader);
  }

  private void initParsers(List<ConfigParser> otherParsers) {
    for (ConfigParser parser : otherParsers) {
      for (var ext : parser.supportedExtensions()) {
        parserMap.put(ext, parser);
      }
    }
  }

  /**
   * Return the extension ConfigParser pairs.
   */
  Set<Map.Entry<String, ConfigParser>> entrySet() {
    return parserMap.entrySet();
  }

  /**
   * Return the ConfigParser for the given extension.
   */
  ConfigParser get(String extension) {
    return parserMap.get(extension.toLowerCase());
  }

  /**
   * Return true if the extension has a matching parser.
   */
  boolean supportsExtension(String extension) {
    return parserMap.containsKey(extension.toLowerCase());
  }

  /**
   * Return the set of supported extensions.
   */
  Set<String> supportedExtensions() {
    return parserMap.keySet();
  }
}
