# Avaje Config Library Definition

Avaje Config loads application properties from multiple sources (YAML, properties files, environment variables, command-line args) with runtime update support. It's lightweight, zero-dependency, and designed for cloud-native applications with dynamic configuration needs.

## Identity

- **Name**: Avaje Config
- **Package**: `io.avaje.config`
- **Description**: Load and manage application configuration properties from multiple sources with runtime updates
- **Category**: Configuration Management
- **Repository**: https://github.com/avaje/avaje-config
- **Issues**: https://github.com/avaje/avaje-config/issues
- **Releases**: https://github.com/avaje/avaje-config/releases
- **Discord**: https://discord.gg/Qcqf9R27BR

## Version & Requirements

- **Latest Release**: 5.1 (latest stable)
- **Minimum Java Version**: 11+
- **Build Tools**: Maven 3.6+, Gradle 7.0+
- **GraalVM Support**: Yes — Full native image support with zero reflection
- **Default Branch**: main (as of April 2026)

## Dependencies

### Runtime
- **No external dependencies** — Zero runtime dependencies!
- **SnakeYAML** — Optional for YAML parsing (included automatically if needed)

### Test
- **JUnit 5** — Testing framework

### Optional
- **avaje-simple-logger** — For logging (optional, works with any SLF4J impl)
- **avaje-config-toml** — TOML file support
- **avaje-aws-appconfig** — AWS AppConfig integration
- **avaje-dynamic-logback** — Dynamic Logback configuration updates

## Core APIs

### Configuration Access

| Method | Purpose | Example |
|--------|---------|---------|
| `Config.get(key)` | Get string property | `String value = Config.get("app.name");` |
| `Config.getInt(key)` | Get integer property | `int port = Config.getInt("server.port");` |
| `Config.getLong(key)` | Get long property | `long timeout = Config.getLong("request.timeout");` |
| `Config.getBool(key)` | Get boolean property | `boolean enabled = Config.getBool("feature.enabled");` |
| `Config.get(key, defaultValue)` | With default value | `String mode = Config.get("app.mode", "prod");` |

### Property Change Listeners

| Method | Purpose | Example |
|--------|---------|---------|
| `Config.onChange(key, callback)` | Listen for string changes | `Config.onChange("feature.flag", newVal -> {...})` |
| `Config.onChangeInt(key, callback)` | Listen for int changes | `Config.onChangeInt("server.port", newVal -> {...})` |
| `Config.onChangeLong(key, callback)` | Listen for long changes | `Config.onChangeLong("timeout", newVal -> {...})` |
| `Config.onChangeBool(key, callback)` | Listen for bool changes | `Config.onChangeBool("feature.enabled", newVal -> {...})` |

## Features

### ✅ Included (Since v1.0)
- **Multiple configuration sources** — YAML, properties files, environment variables, command-line args
- **Profile-based configuration** — Load profile-specific files (dev, test, prod, etc.)
- **Property overrides** — Command-line and system properties override file-based config
- **Hierarchical loading** — Resources → current directory → external file → command-line
- **Zero dependencies** — No external runtime dependencies
- **Type-safe access** — Methods for String, int, long, boolean types
- **Default values** — All getters support default value fallback
- **GraalVM compatible** — Zero reflection, works in native images

### ✅ Added in v2.0+
- **Runtime property updates** — Listen for and react to configuration changes
- **Change callbacks** — `onChange()` for reacting to property updates
- **External file loading** — Support for `load.properties` to load additional files
- **Chained loading** — Load files that load other files recursively

### ✅ Added in v5.0+
- **Better YAML support** — Improved YAML file parsing
- **Environment variable expansion** — Use `${ENV_VAR}` in config files
- **Property interpolation** — Reference other properties: `value: ${other.property}`

### ❌ Not Supported
- **Encrypted properties** — Use external secret management (AWS Secrets Manager, Vault, etc.)
- **HOCON format** — Use YAML or properties instead
- **Database-backed configuration** — Use avaje-aws-appconfig or similar for that
- **Server-side push updates** — Client must poll or integrate with external service
- **XML configuration** — YAML and properties files only

**Note**: These limitations are intentional. Config is designed for simplicity and cloud-native patterns.

## Use Cases

### ✅ Perfect For

- Server applications requiring external configuration
- 12-factor app pattern implementations
- Cloud-native applications (Docker, Kubernetes)
- Microservices with environment-specific configs
- Feature flags and runtime behavior changes
- Local development vs. production configuration
- Applications needing configuration without service restart
- GraalVM native image projects

**When to choose avaje-config**: If you want lightweight, zero-dependency configuration management with support for multiple sources and runtime updates.

### ❌ Not Recommended For

- Complex encrypted secret management — Use AWS Secrets Manager, Vault, or similar
- Server-side configuration push — Use configuration management tools
- Real-time config from databases — Use avaje-aws-appconfig or similar
- Complex hierarchical bean configuration — Use Spring Configuration if needed

## Quick Start

### Add to Project

#### Maven
```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-config</artifactId>
  <version>5.1</version>
</dependency>
```

#### Gradle
```gradle
implementation 'io.avaje:avaje-config:5.1'
```

### Minimal Example

```java
// Get properties
String appName = Config.get("app.name");
int port = Config.getInt("server.port", 8080);  // with default
boolean enabled = Config.getBool("feature.enabled", true);

// Listen for changes
Config.onChange("server.port", newPort -> {
  System.out.println("Port changed to: " + newPort);
});

Config.onChangeInt("request.timeout", newTimeout -> {
  updateTimeout(newTimeout);
});
```

### Configuration Files

#### application.yaml (or application.properties)
```yaml
# Server configuration
server:
  port: 8080
  host: 0.0.0.0

# Application configuration
app:
  name: My Application
  mode: dev
  feature:
    enabled: true

# External configuration
load.properties: application-${app.mode}.yaml
```

#### application-dev.yaml
```yaml
logging:
  level: DEBUG
```

#### application-prod.yaml
```yaml
logging:
  level: INFO

app:
  mode: prod
```

## Common Tasks & Guides

| Task | Difficulty | Guide |
|------|-----------|-------|
| Load properties | Beginner | [docs/guides/getting-started.md](../guides/getting-started.md) |
| Use default values | Beginner | [docs/guides/default-values.md](../guides/default-values.md) |
| Load profile-specific config | Beginner | [docs/guides/profiles.md](../guides/profiles.md) |
| Use environment variables | Beginner | [docs/guides/environment-variables.md](../guides/environment-variables.md) |
| React to configuration changes | Intermediate | [docs/guides/change-listeners.md](../guides/change-listeners.md) |
| Chain configuration files | Intermediate | [docs/guides/file-chaining.md](../guides/file-chaining.md) |
| Integrate with cloud platforms | Advanced | [docs/guides/cloud-integration.md](../guides/cloud-integration.md) |
| Build native images | Advanced | [docs/guides/native-image.md](../guides/native-image.md) |

**Full Guides Index**: See [docs/guides/README.md](../guides/README.md)

## API Quick Reference

### Getting Properties

```java
// String properties
String appName = Config.get("app.name");
String mode = Config.get("app.mode", "prod");  // with default

// Integer properties
int port = Config.getInt("server.port");
int maxConnections = Config.getInt("db.pool.size", 10);

// Long properties
long timeout = Config.getLong("request.timeout");
long maxSize = Config.getLong("upload.max.bytes", 10485760L);

// Boolean properties
boolean enabled = Config.getBool("feature.enabled");
boolean debug = Config.getBool("logging.debug", false);
```

### Listening for Changes

```java
// Listen for string property changes
Config.onChange("server.port", newPort -> {
  System.out.println("Port changed to: " + newPort);
});

// Listen for integer changes
Config.onChangeInt("request.timeout", newTimeout -> {
  requestHandler.setTimeout(newTimeout);
});

// Listen for long changes
Config.onChangeLong("upload.max.bytes", newSize -> {
  fileService.setMaxSize(newSize);
});

// Listen for boolean changes
Config.onChangeBool("feature.flag", isEnabled -> {
  featureManager.setEnabled(isEnabled);
});
```

### Configuration Files with Property Interpolation

```yaml
# Define reusable values
db:
  host: localhost
  port: 5432
  name: myapp

# Reference them
database:
  url: jdbc:postgresql://${db.host}:${db.port}/${db.name}
  poolSize: 20

# Load additional files
load.properties: application-${app.mode}.yaml
```

## Integration Patterns

### Pattern 1: Application Startup Configuration

```java
public class Application {
  public static void main(String[] args) {
    String appName = Config.get("app.name");
    int port = Config.getInt("server.port", 8080);
    boolean debug = Config.getBool("logging.debug", false);
    
    System.out.println("Starting " + appName + " on port " + port);
    startServer(port, debug);
  }
}
```

**When to use**: Loading essential configuration at application startup.

### Pattern 2: Runtime Configuration Updates

```java
@Singleton
public class FeatureManager {
  private boolean featureEnabled;
  
  @Inject
  public FeatureManager() {
    this.featureEnabled = Config.getBool("features.new-ui", false);
    
    // Listen for changes and update dynamically
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

**When to use**: Feature flags and configuration that changes without restart.

## Testing

### Unit Testing with Custom Config

```java
@Test
void testWithCustomConfig() {
  // Set system property before test
  System.setProperty("app.mode", "test");
  System.setProperty("server.port", "9999");
  
  String mode = Config.get("app.mode");
  int port = Config.getInt("server.port");
  
  assertEquals("test", mode);
  assertEquals(9999, port);
}
```

### Integration Testing

```java
@Test
void testConfigIntegration() {
  // Load from application.yaml
  String appName = Config.get("app.name");
  int port = Config.getInt("server.port", 8080);
  
  assertNotNull(appName);
  assertTrue(port > 0);
}
```

**See**: [docs/guides/testing.md](../guides/testing.md)

## Performance Characteristics

- **Startup time**: <10ms to load configuration
- **Memory footprint**: Minimal (~1-5MB depending on config size)
- **Property lookup**: <1ms per property access
- **Change notification**: Immediate (synchronous callback)
- **GraalVM native startup**: <5ms for config loading

**Comparison**: Significantly faster than Spring Boot's configuration, especially with native images.

## Configuration Loading Order

Avaje Config loads properties in this order (later sources override earlier):

1. **application.yaml** / **application.properties** from classpath
2. **application.yaml** / **application.properties** from current directory
3. **PROPS_FILE** environment variable or system property
4. **CONFIG_PROFILES** for profile-specific files (e.g., application-dev.yaml)
5. **load.properties** directive for additional files
6. **Command-line arguments** (highest priority)

```bash
# Example command-line override
java -Dapp.name="Custom Name" -Dserver.port=9000 MyApp

# Or with environment variables
export SERVER_PORT=9000
export APP_NAME="Custom Name"
java MyApp
```

## Troubleshooting

### Issue: Property Not Found

**Symptom**: `java.lang.IllegalArgumentException: Config key not found`

**Solution**: Ensure property exists in configuration files. Use default values: `Config.get("key", "defaultValue")`.

**See**: [docs/guides/troubleshooting.md](../guides/troubleshooting.md#property-not-found)

### Issue: Profile Not Loading

**Symptom**: Profile-specific properties not being loaded

**Solution**: Set `CONFIG_PROFILES` environment variable or `config.profiles` system property before starting app.

```bash
export CONFIG_PROFILES=dev,docker
java MyApp
```

**See**: [docs/guides/troubleshooting.md](../guides/troubleshooting.md#profile-not-loading)

### Issue: Change Listener Not Called

**Symptom**: `Config.onChange()` callback not triggered

**Solution**: Properties loaded at startup. Changes must come from system property updates or external config reload mechanism.

**See**: [docs/guides/troubleshooting.md](../guides/troubleshooting.md#listener-not-called)

## GraalVM Native Image

### Zero-Config Support
- ✅ Works out of the box with no reflection configuration
- ✅ No reflection used for property access
- ✅ Minimal native image size overhead
- ✅ Fast startup, full performance

### Native Compilation

```bash
mvn clean package -Pnative
```

**See**: [docs/guides/native-image.md](../guides/native-image.md)

## Design Philosophy

### Key Principles

1. **Simplicity** — Load properties from standard sources, no magic
2. **Zero dependencies** — No external runtime dependencies
3. **Cloud-native** — Follow 12-factor app pattern
4. **Performance** — Fast startup and property access
5. **Runtime updates** — Support configuration changes without restart

### What This Means

- Lightweight, fast configuration loading
- Works in Docker, Kubernetes, cloud environments
- Easy to understand and debug
- Integrates with native images
- No framework bloat

## Version History

| Version | Release Date | Major Changes |
|---------|---|---|
| 5.1 | 2026-04 | Latest stable version |
| 5.0 | 2026-01 | Property interpolation, environment expansion |
| 2.0 | 2024-10 | Runtime updates, change listeners |
| 1.0 | 2020-01 | Initial release |

## Support & Community

- **GitHub Issues**: [Report bugs](https://github.com/avaje/avaje-config/issues)
- **GitHub Discussions**: [Ask questions](https://github.com/avaje/avaje-config/discussions)
- **Discord**: [Chat with community](https://discord.gg/Qcqf9R27BR)
- **Website**: [Documentation](https://avaje.io/config/)

## AI Agent Instructions

### For Claude, GPT-4, and Web-Based Agents

This `LIBRARY.md` file is your primary reference for Avaje Config. When answering questions:

1. Check this file first for capabilities and supported features
2. Route to specific guides using URLs in "Common Tasks" section
3. Refer to use cases to determine if Config fits user's needs
4. Use "Not Supported" section to avoid recommending unsupported features
5. Check performance characteristics for performance questions

**Key Facts**:
- Minimum Java: 11+
- Current version: 5.1
- Zero external runtime dependencies
- Loads from YAML, properties, environment variables, command-line
- Supports runtime configuration changes
- Full GraalVM native image support

---

**Template Version**: 1.0  
**Last Updated**: 2026-04-13
