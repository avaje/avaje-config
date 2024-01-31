package io.avaje.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import static java.util.Objects.requireNonNull;

final class CoreConfigurationBuilder implements Configuration.Builder {

  private final Map<String, String> sourceMap = new LinkedHashMap<>();
  private ModificationEventRunner eventRunner;
  private ConfigurationLog configurationLog;
  private ResourceLoader resourceLoader;

  private boolean includeResourceLoading;
  private InitialLoader initialLoader;

  @Override
  public Configuration.Builder eventRunner(ModificationEventRunner eventRunner) {
    this.eventRunner = eventRunner;
    return this;
  }

  @Override
  public Configuration.Builder log(ConfigurationLog configurationLog) {
    this.configurationLog = configurationLog;
    return this;
  }

  @Override
  public Configuration.Builder resourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
    return this;
  }

  @Override
  public Configuration.Builder put(String key, String value) {
    requireNonNull(key);
    requireNonNull(value);
    sourceMap.put(key, value);
    return this;
  }

  @Override
  public Configuration.Builder putAll(Map<String, ?> source) {
    requireNonNull(source);
    source.forEach((key, value) -> {
      if (key != null && value != null) {
        put(key, value.toString());
      }
    });
    return this;
  }

  @Override
  public Configuration.Builder putAll(Properties source) {
    requireNonNull(source);
    source.forEach((key, value) -> {
      if (key != null && value != null) {
        put(key.toString(), value.toString());
      }
    });
    return this;
  }

  @Override
  public Configuration.Builder includeResourceLoading() {
    this.includeResourceLoading = true;
    return this;
  }

  @Override
  public Configuration build() {
    final var runner = initRunner();
    final var log = initLog();
    final var parsers = new Parsers();
    if (includeResourceLoading) {
      log.preInitialisation();
      initialLoader = new InitialLoader(parsers, log, initResourceLoader());
    }
    return new CoreConfiguration(parsers, runner, log, initEntries()).postLoad(initialLoader);
  }

  private CoreEntry.CoreMap initEntries() {
    final var entries = initEntryMap();
    sourceMap.forEach((key, value) -> entries.put(key, value, "initial"));
    return CoreExpressionEval.evalFor(entries);
  }

  private CoreEntry.CoreMap initEntryMap() {
    return initialLoader == null ? CoreEntry.newMap() : initialLoader.load();
  }

  private ResourceLoader initResourceLoader() {
    if (resourceLoader == null) {
      resourceLoader = ServiceLoader.load(ResourceLoader.class)
        .findFirst()
        .orElseGet(DefaultResourceLoader::new);
    }
    return resourceLoader;
  }

  private ConfigurationLog initLog() {
    if (configurationLog == null) {
      configurationLog = ServiceLoader.load(ConfigurationLog.class)
        .findFirst()
        .orElseGet(DefaultConfigurationLog::new);
    }
    return configurationLog;
  }

  private ModificationEventRunner initRunner() {
    if (eventRunner == null) {
      eventRunner = ServiceLoader.load(ModificationEventRunner.class)
        .findFirst()
        .orElseGet(CoreConfiguration.ForegroundEventRunner::new);
    }
    return eventRunner;
  }
}
