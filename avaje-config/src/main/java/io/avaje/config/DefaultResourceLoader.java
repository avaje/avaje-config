package io.avaje.config;

import java.io.InputStream;

/**
 * Default implementation of the ResourceLoader.
 */
final class DefaultResourceLoader implements ResourceLoader {

  @Override
  public InputStream getResourceAsStream(String resourcePath) {
    var is = getClass().getResourceAsStream("/" + resourcePath);
    if (is == null) {
      // search the module path for top level resource
      is = ClassLoader.getSystemResourceAsStream(resourcePath);
    }
    return is;
  }
}
