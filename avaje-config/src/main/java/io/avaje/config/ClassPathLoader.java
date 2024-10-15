package io.avaje.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.avaje.config.InitialLoader.Source;

class ClassPathLoader implements URIConfigLoader {

  private final ResourceLoader resourceLoader;

  public ClassPathLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Override
  public String[] supportedSchemes() {
    return new String[] {"classpath", "file", null};
  }

  @Override
  public Map<String, String> load(URI uri, URILoadContext ctx) {

    final var scheme = uri.getScheme();
    var resource = scheme == null || "file".equals(scheme) ? Source.FILE : Source.RESOURCE;
    var fileName = uri.getPath();
    var extension = fileName.substring(fileName.lastIndexOf(".") + 1);

    return loadCustomExtension(uri, ctx.configParser(extension), resource);
  }

  Map<String, String> loadCustomExtension(URI resourcePath, ConfigParser parser, Source source) {
    try (InputStream is = resource(resourcePath, source)) {
      if (is != null) {
        return parser.load(is);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Error loading properties - " + resourcePath, e);
    }
    return Map.of();
  }

  @Nullable InputStream resource(URI resourcePath, Source source) {
    InputStream is = null;
    if (source == Source.RESOURCE) {
      is = resourceLoader.getResourceAsStream(resourcePath.getPath().substring(1));
    } else {

      var file =
          resourcePath.getScheme() != null
              ? new File(resourcePath)
              : new File(resourcePath.toString());

      if (file.exists()) {
        try {
          is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
          throw new UncheckedIOException(e);
        }
      }
    }
    return is;
  }
}
