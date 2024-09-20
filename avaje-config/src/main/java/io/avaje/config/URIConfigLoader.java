package io.avaje.config;

import java.net.URI;
import java.util.Map;

/** Additional source to load and update configuration. */
public interface URIConfigLoader extends ConfigExtension {

  /** URI Scheme Supported by this loader */
  String supportedScheme();

  /**
   * Load additional configuration.
   *
   * <p>The {@link Configuration#setProperty(String, String)} method is used when loading the
   * additional properties from the source.
   *
   * <p>Also note that the source can additionally use {@link Configuration#schedule(long, long,
   * Runnable)} to schedule a period task to for example refresh data etc.
   *
   * @param configuration The configuration with initially properties.
   */
  Map<String, String> load(URI uri, ConfigParsers parsers);
}
