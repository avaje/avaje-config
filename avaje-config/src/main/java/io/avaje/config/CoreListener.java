package io.avaje.config;

import java.util.function.Consumer;

/**
 * Wraps the listener taking the interesting keys into account.
 */
final class CoreListener {

  private final Consumer<Event> listener;
  private final String[] keys;

  CoreListener(Consumer<Event> listener, String[] keys) {
    this.listener = listener;
    this.keys = keys;
  }

  void accept(CoreEvent event) {
    if (keys == null || keys.length == 0  || containsKey(event)) {
      listener.accept(event);
    }
  }

  private boolean containsKey(CoreEvent event) {
    final var modifiedKeys = event.modifiedKeys();
    for (String key : keys) {
      if (modifiedKeys.contains(key)) {
        return true;
      }
    }
    return false;
  }
}
