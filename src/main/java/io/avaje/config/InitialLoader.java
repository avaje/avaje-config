package io.avaje.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Pattern;

import static io.avaje.config.InitialLoader.Source.FILE;
import static io.avaje.config.InitialLoader.Source.RESOURCE;

/**
 * Loads the configuration from known/expected locations.
 * <p>
 * Defines the loading order of resources and files.
 * </p>
 */
class InitialLoader {

  private static final Logger log = LoggerFactory.getLogger(InitialLoader.class);

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

  private final InitialLoadContext loadContext = new InitialLoadContext();

  private YamlLoader yamlLoader;

  InitialLoader() {
    initYamlLoader();
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
  Properties load() {
    loadEnvironmentVars();
    loadLocalFiles();
    return eval();
  }

  void initWatcher(CoreConfiguration configuration) {
    if (configuration.getBool("config.watch.enabled", false)) {
      configuration.setWatcher(new FileWatch(configuration, loadContext.loadedFiles(), yamlLoader != null));
    }
  }

  private void initYamlLoader() {
    if (!"true".equals(System.getProperty("skipYaml"))) {
      try {
        Class.forName("org.yaml.snakeyaml.Yaml");
        yamlLoader = new LoadYaml();
      } catch (ClassNotFoundException e) {
        // ignored, no yaml loading
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
    // external file configuration overrides the resources configuration
    loadMain(FILE);
    loadViaSystemProperty();
    loadViaIndirection();
    // test configuration (if found) overrides main configuration
    // we should only find these resources when running tests
    if (!loadTest()) {
      loadLocalDev();
      loadViaCommandLineArgs();
    }
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
        loadFileWithExtensionCheck(prefix + ".yaml");
        loadFileWithExtensionCheck(prefix + ".properties");
      }
    }
  }

  /**
   * Load test configuration.
   *
   * @return true if test properties have been loaded.
   */
  private boolean loadTest() {
    int before = loadContext.size();
    loadProperties("application-test.properties", RESOURCE);
    loadYaml("application-test.yaml", RESOURCE);
    if (loadYaml("application-test.yml", RESOURCE)) {
      log.warn("Please rename application-test.yml to application-test.yaml - Using yml suffix (rather than yaml) is deprecated.");
    }
    if (loadProperties("test-ebean.properties", RESOURCE)) {
      log.warn("Loading properties from test-ebean.properties is deprecated. Please migrate to application-test.yaml or application-test.properties instead.");
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

  private void loadViaPaths(String paths) {
    for (String path : splitPaths(paths)) {
      loadFileWithExtensionCheck(loadContext.eval(path));
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
    loadYaml("application.yaml", source);
    if (loadYaml("application.yml", source)) {
      log.warn("Please rename application.yml to application.yaml - Using yml suffix (rather than yaml) is deprecated.");
    }
    loadProperties("application.properties", source);
    if (loadProperties("ebean.properties", source)) {
      log.warn("Loading properties from ebean.properties is deprecated. Please migrate to use application.yaml or application.properties instead.");
    }
  }

  private void loadViaSystemProperty() {
    String fileName = System.getenv("PROPS_FILE");
    if (fileName == null) {
      fileName = System.getProperty("props.file");
      if (fileName != null) {
        loadFileWithExtensionCheck(fileName);
      }
    }
  }

  void loadFileWithExtensionCheck(String fileName) {
    if (fileName.endsWith("yaml") || fileName.endsWith("yml")) {
      loadYaml(fileName, FILE);
    } else if (fileName.endsWith("properties")) {
      loadProperties(fileName, FILE);
    } else {
      throw new IllegalArgumentException("Expecting only yaml or properties file but got [" + fileName + "]");
    }
  }

  /**
   * Evaluate all the configuration entries and return as properties.
   */
  Properties eval() {
    return loadContext.evalAll();
  }

  boolean loadYaml(String resourcePath, Source source) {
    if (yamlLoader != null) {
      try {
        try (InputStream is = resource(resourcePath, source)) {
          if (is != null) {
            yamlLoader.load(is);
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
          loadProperties(is);
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

  private void loadProperties(InputStream is) throws IOException {
    Properties properties = new Properties();
    properties.load(is);
    put(properties);
  }

  private void put(Properties properties) {
    Enumeration<?> enumeration = properties.propertyNames();
    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      String val = properties.getProperty(key);
      loadContext.put(key, val);
    }
  }

  private class LoadYaml extends YamlLoader {
    @Override
    void add(String key, String val) {
      loadContext.put(key, val);
    }
  }

}
