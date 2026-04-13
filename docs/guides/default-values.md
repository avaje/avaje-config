# Using Default Values

How to provide sensible defaults for configuration properties.

## Simple Defaults

Provide a default when reading a property:

```java
import io.avaje.config.Config;

// Returns 8080 if not configured
int port = Config.getInt("server.port", 8080);

// Returns "localhost" if not configured
String host = Config.get("server.host", "localhost");

// Returns false if not configured
boolean ssl = Config.getBool("server.ssl.enabled", false);
```

## Defaults in Configuration Classes

Use constructor parameters with defaults:

```java
@Config
public class AppConfig {
  public final int serverPort;
  public final String serverHost;
  public final boolean sslEnabled;
  
  public AppConfig(
    @ConfigProperty(value = "server.port", defValue = "8080") int serverPort,
    @ConfigProperty(value = "server.host", defValue = "localhost") String serverHost,
    @ConfigProperty(value = "server.ssl.enabled", defValue = "false") boolean sslEnabled
  ) {
    this.serverPort = serverPort;
    this.serverHost = serverHost;
    this.sslEnabled = sslEnabled;
  }
}
```

## Defaults in Configuration Files

Specify defaults in `application.yaml`:

```yaml
server:
  port: 8080
  host: localhost
  ssl:
    enabled: false
    keystore: classpath:keystore.jks

database:
  connections: 10
  timeout: 30
```

## When to Use Each Approach

| Approach | Use When |
|----------|----------|
| Direct call defaults | Simple, single properties |
| Configuration class defaults | Multiple related properties, type safety |
| YAML file defaults | Defaults shared across environments |

## Overriding Defaults

Defaults can be overridden by:

1. **System properties**: `java -Dserver.port=9000`
2. **Environment variables**: `export SERVER_PORT=9000`
3. **Configuration files** for current environment: `application-prod.yaml`

See [Profiles](profiles.md) for loading environment-specific defaults.

## Next Steps

- Learn about [environment-specific profiles](profiles.md)
- Use [environment variables](environment-variables.md)
