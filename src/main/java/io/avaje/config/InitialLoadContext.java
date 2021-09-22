package io.avaje.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Manages the underlying map of properties we are gathering.
 */
final class InitialLoadContext {

  /**
   * Map we are loading the properties into.
   */
  private final Map<String, String> map = new LinkedHashMap<>();

  /**
   * Names of resources/files that were loaded.
   */
  private final Set<String> loadedResources = new LinkedHashSet<>();
  private final List<File> loadedFiles = new ArrayList<>();
  private final CoreExpressionEval exprEval;

  InitialLoadContext() {
    this.exprEval = new CoreExpressionEval(map);
  }

  String loadedFrom() {
    return loadedResources.toString();
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
      map.put(key, envValue);
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
    InputStream is = getClass().getResourceAsStream("/" + resourcePath);
    if (is == null) {
      // search the module path for top level resource
      is = ClassLoader.getSystemResourceAsStream(resourcePath);
    }
    return is;
  }

  /**
   * Add a property entry.
   */
  void put(String key, String val) {
    if (val != null) {
      val = val.trim();
    }
    map.put(key, val);
  }

  /**
   * Evaluate all the expressions and return as a Properties object.
   */
  Properties evalAll() {
    Config.log.trace("load from {}", loadedResources);
    Properties properties = new Properties();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      properties.setProperty(key, exprEval.eval(entry.getValue()));
    }
    return properties;
  }

  /**
   * Read the special properties that can point to an external properties source.
   */
  String indirectLocation() {
    String indirectLocation = map.get("load.properties");
    if (indirectLocation == null) {
      indirectLocation = map.get("load.properties.override");
    }
    return indirectLocation;
  }

  /**
   * Return the number of properties resources loaded.
   */
  int size() {
    return loadedResources.size();
  }

  String getAppName() {
    final String appName = map.get("app.name");
    return (appName != null) ? appName : System.getProperty("app.name");
  }
}
