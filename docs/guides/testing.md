# Testing with Avaje Config

How to test applications that use avaje-config.

## Test Configuration Files

Create test-specific configuration in `src/test/resources`:

```
src/test/resources/
├── application.yaml          # Test defaults
├── application-test.yaml     # Profile-specific
└── application-it.yaml       # Integration test config
```

**application-test.yaml**:
```yaml
server:
  port: 0  # Use random port

database:
  host: localhost
  port: 5432

cache:
  enabled: false
```

## Test Resource Auto-Loading

avaje-config automatically loads `src/test/resources/application-test.yaml` (or
`.properties`) when it is present on the classpath. This happens unconditionally —
no profile activation (`config.profiles`, `avaje.profiles`, etc.) is needed.

The loading order during tests is:

1. `src/main/resources/application.yaml` — production defaults
2. `src/test/resources/application-test.yaml` — test overrides (loaded automatically)
3. System properties / command-line arguments — highest priority

> **Note:** `application-test.yaml` is a special hardcoded filename. It is not the
> same as activating a `test` profile. Other profile files (e.g., `application-it.yaml`)
> still require explicit profile activation via `config.profiles=it`.

```java
@Test
public void testConfiguration() {
  // application-test.yaml values are available automatically
  String dbHost = Config.get("database.host");
  assertEquals("localhost", dbHost);
}
```

## Overriding Configuration in Tests

Override specific properties:

```java
@Test
public void testWithCustomPort() {
  System.setProperty("server.port", "9000");
  try {
    int port = Config.getInt("server.port");
    assertEquals(9000, port);
  } finally {
    System.clearProperty("server.port");
  }
}
```

## JUnit 5 Extension

Create a custom extension for configuration:

```java
public class ConfigExtension implements BeforeEachCallback {
  private Map<String, String> originalProperties;
  
  @Override
  public void beforeEach(ExtensionContext context) {
    originalProperties = new HashMap<>();
    
    // Save original values
    originalProperties.put("server.port", System.getProperty("server.port"));
  }
  
  public void setProperty(String key, String value) {
    System.setProperty(key, value);
  }
  
  public void reset() {
    // Restore original values
    originalProperties.forEach((key, value) -> {
      if (value != null) {
        System.setProperty(key, value);
      } else {
        System.clearProperty(key);
      }
    });
  }
}
```

Use in tests:

```java
@ExtendWith(ConfigExtension.class)
public class MyTest {
  @Test
  public void test(ConfigExtension config) {
    config.setProperty("server.port", "9000");
    
    int port = Config.getInt("server.port");
    assertEquals(9000, port);
  }
}
```

## Integration Testing

For integration tests with external services:

**application-it.yaml**:
```yaml
server:
  port: 8080

database:
  host: localhost
  port: 5432
  name: test_db

redis:
  host: localhost
  port: 6379
```

Use Docker Compose or Testcontainers:

```java
public class IntegrationTest {
  @ClassRule
  public static DockerComposeContainer<?> environment =
    new DockerComposeContainer<>(new File("docker-compose.it.yml"))
      .withExposedService("postgres", 5432)
      .withExposedService("redis", 6379);
  
  @Test
  public void testWithRealServices() {
    String dbHost = Config.get("database.host");
    // Test with real database and redis
  }
}
```

## Testing Configuration Changes

Test configuration change listeners using `Config.onChange()`:

```java
@Test
public void testConfigChangeListener() {
  List<String> changedKeys = new ArrayList<>();

  Config.onChange(event -> {
    changedKeys.addAll(event.modifiedKeys());
  }, "server.port");

  System.setProperty("server.port", "9000");

  // Trigger reload of all configuration sources
  Config.asConfiguration().reloadSources();

  assertTrue(changedKeys.contains("server.port"));
}
```

For single-property typed listeners:

```java
Config.onChangeInt("server.port", newPort -> {
  System.out.println("Port changed to: " + newPort);
});
```

## Best Practices

| Practice | Reason |
|----------|--------|
| Use separate test config file | Prevents test pollution |
| Reset properties after tests | Clean state for next test |
| Use random ports | Allows parallel test execution |
| Mock external services | Faster, more reliable tests |
| Test both success and failure cases | Comprehensive coverage |

## Next Steps

- Learn about [environment variables](environment-variables.md) in tests
- See [troubleshooting](troubleshooting.md) for test issues
