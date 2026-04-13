# Reacting to Configuration Changes

How to listen for and respond to configuration changes at runtime.

## Adding a Configuration Listener

Implement `ConfigChangeListener` to be notified of configuration changes:

```java
import io.avaje.config.Config;
import io.avaje.config.ConfigChangeListener;

public class MyConfigListener implements ConfigChangeListener {
  @Override
  public void onConfigChange(ConfigChangeEvent event) {
    System.out.println("Configuration changed: " + event.getProperty());
  }
}
```

Register the listener:

```java
Config.addChangeListener(new MyConfigListener());
```

## Listening to Specific Properties

Listen to changes on specific properties:

```java
public class DatabaseConfigListener implements ConfigChangeListener {
  @Override
  public void onConfigChange(ConfigChangeEvent event) {
    String property = event.getProperty();
    String newValue = event.getNewValue();
    
    if (property.equals("database.host")) {
      System.out.println("Database host changed to: " + newValue);
      reconnectDatabase(newValue);
    }
  }
  
  private void reconnectDatabase(String newHost) {
    // Close existing connection and reconnect
  }
}
```

## Practical Examples

### Reload Cache on Configuration Change

```java
public class CacheConfigListener implements ConfigChangeListener {
  private final CacheService cacheService;
  
  public CacheConfigListener(CacheService cacheService) {
    this.cacheService = cacheService;
  }
  
  @Override
  public void onConfigChange(ConfigChangeEvent event) {
    if (event.getProperty().equals("cache.ttl")) {
      int newTtl = Integer.parseInt(event.getNewValue());
      cacheService.setTtl(newTtl);
      cacheService.clear();
    }
  }
}
```

### Update Logger Configuration

```java
public class LoggerConfigListener implements ConfigChangeListener {
  @Override
  public void onConfigChange(ConfigChangeEvent event) {
    if (event.getProperty().equals("logging.level")) {
      String level = event.getNewValue();
      LoggerFactory.setLogLevel(level);
    }
  }
}
```

### Notify Dependents

```java
public class AppConfigListener implements ConfigChangeListener {
  private final ApplicationEventBus eventBus;
  
  public AppConfigListener(ApplicationEventBus eventBus) {
    this.eventBus = eventBus;
  }
  
  @Override
  public void onConfigChange(ConfigChangeEvent event) {
    ConfigurationChangedEvent evt = 
      new ConfigurationChangedEvent(event.getProperty(), event.getNewValue());
    eventBus.publish(evt);
  }
}
```

## Using with Dependency Injection

With Avaje Inject, register the listener in your application setup:

```java
@Singleton
public class ApplicationStartup {
  public ApplicationStartup(ConfigChangeListener listener) {
    Config.addChangeListener(listener);
  }
}
```

## Important Notes

- Listeners are called **synchronously** - keep processing quick
- Changes are detected when configuration is reloaded
- Use for dynamic reconfiguration, not for every request
- External configuration services can push updates (cloud config)

## Next Steps

- Configure [cloud integration](cloud-integration.md) for dynamic configuration
- See [troubleshooting](troubleshooting.md#listener-not-called) if listeners aren't working
