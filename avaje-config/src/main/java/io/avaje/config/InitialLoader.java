package io.avaje.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import io.avaje.config.CoreEntry.CoreMap;
import io.avaje.lang.Nullable;

import static io.avaje.config.InitialLoader.Source.FILE;
import static io.avaje.config.InitialLoader.Source.RESOURCE;

/**
 * Loads the configuration from known/expected locations.
 * <p>
 * Defines the loading order of resources and files.
 * </p>
 */
final class InitialLoader {

  private static final Pattern SPLIT_PATHS = Pattern.compile("[\\s,;]+");

  /**
   * Return the Expression evaluator using the given properties.
   */
  public static Configuration.ExpressionEval evalFor(Properties properties) {
    return new CoreExpressionEval(properties);
  }

  enum Source {
    RESOURCE,
    FILE
  }

  private final ConfigurationLog log;
  private final InitialLoadContext loadContext;
  private final Set<String> profileResourceLoaded = new HashSet<>();
  private YamlLoader yamlLoader;

  InitialLoader(ConfigurationLog log, ResourceLoader resourceLoader) {
    this.log = log;
    this.loadContext = new InitialLoadContext(log, resourceLoader);
    initYamlLoader();
  }

  Set<String> loadedFrom() {
    return loadContext.loadedFrom();
  }

  /**
   * Provides properties by reading known locations.
   * <p>
   * <h3>Main configuration</h3>
   * <p>
   * <p>Firstly loads from main resources</p>
   * <pre>
   *   - application.properties
   *   - application.yaml
   * </pre>
   * <p>
   * <p>Then loads from local files</p>
   * <pre>
   *   - application.properties
   *   - application.yaml
   * </pre>
   * <p>
   * <p>Then loads from environment variable <em>PROPS_FILE</em></p>
   * <p>Then loads from system property <em>props.file</em></p>
   * <p>Then loads from <em>load.properties</em></p>
   * <p>
   * <h3>Test configuration</h3>
   * <p>
   * Once the main configuration is read it will try to read common test configuration.
   * This will only be successful if the test resources are available (i.e. running tests).
   * </p>
   * <p>Loads from test resources</p>
   * <pre>
   *   - application-test.properties
   *   - application-test.yaml
   * </pre>
   */
  CoreMap load() {
    loadEnvironmentVars();
    loadLocalFiles();
    return eval();
  }

  void initWatcher(CoreConfiguration configuration) {
    if (configuration.getBool("config.watch.enabled", false)) {
      configuration.setWatcher(new FileWatch(configuration, loadContext.loadedFiles(), yamlLoader));
    }
  }

  private void initYamlLoader() {
    if (!"true".equals(System.getProperty("skipYaml"))) {
      try {
        Class.forName("org.yaml.snakeyaml.Yaml");
        yamlLoader = new YamlLoaderSnake();
      } catch (ClassNotFoundException e) {
        yamlLoader = new YamlLoaderSimple();
      }
    }
  }

  void loadEnvironmentVars() {
    loadContext.loadEnvironmentVars();
  }

  /**
   * Load from local files and resources.
   */
  void loadLocalFiles() {
    loadMain(RESOURCE);
    loadViaProfiles(RESOURCE);
    // external file configuration overrides the resources configuration
    loadMain(FILE);
    // load additional profile RESOURCE(s) if added via loadMain()
    loadViaProfiles(RESOURCE);
    loadViaProfiles(FILE);
    loadViaSystemProperty();
    loadViaIndirection();
    // test configuration (if found) overrides main configuration
    // we should only find these resources when running tests
    if (!loadTest()) {
      loadLocalDev();
    }
    loadViaCommandLineArgs();
  }

  private void loadViaCommandLineArgs() {
    final String rawArgs = System.getProperty("sun.java.command");
    if (rawArgs != null) {
      loadViaCommandLine(rawArgs.split(" "));
    }
  }

  void loadViaCommandLine(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.startsWith("-P") || arg.startsWith("-p")) {
        if (arg.length() == 2 && i < args.length - 1) {
          // next argument expected to be a properties file paths
          i++;
          loadCommandLineArg(args[i]);
        } else {
          // no space between -P and properties file paths
          loadCommandLineArg(arg.substring(2));
        }
      }
    }
  }

  private void loadCommandLineArg(String arg) {
    if (isValidExtension(arg)) {
      loadViaPaths(arg);
    }
  }

  private boolean isValidExtension(String arg) {
    return arg.endsWith(".yaml") || arg.endsWith(".yml") || arg.endsWith(".properties");
  }

  /**
   * Provides a way to override properties when running via main() locally.
   */
  private void loadLocalDev() {
    File localDev = new File(System.getProperty("user.home"), ".localdev");
    if (localDev.exists()) {
      final String appName = loadContext.getAppName();
      if (appName != null) {
        final String prefix = localDev.getAbsolutePath() + File.separator + appName;
        load(prefix, FILE);
      }
    }
  }

  /**
   * Load test configuration.
   *
   * @return true if test properties have been loaded.
   */
  private boolean loadTest() {
    if (Boolean.getBoolean("suppressTestResource")) {
      return false;
    }
    int before = loadContext.size();
    load("application-test", RESOURCE);
    if (loadProperties("test-ebean.properties", RESOURCE)) {
      log.log(Level.WARNING, "Loading properties from test-ebean.properties is deprecated. Please migrate to application-test.yaml or application-test.properties instead.");
    }
    return loadContext.size() > before;
  }

  /**
   * Load configuration defined by a <em>load.properties</em> entry in properties file.
   */
  private void loadViaIndirection() {
    String paths = loadContext.indirectLocation();
    if (paths != null) {
      loadViaPaths(paths);
    }
  }

  @Nullable
  private String[] profiles() {
    final String paths = loadContext.profiles();
    return paths == null ? null : splitPaths(paths);
  }

  /**
   * Load configuration defined by a <em>config.profiles</em> property.
   */
  private void loadViaProfiles(Source source) {
    final var profiles = profiles();
    if (profiles != null) {
      for (final String path : profiles) {
        final var profile = loadContext.eval(path);
        if (source != RESOURCE || !profileResourceLoaded.contains(profile)) {
          if (load("application-" + profile, source)) {
            profileResourceLoaded.add(profile);
          }
        }
      }
    }
  }

  private void loadViaPaths(String paths) {
    for (String path : splitPaths(paths)) {
      loadWithExtensionCheck(loadContext.eval(path));
    }
  }

  int size() {
    return loadContext.size();
  }

  String[] splitPaths(String location) {
    return SPLIT_PATHS.split(location);
  }

  /**
   * Load the main configuration for the given source.
   */
  private void loadMain(Source source) {
    load("application", source);
    if (loadProperties("ebean.properties", source)) {
      log.log(Level.WARNING, "Loading properties from ebean.properties is deprecated. Please migrate to use application.yaml or application.properties instead.");
    }
  }

  private void loadViaSystemProperty() {
    String fileName = System.getenv("PROPS_FILE");
    if (fileName == null) {
      fileName = System.getProperty("props.file");
      if (fileName != null) {
        if (!loadWithExtensionCheck(fileName)) {
          log.log(Level.WARNING, "Unable to find file {0} to load properties", fileName);
        }
      }
    }
  }

  boolean loadWithExtensionCheck(String fileName) {
    if (fileName.endsWith("yaml") || fileName.endsWith("yml")) {
      return loadYamlPath(fileName, RESOURCE) | loadYamlPath(fileName, FILE);
    } else if (fileName.endsWith("properties")) {
      return loadProperties(fileName, RESOURCE) | loadProperties(fileName, FILE);
    } else {
      throw new IllegalArgumentException("Expecting only yaml or properties file but got [" + fileName + "]");
    }
  }

  /**
   * Evaluate all the configuration entries and return as properties.
   */
  CoreMap eval() {
    return loadContext.evalAll();
  }

  /**
   * Attempt to load a properties and yaml/yml file.
   * Return true if at least one was loaded.
   */
  boolean load(String resourcePath, Source source) {
    final boolean props = loadProperties(resourcePath + ".properties", source);
    final boolean yaml = loadYaml(resourcePath, source);
    return props || yaml;
  }

  /**
   * Load YAML first and if not found load YML.
   */
  private boolean loadYaml(String resourcePath, Source source) {
    if (loadYamlPath(resourcePath + ".yaml", source)) {
      return true;
    } else {
      return loadYamlPath(resourcePath + ".yml", source);
    }
  }

  boolean loadYamlPath(String resourcePath, Source source) {
    if (yamlLoader != null) {
      try {
        try (InputStream is = resource(resourcePath, source)) {
          if (is != null) {
            yamlLoader.load(is).forEach((k, v) -> loadContext.put(k, v, (source == RESOURCE ? "resource:" : "file:") + resourcePath));
            return true;
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Error loading yaml properties - " + resourcePath, e);
      }
    }
    return false;
  }

  boolean loadProperties(String resourcePath, Source source) {
    try {
      try (InputStream is = resource(resourcePath, source)) {
        if (is != null) {
          loadProperties(is, (source == RESOURCE ? "resource:" : "file") + resourcePath);
          return true;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Error loading properties - " + resourcePath, e);
    }
    return false;
  }

  private InputStream resource(String resourcePath, Source source) {
    return loadContext.resource(resourcePath, source);
  }

  private void loadProperties(InputStream is, String source) throws IOException {
    Properties properties = new Properties();
    properties.load(is);
    put(properties, source);
  }

  private void put(Properties properties, String source) {
    Enumeration<?> enumeration = properties.propertyNames();
    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      String val = properties.getProperty(key);
      loadContext.put(key, val, source);
    }
  }

}
