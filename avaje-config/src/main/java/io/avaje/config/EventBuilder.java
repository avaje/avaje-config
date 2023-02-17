package io.avaje.config;

public interface EventBuilder {

  EventBuilder put(String key, String value);

  EventBuilder remove(String key);

  void publish();

}
