package io.avaje.config;

import java.util.Set;

public interface Event {

  String name();

  Configuration configuration();

  Set<String> modifiedKeys();
}
