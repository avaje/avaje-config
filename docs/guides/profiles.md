# Loading Profile-Specific Configuration

How to load different configuration for development, test, and production environments.

## Profile Files

Create environment-specific configuration files:

```
src/main/resources/
├── application.yaml          # Shared defaults
├── application-dev.yaml      # Development
├── application-test.yaml     # Test
└── application-prod.yaml     # Production
```

## Activating Profiles

Activate a profile by setting the `config.profile` property:

```bash
# Development
java -Dconfig.profile=dev myapp.jar

# Test
java -Dconfig.profile=test myapp.jar

# Production
java -Dconfig.profile=prod myapp.jar
```

Or with environment variables:

```bash
export CONFIG_PROFILE=prod
java myapp.jar
```

## Example Profile Configurations

**application.yaml** (shared defaults):
```yaml
app:
  name: MyApp
  version: 1.0.0

logging:
  level: INFO
```

**application-dev.yaml** (development):
```yaml
server:
  port: 8080

database:
  host: localhost
  port: 5432

logging:
  level: DEBUG
```

**application-prod.yaml** (production):
```yaml
server:
  port: 443
  ssl:
    enabled: true

database:
  host: db.example.com
  port: 5432

logging:
  level: WARN
```

## Accessing Profile in Code

Get the active profile:

```java
String profile = Config.get("config.profile", "dev");
if (profile.equals("prod")) {
  // Production-specific behavior
}
```

## Profile-Specific Beans

Use profiles with dependency injection:

```java
@Config
public class DatabaseConfig {
  public final String host;
  
  @Config
  public static class Prod {
    public final String host = "prod-db.example.com";
  }
  
  @Config
  public static class Dev {
    public final String host = "localhost";
  }
}
```

## Next Steps

- Use [environment variables](environment-variables.md) to override values
- Set up [change listeners](change-listeners.md) to react to configuration changes
