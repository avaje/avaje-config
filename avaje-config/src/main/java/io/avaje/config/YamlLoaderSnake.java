package io.avaje.config;

import io.avaje.lang.NonNullApi;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads configuration from Yml into the load context.
 * <p>
 * Note that this ignores 'lists' so just reads 'maps' and scalar values.
 */
@NonNullApi
final class YamlLoaderSnake implements YamlLoader {

  private final Yaml yaml;

  YamlLoaderSnake() {
    this.yaml = new Yaml();
  }

  @Override
  public Map<String, String> load(Reader reader) {
    return load(yaml.loadAll(reader));
  }

  @Override
  public Map<String, String> load(InputStream is) {
    return load(yaml.loadAll(is));
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> load(Iterable<Object> source) {
    Load load = new Load();
    for (Object map : source) {
      load.loadMap((Map<String, Object>) map, null);
    }
    return load.map();
  }

  private static class Load {

    private final Map<String, String> map = new LinkedHashMap<>();

    void add(String key, String val) {
      map.put(key, val);
    }

    @SuppressWarnings("unchecked")
    private void loadMap(Map<String, Object> map, String path) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        if (path != null) {
          key = path + "." + key;
        }
        Object val = entry.getValue();
        if (val instanceof Map) {
          loadMap((Map<String, Object>) val, key);
        } else {
          addScalar(key, val);
        }
      }
    }

    private void addScalar(String key, Object val) {
      if (val instanceof String) {
        add(key, (String) val);
      } else if (val instanceof Number || val instanceof Boolean) {
        add(key, val.toString());
      }
    }

    private Map<String, String> map() {
      return map;
    }
  }
}
