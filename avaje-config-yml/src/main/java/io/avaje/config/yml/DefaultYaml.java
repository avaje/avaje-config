package io.avaje.config.yml;

import io.avaje.config.YamlLoader;
import io.avaje.config.YamlLoaderProvider;

public final class DefaultYaml implements YamlLoaderProvider {

  @Override
  public YamlLoader getLoader() {
    try {
      Class.forName("org.yaml.snakeyaml.Yaml");
      return new YamlLoaderSnake();
    } catch (final ClassNotFoundException e) {
      return new YamlLoaderSimple();
    }
  }
}
