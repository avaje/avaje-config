package io.avaje.config.dynamiclogback;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.avaje.applog.AppLog;
import io.avaje.config.Configuration;
import io.avaje.config.ConfigurationPlugin;
import io.avaje.config.ModificationEvent;
import io.avaje.spi.ServiceProvider;

/**
 * Plugin to dynamically adjust the log levels via configuration changes.
 */
@ServiceProvider
public final class LogbackPlugin implements ConfigurationPlugin {

  private static final System.Logger log = AppLog.getLogger(LogbackPlugin.class);

  @Override
  public void apply(Configuration configuration) {
    final var loggerContext = loggerContext();
    final var config = configuration.forPath("log.level");
    for (String key : config.keys()) {
      String rawLevel = config.getNullable(key);
      setLogLevel(key, loggerContext, rawLevel);
      log.log(TRACE, "log level {0} for {1}", rawLevel, key);
    }
    configuration.onChange(this::onChangeAny);
  }

  private static void setLogLevel(String key, LoggerContext loggerContext, String level) {
    Logger logger = loggerContext.getLogger(key);
    if (logger != null && level != null) {
      logger.setLevel(Level.toLevel(level));
    }
  }

  private void onChangeAny(ModificationEvent modificationEvent) {
    final var loggerContext = loggerContext();
    final var config = modificationEvent.configuration();
    modificationEvent.modifiedKeys().stream()
      .filter(key -> key.startsWith("log.level."))
      .forEach(key -> {
        String logKey = key.substring(10);
        String rawLevel = config.getNullable(key);
        setLogLevel(logKey, loggerContext, rawLevel);
        log.log(DEBUG, "set log level {0} for {1}", rawLevel, logKey);
      });
  }

  private LoggerContext loggerContext() {
    return (LoggerContext) LoggerFactory.getILoggerFactory();
  }
}
