package io.avaje.config;

import java.util.function.Consumer;

/**
 * Wraps the listener taking the interesting keys into account.
 */
final class CoreListener {

  private final Consumer<ModificationEvent> listener;
  private final String[] keys;

  CoreListener(Consumer<ModificationEvent> listener, String[] keys) {
    this.listener = listener;
    this.keys = keys;
  }

  void accept(CoreModificationEvent event) {
    if (keys == null || keys.length == 0 || containsKey(event)) {
      listener.accept(event);
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
