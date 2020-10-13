package io.avaje.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads configuration from Yml into the load context.
 * <p>
 * Note that this ignores 'lists' so just reads 'maps' and scalar values.
 * </p>
 */
final class YamlLoaderSnake implements YamlLoader {

  private final Yaml yaml;

  YamlLoaderSnake() {
    this.yaml = new Yaml();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, String> load(InputStream is) {
    Load load = new Load();
    for (Object map : yaml.loadAll(is)) {
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
