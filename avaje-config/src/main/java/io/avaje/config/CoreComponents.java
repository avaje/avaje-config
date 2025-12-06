package io.avaje.config;

import java.util.Collections;
import java.util.List;

final class CoreComponents {

  private final ModificationEventRunner runner;
  private final ConfigurationLog log;
  private final Parsers parsers;
  private final ConfigurationFallback fallback;
  private final List<ConfigurationSource> sources;
  private final List<ConfigurationPlugin> plugins;

  CoreComponents(
          ModificationEventRunner runner,
          ConfigurationLog log,
          Parsers parsers,
          ConfigurationFallback fallback,
          List<ConfigurationSource> sources,
          List<ConfigurationPlugin> plugins) {
    this.runner = runner;
    this.log = log;
    this.parsers = parsers;
    this.sources = sources;
    this.plugins = plugins;
    this.fallback = fallback;
  }

  /** For testing only */
  CoreComponents() {
    this.runner = new CoreConfiguration.ForegroundEventRunner();
    this.log = new DefaultConfigurationLog();
    this.parsers = new Parsers(Collections.emptyList());
    this.sources = Collections.emptyList();
    this.plugins = Collections.emptyList();
    this.fallback = new DefaultFallback();
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

  ConfigurationFallback fallbacks() {
    return fallback;
  }

  List<ConfigurationSource> sources() {
    return sources;
  }

  List<ConfigurationPlugin> plugins() {
    return plugins;
  }
}
