package io.avaje.config.load;

import io.avaje.config.PropertyExpression;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Pattern;

import static io.avaje.config.load.Loader.Source.FILE;
import static io.avaje.config.load.Loader.Source.RESOURCE;

/**
 * Loads the configuration from known/expected locations.
 * <p>
 * Defines the loading order of resources and files.
 * </p>
 */
class Loader {

  private static final Pattern SPLIT_PATHS = Pattern.compile("[\\s,;]+");

  enum Source {
    RESOURCE,
    FILE
  }

  private final LoadContext loadContext = new LoadContext();

  private YamlLoader yamlLoader;

  Loader() {
    if (!"true".equals(System.getProperty("skipYaml"))) {
      try {
        Class<?> exists = Class.forName("org.yaml.snakeyaml.Yaml");
        if (exists != null) {
          yamlLoader = new YamlLoader(loadContext);
        }
      } catch (ClassNotFoundException e) {
        // ignored, no yaml loading
      }
    }
  }

  void loadEnvironmentVars() {
    loadContext.loadEnvironmentVars();
  }

  /**
   * Load the configuration with the expected ordering.
   */
  void load() {

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

  private void loadViaCommandLine(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.startsWith("-P") || arg.startsWith("-p")) {
        if (arg.length() == 2 && i < args.length - 1) {
          // next argument expected to be a properties file paths
          i++;
          loadViaPaths(args[i]);
        } else {
          // no space between -P and properties file paths
          loadViaPaths(arg.substring(2));
        }
      }
    }
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
    loadYaml("application-test.yml", RESOURCE);
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
      loadFileWithExtensionCheck(PropertyExpression.eval(path));
    }
  }

  String[] splitPaths(String location) {
    return SPLIT_PATHS.split(location);
  }

  /**
   * Load the main configuration for the given source.
   */
  private void loadMain(Source source) {
    loadYaml("application.yaml", source);
    loadYaml("application.yml", source);
    loadProperties("application.properties", source);
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
    return loadContext.eval();
  }

  void loadYaml(String resourcePath, Source source) {
    if (yamlLoader != null) {
      try {
        try (InputStream is = resource(resourcePath, source)) {
          yamlLoader.load(is);
        }
      } catch (Exception e) {
        throw new RuntimeException("Error loading yaml properties - " + resourcePath, e);
      }
    }
  }

  void loadProperties(String resourcePath, Source source) {
    try {
      try (InputStream is = resource(resourcePath, source)) {
        if (is != null) {
          loadProperties(is);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Error loading properties - " + resourcePath, e);
    }
  }

  private InputStream resource(String resourcePath, Source source) {
    return loadContext.resource(resourcePath, source);
  }

  private void loadProperties(InputStream is) throws IOException {
    if (is != null) {
      Properties properties = new Properties();
      properties.load(is);
      put(properties);
    }
  }

  private void put(Properties properties) {
    Enumeration<?> enumeration = properties.propertyNames();
    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      String property = properties.getProperty(key);
      loadContext.put(key, property);
    }
  }

}
