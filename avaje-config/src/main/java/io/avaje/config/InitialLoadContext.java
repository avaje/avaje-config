package io.avaje.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.*;

import io.avaje.config.CoreEntry.CoreMap;

/**
 * Manages the underlying map of properties we are gathering.
 */
final class InitialLoadContext {

  private final ConfigurationLog log;
  private final ResourceLoader resourceLoader;
  /**
   * CoreMap we are loading the properties into.
   */
  private final CoreEntry.CoreMap map = CoreEntry.newMap();

  /**
   * Names of resources/files that were loaded.
   */
  private final Set<String> loadedResources = new LinkedHashSet<>();
  private final List<File> loadedFiles = new ArrayList<>();
  private final CoreExpressionEval exprEval;

  InitialLoadContext(ConfigurationLog log, ResourceLoader resourceLoader) {
    this.log = log;
    this.resourceLoader = resourceLoader;
    this.exprEval = new CoreExpressionEval(map);
  }

  Set<String> loadedFrom() {
    return loadedResources;
  }

  List<File> loadedFiles() {
    return loadedFiles;
  }

  String eval(String expression) {
    return exprEval.eval(expression);
  }

  /**
   * If we are in Kubernetes and expose environment variables
   * POD_NAMESPACE, POD_NAME, POD_VERSION, POD_ID we can use these to set
   * app.environment, app.name, app.instanceId, app.version and app.ipAddress.
   */
  void loadEnvironmentVars() {
    final String podName = System.getenv("POD_NAME");
    initSystemProperty(podName, "app.instanceId");
    initSystemProperty(podService(podName), "app.name");
    initSystemProperty(System.getenv("POD_NAMESPACE"), "app.environment");
    initSystemProperty(System.getenv("POD_VERSION"), "app.version");
    initSystemProperty(System.getenv("POD_IP"), "app.ipAddress");
  }

  private void initSystemProperty(String envValue, String key) {
    if (envValue != null && System.getProperty(key) == null) {
      map.put(key, envValue, Constants.ENV_VARIABLES);
    }
  }

  static String podService(String podName) {
    if (podName != null && podName.length() > 16) {
      int p0 = podName.lastIndexOf('-', podName.length() - 16);
      if (p0 > -1) {
        return podName.substring(0, p0);
      }
    }
    return null;
  }

  /**
   * Return the input stream (maybe null) for the given source.
   */
  InputStream resource(String resourcePath, InitialLoader.Source source) {
    InputStream is = null;
    if (source == InitialLoader.Source.RESOURCE) {
      is = resourceStream(resourcePath);
      if (is != null) {
        loadedResources.add("resource:" + resourcePath);
      }
    } else {
      File file = new File(resourcePath);
      if (file.exists()) {
        try {
          is = new FileInputStream(file);
          loadedResources.add("file:" + resourcePath);
          loadedFiles.add(file);
        } catch (FileNotFoundException e) {
          throw new IllegalStateException(e);
        }
      }
    }
    return is;
  }

  private InputStream resourceStream(String resourcePath) {
    InputStream is = resourceLoader.getResourceAsStream(resourcePath);
    if (is == null) {
      // search the module path for top level resource
      is = ClassLoader.getSystemResourceAsStream(resourcePath);
    }
    return is;
  }

  /**
   * Add a property entry.
   */
  void put(String key, String val, String source) {
    if (val != null) {
      val = val.trim();
    }
    map.put(key, val, source);
  }

  /**
   * Evaluate all the expressions and return as a Properties object.
   */
  CoreMap evalAll() {
    log.log(Level.TRACE, "load from {0}", loadedResources);
    var core = CoreEntry.newMap();
    map.forEach((key, entry) -> core.put(key, exprEval.eval(entry.value()), entry.source()));
    return core;
  }

  /**
   * Read the special properties that can point to an external properties source.
   */
  String indirectLocation() {
    CoreEntry indirectLocation = map.get("load.properties");
    if (indirectLocation == null) {
      indirectLocation = map.get("load.properties.override");
    }
    return indirectLocation == null ? null : indirectLocation.value();
  }

  /**
   * Return the number of properties resources loaded.
   */
  int size() {
    return loadedResources.size();
  }

  String getAppName() {
    final CoreEntry appName = map.get("app.name");
    return (appName != null) ? appName.value() : System.getProperty("app.name");
  }
}
