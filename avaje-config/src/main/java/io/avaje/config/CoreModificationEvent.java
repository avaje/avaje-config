package io.avaje.config;

import java.util.Set;

final class CoreModificationEvent implements ModificationEvent {

  private final String name;
  private final Set<String> modifiedKeys;
  private final CoreConfiguration origin;

  CoreModificationEvent(String name, Set<String> modifiedKeys, CoreConfiguration origin) {
    this.name = name;
    this.modifiedKeys = modifiedKeys;
    this.origin = origin;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Configuration configuration() {
    return origin;
  }

  @Override
  public Set<String> modifiedKeys() {
    return modifiedKeys;
  }
}
