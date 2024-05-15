package io.avaje.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Load all the services via ServiceLoader.
 */
final class ConfigServiceLoader {

  private static final ConfigServiceLoader INSTANCE = new ConfigServiceLoader();

  static ConfigServiceLoader get() {
    return INSTANCE;
  }

  private final ConfigurationLog log;
  private final ResourceLoader resourceLoader;
  private final ModificationEventRunner eventRunner;
  private final List<ConfigurationSource> sources = new ArrayList<>();
  private final List<ConfigurationPlugin> plugins = new ArrayList<>();
  private final Parsers parsers;

  ConfigServiceLoader() {
    ModificationEventRunner _eventRunner = null;
    ConfigurationLog _log = null;
    ResourceLoader _resourceLoader = null;
    List<ConfigParser> otherParsers = new ArrayList<>();

    for (var spi : ServiceLoader.load(ConfigExtension.class)) {
      if (spi instanceof ConfigurationSource) {
        sources.add((ConfigurationSource) spi);
      } else if (spi instanceof ConfigurationPlugin) {
        plugins.add((ConfigurationPlugin) spi);
      } else if (spi instanceof ConfigParser) {
        otherParsers.add((ConfigParser) spi);
      } else if (spi instanceof ConfigurationLog) {
        _log = (ConfigurationLog) spi;
      } else if (spi instanceof ResourceLoader) {
        _resourceLoader = (ResourceLoader) spi;
      } else if (spi instanceof ModificationEventRunner) {
        _eventRunner = (ModificationEventRunner) spi;
      }
    }

    this.log = _log == null ? new DefaultConfigurationLog() : _log;
    this.resourceLoader = _resourceLoader == null ? new DefaultResourceLoader() : _resourceLoader;
    this.eventRunner = _eventRunner == null ? new CoreConfiguration.ForegroundEventRunner() : _eventRunner;
    this.parsers = new Parsers(otherParsers);
  }

  Parsers parsers() {
    return parsers;
  }

  ConfigurationLog log() {
    return log;
  }

  ResourceLoader resourceLoader() {
    return resourceLoader;
  }

  ModificationEventRunner eventRunner() {
    return eventRunner;
  }

  List<ConfigurationSource> sources() {
    return sources;
  }

  List<ConfigurationPlugin> plugins() {
    return plugins;
  }
}
