# Reacting to Configuration Changes

How to listen for and respond to configuration changes at runtime.

## Listening to a Specific Property

Use `Config.onChange()` to register a lambda that fires when a named property changes:

```java
import io.avaje.config.Config;

// String value listener
Config.onChange("database.host", newHost -> {
  System.out.println("Database host changed to: " + newHost);
  reconnectDatabase(newHost);
});

// Typed listeners
Config.onChangeInt("server.port", newPort -> {
  System.out.println("Port changed to: " + newPort);
});

Config.onChangeLong("upload.max.bytes", newSize -> {
  fileService.setMaxSize(newSize);
});

Config.onChangeBool("features.enabled", isEnabled -> {
  featureManager.setEnabled(isEnabled);
});
```

## Listening to Multiple Properties

Use the bulk `Config.onChange(Consumer<ModificationEvent>, String... keys)` form
to watch several properties with a single listener:

```java
import io.avaje.config.Config;
import io.avaje.config.ModificationEvent;

Config.onChange(event -> {
  Set<String> changed = event.modifiedKeys();
  System.out.println("Config changed. Modified keys: " + changed);

  if (changed.contains("database.host")) {
    reconnectDatabase(Config.get("database.host"));
  }
  if (changed.contains("cache.ttl")) {
    cacheService.setTtl(Config.getInt("cache.ttl", 300));
  }
}, "database.host", "cache.ttl");
```

Omit the key arguments to listen for **any** configuration change:

```java
Config.onChange(event -> {
  System.out.println("Any config changed: " + event.modifiedKeys());
});
```

## Practical Example: Dynamic Feature Flag

```java
@Singleton
public class FeatureManager {
  private volatile boolean featureEnabled;

  @Inject
  public FeatureManager() {
    this.featureEnabled = Config.getBool("features.new-ui", false);

    Config.onChangeBool("features.new-ui", newValue -> {
      this.featureEnabled = newValue;
      System.out.println("Feature toggle updated: " + newValue);
    });
  }

  public boolean isEnabled() {
    return featureEnabled;
  }
}
```

## Triggering a Reload

Force an immediate reload of all configuration sources (e.g. for AWS AppConfig):

```java
Config.asConfiguration().reloadSources();
```

## Important Notes

- Listeners are called **synchronously**, keeping processing quick
- Changes are delivered when configuration is reloaded (polling or explicit `reloadSources()`)
- Use for dynamic reconfiguration (feature flags, pool sizes, log levels)
- Listeners registered via `Config.onChange()` are held with a strong reference

## Next Steps

- Configure [cloud integration](cloud-integration.md) for dynamic configuration
- See [troubleshooting](troubleshooting.md#listener-not-called) if listeners aren't working
