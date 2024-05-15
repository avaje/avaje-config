import io.avaje.config.ConfigExtension;
import io.avaje.config.dynamiclogback.LogbackPlugin;

module io.avaje.config.dynamic.logback {

  requires io.avaje.config;
  requires transitive io.avaje.lang;
  requires transitive io.avaje.applog;

  requires static io.avaje.spi;
  requires static org.slf4j;
  requires static ch.qos.logback.core;
  requires static ch.qos.logback.classic;

  provides ConfigExtension with LogbackPlugin;
}
