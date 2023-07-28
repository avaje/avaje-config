package io.avaje.config.yml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import io.avaje.config.YamlLoader;

/**
 * Loads configuration from Yml into the load context.
 *
 * <p>Note that this ignores 'lists' so just reads 'maps' and scalar values.
 */
public final class YamlLoaderSnake implements YamlLoader {

  private final Yaml yaml;

  public YamlLoaderSnake() {
    this.yaml = new Yaml();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, String> load(InputStream is) {
    final Load load = new Load();
    for (final Object map : yaml.loadAll(is)) {
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
      for (final Map.Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        if (path != null) {
          key = path + "." + key;
        }
        final Object val = entry.getValue();
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
