# Getting Started with Avaje Config

A quick introduction to loading configuration with avaje-config.

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-config</artifactId>
  <version>2.5</version>
</dependency>
```

## Loading Configuration

Create a `application.yaml` file in `src/main/resources`:

```yaml
server:
  port: 8080
  ssl:
    enabled: false

database:
  host: localhost
  port: 5432
  name: myapp
```

Access configuration in your code:

```java
import io.avaje.config.Config;

public class MyApplication {
  public static void main(String[] args) {
    int serverPort = Config.getInt("server.port");
    String dbHost = Config.get("database.host");
    boolean sslEnabled = Config.getBool("server.ssl.enabled", false);
  }
}
```

## Configuration Sources

Avaje Config supports multiple configuration sources (in priority order):

1. System properties: `-Dserver.port=9000`
2. Environment variables: `SERVER_PORT=9000`
3. Application properties file: `application.yaml`
4. Default values in code

## Type-Safe Configuration

For larger applications, use type-safe configuration classes:

```java
@Config
public class AppConfig {
  public final String dbHost;
  public final int dbPort;
  
  public AppConfig(String dbHost, int dbPort) {
    this.dbHost = dbHost;
    this.dbPort = dbPort;
  }
}
```

Use in your code:

```java
public class MyService {
  private final AppConfig config;
  
  public MyService(AppConfig config) {
    this.config = config;
  }
}
```

## Next Steps

- Learn about [default values](default-values.md)
- Load [profile-specific configuration](profiles.md)
- Use [environment variables](environment-variables.md)
