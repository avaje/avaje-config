package io.avaje.config;

import io.avaje.lang.Nullable;
import io.avaje.spi.Service;

import java.io.InputStream;

/**
 * Plugin API for loading resources typically from the classpath or module path.
 * <p>
 * When not specified Avaje Config provides a default implementation that looks
 * to find resources using the class loader associated with the ResourceLoader.
 * <p>
 * Note there is a fallback to use {@link ClassLoader#getSystemResourceAsStream(String)}
 * if the ResourceLoader returns null.
 */
public interface ResourceLoader extends ConfigSPI {

  /**
   * Return the InputStream for the given resource or null if it can not be found.
   */
  @Nullable
  InputStream getResourceAsStream(String resourcePath);
}
