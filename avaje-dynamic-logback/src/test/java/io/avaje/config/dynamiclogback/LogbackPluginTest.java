package io.avaje.config.dynamiclogback;

import io.avaje.config.Configuration;
import org.junit.jupiter.api.Test;

import java.util.Map;

class LogbackPluginTest {

  @Test
  void apply() {
    Configuration config = Configuration.builder()
      .put("log.level.other.Foo", "DEBUG")
      .build();

    var plugin = new LogbackPlugin();
    plugin.postInitialization(config);

    config.putAll(Map.of("log.level.other.Foo", "INFO", "log.level.my.Bar", "TRACE"));
  }
}
