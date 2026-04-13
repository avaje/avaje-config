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

## Using Test Configuration

Tests automatically use `src/test/resources/application.yaml`:

```java
@Test
public void testConfiguration() {
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

## Mocking Configuration

For advanced testing, mock the Config class:

```java
import static org.mockito.Mockito.*;

@Test
public void testWithMockedConfig() {
  // Create spy on real Config
  Config spy = spy(Config.class);
  
  when(spy.get("server.port")).thenReturn("9000");
  
  int port = Integer.parseInt(spy.get("server.port"));
  assertEquals(9000, port);
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

Test configuration change listeners:

```java
@Test
public void testConfigChangeListener() {
  List<String> changes = new ArrayList<>();
  
  Config.addChangeListener(event -> {
    changes.add(event.getProperty());
  });
  
  System.setProperty("server.port", "9000");
  
  // Trigger configuration reload
  Config.reload();
  
  assertTrue(changes.contains("server.port"));
}
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
