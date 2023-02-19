package io.avaje.config;

import io.avaje.applog.AppLog;

import java.lang.System.Logger.Level;

/**
 * Default implementation of EventLog just uses System.Logger.
 */
final class DefaultConfigurationLog implements ConfigurationLog {

  private final System.Logger log = AppLog.getLogger("io.avaje.config");

  @Override
  public void log(Level level, String message, Throwable thrown) {
    log.log(level, message, thrown);
  }

  @Override
  public void log(Level level, String message, Object... args) {
    log.log(level, message, args);
  }

}
