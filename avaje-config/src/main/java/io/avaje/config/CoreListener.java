package io.avaje.config;

import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;

/**
 * Wraps the listener taking the interesting keys into account.
 */
final class CoreListener {

  private final ConfigurationLog log;
  private final Consumer<ModificationEvent> listener;
  private final String[] keys;

  CoreListener(ConfigurationLog log, Consumer<ModificationEvent> listener, String[] keys) {
    this.log = log;
    this.listener = listener;
    this.keys = keys;
  }

  void accept(CoreModificationEvent event) {
    if (keys == null || keys.length == 0 || containsKey(event)) {
      try {
        listener.accept(event);
      } catch (Exception e) {
        log.log(ERROR, "Error during onChange notification", e);
      }
    }
  }

  private boolean containsKey(CoreModificationEvent event) {
    final var modifiedKeys = event.modifiedKeys();
    for (String key : keys) {
      if (modifiedKeys.contains(key)) {
        return true;
      }
    }
    return false;
  }
}
