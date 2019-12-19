package io.avaje.config.load;

import java.util.Properties;

/**
 * Loads and evaluates properties and yml configuration.
 */
public class PropertiesLoader {

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
  public Properties load() {

    Loader loader = new Loader();
    loader.loadEnvironmentVars();
    loader.load();
    return loader.eval();
  }

}
