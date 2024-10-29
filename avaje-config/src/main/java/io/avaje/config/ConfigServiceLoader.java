package io.avaje.config;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Load all the avaje-config extensions via ServiceLoader using the single common ConfigExtension
 * interface.
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
  private final List<URIConfigLoader> uriLoaders;
  private final ConfigParsers parsers;

  ConfigServiceLoader() {
    ModificationEventRunner spiEventRunner = null;
    ConfigurationLog spiLog = null;
    ResourceLoader spiResourceLoader = null;
    List<ConfigParser> otherParsers = new ArrayList<>();
    List<URIConfigLoader> loaders = new ArrayList<>();

    for (var spi : ServiceLoader.load(ConfigExtension.class)) {
      if (spi instanceof ConfigurationSource) {
        sources.add((ConfigurationSource) spi);
      } else if (spi instanceof ConfigurationPlugin) {
        plugins.add((ConfigurationPlugin) spi);
      } else if (spi instanceof ConfigParser) {
        otherParsers.add((ConfigParser) spi);
      } else if (spi instanceof URIConfigLoader) {
        loaders.add((URIConfigLoader) spi);
      } else if (spi instanceof ConfigurationLog) {
        spiLog = (ConfigurationLog) spi;
      } else if (spi instanceof ResourceLoader) {
        spiResourceLoader = (ResourceLoader) spi;
      } else if (spi instanceof ModificationEventRunner) {
        spiEventRunner = (ModificationEventRunner) spi;
      }
    }

    this.log = spiLog == null ? new DefaultConfigurationLog() : spiLog;
    this.resourceLoader = spiResourceLoader == null ? new DefaultResourceLoader() : spiResourceLoader;
    this.eventRunner =
        spiEventRunner == null ? new CoreConfiguration.ForegroundEventRunner() : spiEventRunner;
    this.parsers = new Parsers(otherParsers);
    this.uriLoaders = loaders;
  }

  ConfigParsers parsers() {
    return parsers;
  }

  public List<URIConfigLoader> uriLoaders() {
    return uriLoaders;
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
