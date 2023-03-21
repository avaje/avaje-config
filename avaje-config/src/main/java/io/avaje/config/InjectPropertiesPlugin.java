package io.avaje.config;

import java.util.Optional;

import io.avaje.inject.spi.PropertyRequiresPlugin;

/**
 * Plugin used with Avaje Inject to support the {@code @RequiresProperty} annotation
 * for conditional wiring based on properties.
 */
public class InjectPropertiesPlugin implements PropertyRequiresPlugin {

  @Override
  public Optional<String> get(String property) {
    return Config.getOptional(property);
  }

  @Override
  public boolean contains(String property) {
    return Config.getNullable(property) != null;
  }

  @Override
  public boolean missing(String property) {
    return Config.getNullable(property) == null;
  }

  @Override
  public boolean equalTo(String property, String value) {
    return value.equals(Config.getNullable(property));
  }

  @Override
  public boolean notEqualTo(String property, String value) {
    return !value.equals(Config.getNullable(property));
  }
}
