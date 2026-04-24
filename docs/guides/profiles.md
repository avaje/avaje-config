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

Activate a profile by setting the `config.profiles` property (note: plural):

```bash
# Development
java -Dconfig.profiles=dev myapp.jar

# Test
java -Dconfig.profiles=test myapp.jar

# Production
java -Dconfig.profiles=prod myapp.jar

# Multiple profiles (comma-separated)
java -Dconfig.profiles=prod,docker myapp.jar
```

Or with environment variables:

```bash
export CONFIG_PROFILES=prod
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
String profile = Config.get("config.profiles", "dev");
if (profile.equals("prod")) {
  // Production-specific behavior
}
```

## Test Configuration Auto-Loading vs Profile Activation

These are two distinct mechanisms — do not confuse them:

**`application-test.yaml` auto-loading (no activation needed):**
`src/test/resources/application-test.yaml` is a special hardcoded filename.
avaje-config loads it automatically whenever it is present on the classpath —
typically during Maven/Gradle test runs. No `config.profiles=test` or
`avaje.profiles=test` is required.

```
src/test/resources/
└── application-test.yaml   ← loaded automatically, no profile activation needed
```

**Explicit profile activation (activation required):**
All other profile files (`application-dev.yaml`, `application-it.yaml`, etc.)
require explicit activation:

```bash
java -Dconfig.profiles=it myapp.jar   # loads application-it.yaml
```

> **Common mistake:** setting `-Davaje.profiles=test` in your test runner to
> load `application-test.yaml`. This is not needed — the file is auto-loaded
> unconditionally when present in test resources.

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
