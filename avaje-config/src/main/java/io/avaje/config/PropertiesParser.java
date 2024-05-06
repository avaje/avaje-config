package io.avaje.config;

import io.avaje.lang.NonNullApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@NonNullApi
final class PropertiesParser implements ConfigParser {

  private static final String[] extensions = new String[]{"properties"};

  @Override
  public String[] supportedExtensions() {
    return extensions;
  }

  @Override
  public Map<String, String> load(Reader reader) {
    try {
      Properties p = new Properties();
      p.load(reader);
      return toMap(p);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Map<String, String> load(InputStream is) {
    try {
      Properties p = new Properties();
      p.load(is);
      return toMap(p);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Map<String, String> toMap(Properties p) {
    Map<String, String> result = new LinkedHashMap<>();
    Set<Map.Entry<Object, Object>> entries = p.entrySet();
    for (Map.Entry<Object, Object> entry : entries) {
      Object value = entry.getValue();
      if (value != null) {
        result.put(entry.getKey().toString(), entry.getValue().toString());
      }
    }
    return result;
  }
}
