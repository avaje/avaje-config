package io.avaje.config;

/**
 * Additional source to load and update configuration.
 */
public interface ConfigurationSource {

  /**
   * Load additional configuration.
   * <p>
   * At this load time the configuration has already loaded properties
   * from files and resources and configuration can be read provide
   * configuration to the source like URL's to load more configuration
   * from etc.
   * <p>
   * The {@link Configuration#setProperty(String, String)} method is
   * used when loading the additional properties from the source.
   * <p>
   * Also note that the source can additionally use
   * {@link Configuration#schedule(long, long, Runnable)} to schedule
   * a period task to for example refresh data etc.
   *
   * @param configuration The configuration with initially properties.
   */
  void load(Configuration configuration);

  /**
   * Explicitly reload the configuration source.
   * <p>
   * Generally the configuration source will schedule a periodic refresh of its
   * configuration but there are cases like Lambda where it can be useful to
   * trigger a refresh explicitly and manually (e.g. on Lambda invocation).
   */
  default void reload() {
    // do nothing by default
  }
}
