package io.avaje.config;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Holds the non-properties ConfigParsers.
 */
final class Parsers {

  private final Map<String, ConfigParser> parserMap = new HashMap<>();

  Parsers() {
    parserMap.put("properties", new PropertiesParser());
    if (!"true".equals(System.getProperty("skipYaml"))) {
      initYamlParser();
    }
    if (!"true".equals(System.getProperty("skipCustomParsing"))) {
      initParsers();
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

  private void initParsers() {
    ServiceLoader.load(ConfigParser.class).forEach(p -> {
      for (var ext : p.supportedExtensions()) {
        parserMap.put(ext, p);
      }
    });
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
    return parserMap.get(extension);
  }

  /**
   * Return true if the extension has a matching parser.
   */
  boolean supportsExtension(String extension) {
    return parserMap.containsKey(extension);
  }

  /**
   * Return the set of supported extensions.
   */
  Set<String> supportedExtensions() {
    return parserMap.keySet();
  }
}
