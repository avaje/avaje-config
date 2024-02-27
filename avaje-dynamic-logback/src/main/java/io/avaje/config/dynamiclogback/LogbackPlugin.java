package io.avaje.config.dynamiclogback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.avaje.applog.AppLog;
import io.avaje.config.Configuration;
import io.avaje.config.ConfigurationPlugin;
import io.avaje.config.ModificationEvent;
import org.slf4j.LoggerFactory;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Plugin to dynamically adjust the log levels via configuration changes.
 */
public final class LogbackPlugin implements ConfigurationPlugin {

  private static final System.Logger log = AppLog.getLogger(LogbackPlugin.class);

  @Override
  public void apply(Configuration configuration) {
    final var loggerContext = loggerContext();
    final var config = configuration.forPath("log.level");
    for (String key : config.keys()) {
      String rawLevel = configuration.get(key);
      setLogLevel(key, loggerContext, rawLevel);
    }
    configuration.onChange(this::onChangeAny);
  }

  private static void setLogLevel(String key, LoggerContext loggerContext, String rawLevel) {
    String logKey = key.substring(10);
    Logger logger = loggerContext.getLogger(logKey);
    if (logger != null) {
      log.log(DEBUG, "logger change for {0} to {1}", logKey, rawLevel);
      logger.setLevel(Level.toLevel(rawLevel));
    }
  }

  private void onChangeAny(ModificationEvent modificationEvent) {
    final var loggerContext = loggerContext();
    final var config = modificationEvent.configuration();
    modificationEvent.modifiedKeys().stream()
      .filter(key -> key.startsWith("log.level."))
      .forEach(key -> {
        String rawLevel = config.get(key);
        setLogLevel(key, loggerContext, rawLevel);
      });
  }

  private LoggerContext loggerContext() {
    return (LoggerContext) LoggerFactory.getILoggerFactory();
  }
}
