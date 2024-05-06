package io.avaje.config;

import java.io.InputStream;

/**
 * Default implementation of the ResourceLoader.
 */
final class DefaultResourceLoader implements ResourceLoader {

  @Override
  public InputStream getResourceAsStream(String resourcePath) {
    var inputStream = getClass().getResourceAsStream("/" + resourcePath);
    if (inputStream == null) {
      // search the module path for top level resource
      inputStream = ClassLoader.getSystemResourceAsStream(resourcePath);
    }
    return inputStream;
  }
}
