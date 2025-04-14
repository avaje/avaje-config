package io.avaje.config;

import java.util.List;

final class CoreComponents {

  private final ModificationEventRunner runner;
  private final ConfigurationLog log;
  private final Parsers parsers;
  private final List<URIConfigLoader> uriLoaders;
  private final List<ConfigurationSource> sources;
  private final List<ConfigurationPlugin> plugins;

  CoreComponents(
      ModificationEventRunner runner,
      ConfigurationLog log,
      Parsers parsers,
      List<URIConfigLoader> uriLoaders,
      List<ConfigurationSource> sources,
      List<ConfigurationPlugin> plugins) {
    this.runner = runner;
    this.log = log;
    this.parsers = parsers;
    this.uriLoaders = uriLoaders;
    this.sources = sources;
    this.plugins = plugins;
  }

  /** For testing only */
  CoreComponents() {
    this.runner = new CoreConfiguration.ForegroundEventRunner();
    this.log = new DefaultConfigurationLog();
    this.parsers = ConfigServiceLoader.get().parsers();
    this.uriLoaders = ConfigServiceLoader.get().uriLoaders();
    this.sources = List.of();
    this.plugins = List.of();
  }

  Parsers parsers() {
    return parsers;
  }

  public List<URIConfigLoader> uriLoaders() {
    return uriLoaders;
  }

  ConfigurationLog log() {
    return log;
  }

  ModificationEventRunner runner() {
    return runner;
  }

  List<ConfigurationSource> sources() {
    return sources;
  }

  List<ConfigurationPlugin> plugins() {
    return plugins;
  }
}
