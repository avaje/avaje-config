import io.avaje.config.ConfigurationPlugin;
import io.avaje.config.dynamiclogback.LogbackPlugin;

module io.avaje.config.dynamic.logback {

  requires io.avaje.config;
  requires transitive io.avaje.lang;
  requires transitive io.avaje.applog;

  requires static org.slf4j;
  requires static ch.qos.logback.core;
  requires static ch.qos.logback.classic;

  provides ConfigurationPlugin with LogbackPlugin;
}
