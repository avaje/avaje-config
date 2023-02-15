package io.avaje.config;

import java.lang.System.Logger.Level;

/**
 * Configuration events are sent to this event log.
 * <p>
 * The EventLog implementation can be provided by ServiceLoader and then can
 * control how the events are logged. For example, it might delay logging messages
 * until logging implementation has finished configuration.
 */
public interface EventLog {

  /**
   * Invoked when the configuration is being initialised.
   */
  default void preInitialisation() {
    // do nothing by default
  }

  /**
   * Invoked when the initialisation of configuration has been completed.
   */
  default void postInitialisation() {
    // do nothing by default
  }

  /**
   * Log an event with the given level, message, and thrown exception.
   */
  void log(Level level, String message, Throwable thrown);

  /**
   * Log an event with the given level, formatted message, and arguments.
   * <p>
   * The message format is as per {@link java.text.MessageFormat#format(String, Object...)}.
   */
  void log(Level level, String message, Object... args);

}
