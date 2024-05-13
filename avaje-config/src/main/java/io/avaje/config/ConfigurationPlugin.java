package io.avaje.config;

import java.util.function.Consumer;

/** Plugin that is initiated before/after the configuration has been loaded. */
public interface ConfigurationPlugin extends ConfigSPI {

  /** Invoked when the configuration is being initialised. */
  default void preInitialisation() {
    // do nothing by default
  }

  /**
   * Apply the plugin. Typically, a plugin might read configuration and do something and listen for
   * configuration changes via {@link Configuration#onChange(Consumer, String...)}.
   *
   * @param configuration The configuration that has been loaded including all {@link
   *     ConfigurationSource}.
   */
  void postInitialization(Configuration configuration);
}
