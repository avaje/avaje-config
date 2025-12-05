package io.avaje.config;

import java.util.Collections;
import java.util.List;

final class CoreComponents {

  private final ModificationEventRunner runner;
  private final ConfigurationLog log;
  private final Parsers parsers;
  private final List<ConfigurationSource> sources;
  private final List<ConfigurationPlugin> plugins;
  private final List<ConfigurationFallbacks> fallbacks;

  CoreComponents(
    ModificationEventRunner runner,
    ConfigurationLog log,
    Parsers parsers,
    List<ConfigurationSource> sources,
    List<ConfigurationPlugin> plugins,
    List<ConfigurationFallbacks> fallbacks) {
    this.runner = runner;
    this.log = log;
    this.parsers = parsers;
    this.sources = sources;
    this.plugins = plugins;
    this.fallbacks = fallbacks;
  }

  /** For testing only */
  CoreComponents() {
    this.runner = new CoreConfiguration.ForegroundEventRunner();
    this.log = new DefaultConfigurationLog();
    this.parsers = new Parsers(Collections.emptyList());
    this.sources = Collections.emptyList();
    this.plugins = Collections.emptyList();
    this.fallbacks = List.of(new DefaultFallbacks());
  }

  Parsers parsers() {
    return parsers;
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

  List<ConfigurationFallbacks> fallbacks() {
    return fallbacks;
  }

}
