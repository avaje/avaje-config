package io.avaje.config;

import java.util.List;
import java.util.Map;

final class CoreComponents {

  private final ModificationEventRunner runner;
  private final ConfigurationLog log;
  private final Map<String, ConfigParser> parsers;
  private final Map<String, SuperConfigSource> superConfigSources;
  private final List<ConfigurationSource> sources;
  private final List<ConfigurationPlugin> plugins;

  CoreComponents(
      ModificationEventRunner runner,
      ConfigurationLog log,
      Map<String, ConfigParser> parsers,
      Map<String, SuperConfigSource> superConfigSources,
      List<ConfigurationSource> sources,
      List<ConfigurationPlugin> plugins) {
    this.runner = runner;
    this.log = log;
    this.parsers = parsers;
    this.superConfigSources = superConfigSources;
    this.sources = sources;
    this.plugins = plugins;
  }

  /** For testing only */
  CoreComponents() {
    this.runner = new CoreConfiguration.ForegroundEventRunner();
    this.log = new DefaultConfigurationLog();
    this.parsers = ConfigServiceLoader.get().parsers();
    this.superConfigSources = ConfigServiceLoader.get().superConfigSources();
    this.sources = List.of();
    this.plugins = List.of();
  }

  Map<String, ConfigParser> parsers() {
    return parsers;
  }

  public Map<String, SuperConfigSource> superConfigSources() {
    return superConfigSources;
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
