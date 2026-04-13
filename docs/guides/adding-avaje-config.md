# Adding avaje-config to Your Project

This guide provides step-by-step instructions for integrating **avaje-config** into a Java project using Maven. It covers basic setup and common usage patterns for retrieving configuration properties.

## What is avaje-config?

avaje-config is a lightweight configuration library that loads and manages application properties from YAML and properties files. It provides:
- Automatic loading of configuration from standard locations
- Type-safe property access (String, int, long, boolean)
- Simple, fluent API for retrieving values

## Step 1: Add Maven Dependency

Add avaje-config to your `pom.xml`:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-config</artifactId>
  <version>5.1</version>
</dependency>
```

Replace `5.1` with the latest version from [Maven Central](https://mvnrepository.com/artifact/io.avaje/avaje-config).

## Step 2: Create Configuration Files

### 2a. Main Application Configuration

Create `src/main/resources/application.yaml` for production configuration with reasonable defaults:

```yaml
# Application configuration
app:
  name: MyApplication
  version: 1.0.0
  port: 8080
  timeout-seconds: 30
  debug: false

# Database configuration
database:
  url: jdbc:postgresql://localhost:5432/myapp
  username: appuser
  max-pool-size: 10

# Feature flags
features:
  logging-enabled: true
```

**Alternative with properties format** - `src/main/resources/application.properties`:

```properties
app.name=MyApplication
app.version=1.0.0
app.port=8080
app.timeout-seconds=30
app.debug=false

database.url=jdbc:postgresql://localhost:5432/myapp
database.username=appuser
database.max-pool-size=10

features.logging-enabled=true
```

### 2b. Test Configuration

Create `src/test/resources/application-test.yaml` for test-specific configuration:

```yaml
# Override settings for testing
app:
  port: 8081
  debug: true

database:
  url: jdbc:h2:mem:test
  username: sa
  max-pool-size: 2

features:
  logging-enabled: false
```

**Alternative with properties format** - `src/test/resources/application-test.properties`:

```properties
app.port=8081
app.debug=true

database.url=jdbc:h2:mem:test
database.username=sa
database.max-pool-size=2

features.logging-enabled=false
```

## Step 3: Use Config API in Your Code

### Getting String Values

```java
import io.avaje.config.Config;

public class AppConfig {

  public static String getAppName() {
    // Get value, throws exception if not found
    return Config.get("app.name");
  }

  public static String getAppName(String defaultName) {
    // Get value with default if not found
    return Config.get("app.name", defaultName);
  }

  public static String getDatabaseUrl() {
    return Config.get("database.url", "jdbc:h2:mem:default");
  }
}
```

### Getting Integer Values

```java
public class AppConfig {

  public static int getPort() {
    // Get integer value
    return Config.getInt("app.port");
  }

  public static int getPort(int defaultPort) {
    // Get integer with default
    return Config.getInt("app.port", defaultPort);
  }

  public static int getMaxPoolSize() {
    return Config.getInt("database.max-pool-size", 5);
  }
}
```

### Getting Long Values

```java
public class AppConfig {

  public static long getTimeoutMillis() {
    // Get long value
    return Config.getLong("app.timeout-millis", 30000L);
  }
}
```

### Getting Boolean Values

```java
public class AppConfig {

  public static boolean isDebugEnabled() {
    // Get boolean value
    return Config.getBool("app.debug");
  }

  public static boolean isLoggingEnabled() {
    return Config.getBool("features.logging-enabled", true);
  }
}
```

## Step 4: Access Configuration in Your Application

### In a Main Application Class

```java
public class MyApplication {

  public static void main(String[] args) {
    String appName = Config.get("app.name", "MyApp");
    int port = Config.getInt("app.port", 8080);
    boolean debug = Config.getBool("app.debug", false);

    System.out.println("Starting " + appName + " on port " + port);
    if (debug) {
      System.out.println("Debug mode enabled");
    }
  }
}
```

### In a Service Class

```java
public class DatabaseService {

  private final String dbUrl;
  private final String dbUser;
  private final int maxPoolSize;

  public DatabaseService() {
    this.dbUrl = Config.get("database.url");
    this.dbUser = Config.get("database.username");
    this.maxPoolSize = Config.getInt("database.max-pool-size", 10);
  }

  public void connect() {
    System.out.println("Connecting to " + dbUrl);
    // Initialize connection pool with maxPoolSize
  }
}
```

## How Configuration is Loaded

When your application starts, avaje-config automatically loads properties in this order:

1. **Main resources** - `src/main/resources/application.yaml` or `.properties`
2. **Test resources** (when running tests) - `src/test/resources/application-test.yaml` or `.properties`
3. **Later sources override earlier ones** - Test configuration takes precedence over main configuration

This means:
- Define default values in `application.yaml` (main resources)
- Override specific values in `application-test.yaml` when running tests
- Your tests run with test-specific configuration automatically

## Common Patterns

### Configuration Wrapper Class

Create a configuration class to centralize all property access:

```java
public class Config {

  public static String appName() {
    return io.avaje.config.Config.get("app.name", "MyApplication");
  }

  public static int port() {
    return io.avaje.config.Config.getInt("app.port", 8080);
  }

  public static String databaseUrl() {
    return io.avaje.config.Config.get("database.url");
  }

  public static int maxPoolSize() {
    return io.avaje.config.Config.getInt("database.max-pool-size", 10);
  }
}

// Usage:
int port = Config.port();
String dbUrl = Config.databaseUrl();
```

### Constructor Injection Pattern

Use configuration to initialize services:

```java
public class DatabaseConnectionPool {

  private final String url;
  private final int maxSize;

  public DatabaseConnectionPool() {
    this.url = io.avaje.config.Config.get("database.url");
    this.maxSize = io.avaje.config.Config.getInt("database.max-pool-size", 10);
  }

  public void initialize() {
    // Set up connection pool
  }
}
```

## Testing with Configuration

When running tests, avaje-config automatically uses `application-test.yaml` or `application-test.properties`:

```java
@Test
public void testWithTestConfiguration() {
  // This test automatically uses application-test.yaml
  // Port will be 8081 (from test config)
  int port = Config.getInt("app.port");
  assertEquals(8081, port);
}
```

## Key Directories and Files

```
src/
├── main/
│   └── resources/
│       ├── application.yaml       # Main configuration (production defaults)
│       └── application.properties # Alternative format for main config
└── test/
    └── resources/
        ├── application-test.yaml       # Test configuration overrides
        └── application-test.properties # Alternative format for test config
```

## Next Steps

- Review the [avaje-config documentation](https://avaje.io/config/) for advanced features
- Consider creating a wrapper class for type-safe configuration access
- Use environment-specific values to handle different deployment environments
- Organize your configuration hierarchically (use dot notation like `app.port`, `database.url`)

## Troubleshooting

**Property not found exception:**
- Ensure the property exists in `application.yaml` or `application-test.yaml`
- Check property naming - use consistent dot notation (e.g., `app.port` not `app-port`)
- Verify the file is in the correct location (`src/main/resources` or `src/test/resources`)

**Test uses wrong configuration:**
- Ensure `application-test.yaml` or `application-test.properties` exists in `src/test/resources`
- Verify filename spelling exactly matches

**Values not updated:**
- Stop and restart your application
- Configuration is loaded once at startup
