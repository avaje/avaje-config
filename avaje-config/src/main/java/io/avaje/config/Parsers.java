package io.avaje.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public Set<Map.Entry<String, ConfigParser>> entrySet() {
    return parserMap.entrySet();
  }

  public ConfigParser get(String extension) {
    return parserMap.get(extension.toLowerCase());
  }

  public boolean supportsExtension(String extension) {
    return parserMap.containsKey(extension.toLowerCase());
  }

  public Set<String> supportedExtensions() {
    return parserMap.keySet();
  }
}
