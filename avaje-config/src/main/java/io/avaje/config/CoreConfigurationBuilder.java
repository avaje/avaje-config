package io.avaje.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

final class CoreConfigurationBuilder implements Configuration.Builder {

  private final Parsers parsers = new Parsers();
  private final Map<String, String> sourceMap = new LinkedHashMap<>();
  private ResourceLoader resourceLoader = initialiseResourceLoader();
  private ModificationEventRunner eventRunner;
  private ConfigurationLog configurationLog;
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
  public Configuration.Builder load(String resource) {
    final var configParser = parser(resource);
    try {
      try (var inputStream = resourceLoader.getResourceAsStream(resource)) {
        putAll(configParser.load(inputStream));
        return this;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Configuration.Builder load(File file) {
    final var configParser = parser(file.getName());
    try {
      try (var reader = new FileReader(file)) {
        putAll(configParser.load(reader));
        return this;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private ConfigParser parser(String name) {
    int pos = name.lastIndexOf('.');
    if (pos == -1) {
      throw new IllegalArgumentException("Unable to determine the extension for " + name);
    }
    var extension = name.substring(pos + 1);
    ConfigParser configParser = parsers.get(extension);
    if (configParser == null) {
      throw new IllegalArgumentException("No parser registered for extension " + extension);
    }
    return configParser;
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
    final var sources = ServiceLoader.load(ConfigurationSource.class).stream()
      .map(ServiceLoader.Provider::get)
      .collect(Collectors.toList());
    final var plugins = ServiceLoader.load(ConfigurationPlugin.class).stream()
      .map(ServiceLoader.Provider::get)
      .collect(Collectors.toList());

    var components = new CoreComponents(runner, log, parsers, sources, plugins);
    if (includeResourceLoading) {
      log.preInitialisation();
      initialLoader = new InitialLoader(components, resourceLoader);
    }
    return new CoreConfiguration(components, initEntries()).postLoad(initialLoader);
  }

  private CoreEntry.CoreMap initEntries() {
    final var entries = initEntryMap();
    sourceMap.forEach((key, value) -> entries.put(key, value, "initial"));
    return CoreExpressionEval.evalFor(entries);
  }

  private CoreEntry.CoreMap initEntryMap() {
    return initialLoader == null ? CoreEntry.newMap() : initialLoader.load();
  }

  private static ResourceLoader initialiseResourceLoader() {
    return ServiceLoader.load(ResourceLoader.class)
      .findFirst()
      .orElseGet(DefaultResourceLoader::new);
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
