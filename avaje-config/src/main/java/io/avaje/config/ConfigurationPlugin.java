package io.avaje.config;

import java.util.function.Consumer;

/**
 * Plugin that is initiated after the configuration has been loaded.
 */
public interface ConfigurationPlugin extends ConfigExtension {

  /**
   * Apply the plugin. Typically, a plugin might read configuration and do something
   * and listen for configuration changes via {@link Configuration#onChange(Consumer, String...)}.
   * <p>
   * If <code>config.eval.afterPlugins=true</code> then expressions that were unresolved on the initial
   * load are re-evaluated once after all plugins have been applied, so a plugin can supply values
   * that other properties reference via <code>${...}</code>.
   *
   * @param configuration The configuration that has been loaded including all {@link ConfigurationSource}.
   */
  void apply(Configuration configuration);
}
