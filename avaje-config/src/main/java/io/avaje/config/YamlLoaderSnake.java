package io.avaje.config;

import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;
import org.yaml.snakeyaml.Yaml;

/**
 * Loads configuration from Yml into the load context.
 * <p>
 * Note that this ignores 'lists' so just reads 'maps' and scalar values.
 */
@NullMarked
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
        } else if (val instanceof List) {
          loadList((List<?>) val, key);
        } else {
          addScalar(key, val);
        }
      }
    }

    @SuppressWarnings("unchecked")
    private void loadList(List<?> list, String path) {
      boolean hasObjects = list.stream().anyMatch(item -> item instanceof Map);
      if (hasObjects) {
        for (int i = 0; i < list.size(); i++) {
          Object item = list.get(i);
          if (item instanceof Map) {
            loadMap((Map<String, Object>) item, path + "[" + i + "]");
          } else {
            addScalar(path + "[" + i + "]", item);
          }
        }
      } else {
        add(path, list.stream()
            .map(item -> item == null ? "" : item.toString())
            .collect(Collectors.joining(",")));
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
