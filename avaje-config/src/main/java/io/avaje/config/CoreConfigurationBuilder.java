package io.avaje.config;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;

import org.jspecify.annotations.NullMarked;

@NullMarked
final class CoreConfigurationBuilder implements Configuration.Builder {

  private final CoreEntry.CoreMap sourceMap = CoreEntry.newMap();
  private final ConfigServiceLoader serviceLoader = ConfigServiceLoader.get();
  private final Map<String, ConfigParser> parsers = serviceLoader.parsers();
  private final URILoaders uriLoaders = serviceLoader.uriLoaders();
  private ConfigurationLog log = serviceLoader.log();
  private ResourceLoader resourceLoader = serviceLoader.resourceLoader();
  private ModificationEventRunner eventRunner = serviceLoader.eventRunner();
  private boolean includeResourceLoading;
  private InitialLoader initialLoader;

  @Override
  public Configuration.Builder eventRunner(ModificationEventRunner eventRunner) {
    this.eventRunner = requireNonNull(eventRunner);
    return this;
  }

  @Override
  public Configuration.Builder log(ConfigurationLog log) {
    this.log = requireNonNull(log);
    return this;
  }

  @Override
  public Configuration.Builder resourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = requireNonNull(resourceLoader);
    return this;
  }

  @Override
  public Configuration.Builder put(String key, String value) {
    sourceMap.put(requireNonNull(key), requireNonNull(value), "initial");
    return this;
  }

  @Override
  public Configuration.Builder putAll(Map<String, ?> source) {
    requireNonNull(source);
    source.forEach((key, value) -> {
      if (key != null && value != null) {
        sourceMap.put(key, value.toString(), "initial");
      }
    });
    return this;
  }

  @Override
  public Configuration.Builder putAll(Properties source) {
    requireNonNull(source);
    source.forEach((key, value) -> {
      if (key != null && value != null) {
        sourceMap.put(key.toString(), value.toString(), "initial");
      }
    });
    return this;
  }

  @Override
  public Configuration.Builder load(String resource) {
    final var configParser = parser(resource);
    try {
      try (var inputStream = resourceLoader.getResourceAsStream(resource)) {
        if (inputStream == null) {
          log.log(INFO, "Configuration resource:{0} not found", resource);
        } else {
          var source = "resource:" + resource;
          configParser.load(inputStream).forEach((k, v) -> sourceMap.put(k, v, source));
          log.log(DEBUG, "loaded {0}", source);
        }
        return this;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Configuration.Builder load(File file) {
    if (!file.exists()) {
      log.log(INFO, "Configuration file:{0} not found", file);
      return this;
    }
    final var configParser = parser(file.getName());
    try {
      try (var reader = new FileReader(file)) {
        var source = "file:" + file.getName();
        configParser.load(reader).forEach((k, v) -> sourceMap.put(k, v, source));
        log.log(DEBUG, "loaded {0}", source);
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
    var components =
        new CoreComponents(
            eventRunner,
            log,
            parsers,
            uriLoaders,
            serviceLoader.sources(),
            serviceLoader.plugins());
    if (includeResourceLoading) {
      log.preInitialisation();
      initialLoader = new InitialLoader(components, resourceLoader);
    }
    return new CoreConfiguration(components, initEntries()).postLoad(initialLoader);
  }

  private CoreEntry.CoreMap initEntries() {
    final var entries = initEntryMap();
    entries.addAll(sourceMap);
    return CoreExpressionEval.evalFor(entries);
  }

  private CoreEntry.CoreMap initEntryMap() {
    return initialLoader == null ? CoreEntry.newMap() : initialLoader.load();
  }
}
