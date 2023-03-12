package io.avaje.config;

import java.io.InputStream;

/**
 * Default implementation of the ResourceLoader.
 */
final class DefaultResourceLoader implements ResourceLoader {

  @Override
  public InputStream getResourceAsStream(String resourcePath) {
    return getClass().getResourceAsStream("/" + resourcePath);
  }
}
