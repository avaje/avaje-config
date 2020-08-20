package io.avaje.config.load;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * Loads configuration from Yml into the load context.
 * <p>
 * Note that this ignores 'lists' so just reads 'maps' and scalar values.
 * </p>
 */
abstract class YamlLoader {

  private final Yaml yaml;

  YamlLoader() {
    this.yaml = new Yaml();
  }

  /**
   * Set the key value pair.
   */
  abstract void add(String key, String val);

  @SuppressWarnings("unchecked")
  void load(InputStream is) {
    for (Object map : yaml.loadAll(is)) {
      loadMap((Map<String, Object>)map, null);
    }
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
      } else  {
        addScalar(key, val);
      }
    }
  }

  private void addScalar(String key, Object val) {
    if (val instanceof String) {
      add(key,  (String) val);
    } else if (val instanceof Number || val instanceof Boolean) {
      add(key,  val.toString());
    }
  }

}
