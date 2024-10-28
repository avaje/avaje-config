package io.avaje.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

final class CoreComponents {

  private final ModificationEventRunner runner;
  private final ConfigurationLog log;
  private final Map<String, ConfigParser> parsers;
  private final List<ConfigurationSource> sources;
  private final List<ConfigurationPlugin> plugins;

  CoreComponents(ModificationEventRunner runner, ConfigurationLog log, Map<String, ConfigParser> parsers, List<ConfigurationSource> sources, List<ConfigurationPlugin> plugins) {
    this.runner = runner;
    this.log = log;
    this.parsers = parsers;
    this.sources = sources;
    this.plugins = plugins;
  }

  /** For testing only */
  CoreComponents() {
    this.runner = new CoreConfiguration.ForegroundEventRunner();
    this.log = new DefaultConfigurationLog();
    this.parsers = ConfigServiceLoader.get().parsers();
    this.sources = Collections.emptyList();
    this.plugins = Collections.emptyList();
  }

  Map<String, ConfigParser> parsers() {
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
}
