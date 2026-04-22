# Using avaje-config with Spring Boot

How to integrate avaje-config (and avaje-aws-appconfig) into a Spring Boot 3 application.

## How avaje-config Relates to Spring Boot

avaje-config initialises as a standalone library, **independently of the Spring
application context**. This means:

- Spring's `@Value` and `Environment` read from Spring's own property sources
- They do **not** automatically see avaje-config or AWS AppConfig values
- For any property sourced from avaje-config (especially AppConfig dynamic values),
  use `Config.get()` directly rather than `@Value`

```java
// ✅ Correct — reads from avaje-config (including AWS AppConfig values)
var url = Config.get("db.url");

// ⚠️ Only works for values in Spring's Environment (application.yaml loaded by Spring)
@Value("${db.url}")
private String url;
```

> avaje-config can be bridged into Spring's property sources if needed, but the
> simpler approach is to use `Config.get()` wherever you need dynamic config values.

## Step 1: Add Dependencies

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-config</artifactId>
  <version>5.1</version>
</dependency>
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-aws-appconfig</artifactId>
  <version>1.6</version>
</dependency>
```

> **Important:** `avaje-config` is declared `provided` in `avaje-aws-appconfig`'s POM.
> You must add it explicitly as it will not be pulled in transitively.

## Step 2: Configuration Files

avaje-config loads `application.yaml` from the classpath independently of Spring Boot.
Both libraries load the same file, but from their own parsing context.

In the following example, we use `${ENV_VAR:default}` for env-injected values:

```yaml
db:
  url: ${DB_URL:jdbc:postgresql://localhost:5432/mydb}
  user: ${DB_USER:myuser}
  pass: ${DB_PASS:}

aws.appconfig:
  enabled: true
  application: ${ENVIRONMENT_NAME:dev}-my-service
  environment: ${ENVIRONMENT_NAME:dev}
  configuration: default
```

**`src/test/resources/application-test.yaml`** — disable AppConfig in tests:

```yaml
aws.appconfig:
  enabled: false
```

> `application-test.yaml` in test resources is auto-loaded without any profile
> activation. No `-Davaje.profiles=test` or `-Dconfig.profiles=test` is needed.

## Step 3: Database Bean with `@ConditionalOnMissingBean`

Spring Boot 3 disables bean override by default. When a `TestConfiguration` provides
a test database bean (e.g. using Testcontainers), the production `DatabaseConfig`
must not also try to register a `Database` bean.

Use `@ConditionalOnMissingBean` so the production bean is skipped when a test
override is already registered:

```java
@Configuration
class DatabaseConfig {

  @Bean
  @ConditionalOnMissingBean(Database.class)
  Database database() {
    var url = Config.get("db.url");          // reads from avaje-config
    var user = Config.get("db.user", "myuser");
    var pass = Config.get("db.pass", "");

    var dataSource = DataSourceBuilder.create()
        .url(url)
        .username(user)
        .password(pass)
        .build();

    return Database.builder()
        .dataSourceBuilder(dataSource)
        .build();
  }
}
```

The test configuration (imported via `@Import`) is processed first, so
`@ConditionalOnMissingBean` causes the production bean to be skipped.

## Step 4: Test Configuration

```java
@TestConfiguration
class TestDatabaseConfig {

  @Bean
  Database database() {
    // Testcontainers PostgreSQL — registered before DatabaseConfig is processed
    var postgres = new PostgreSQLContainer<>("postgres:16");
    postgres.start();

    var dataSource = DataSourceBuilder.create()
        .url(postgres.getJdbcUrl())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .build();

    return Database.builder()
        .dataSourceBuilder(dataSource)
        .build();
  }
}
```

In each test class:

```java
@SpringBootTest
@Import(TestDatabaseConfig.class)
class MyControllerTest {
  // ...
}
```

## Step 5: Dynamic Configuration with `Config.onChange()`

To react to AWS AppConfig updates at runtime, register listeners at application startup:

```java
@Component
class FeatureFlags {

  private volatile boolean newUiEnabled;

  @PostConstruct
  void init() {
    this.newUiEnabled = Config.getBool("features.new-ui", false);

    Config.onChangeBool("features.new-ui", enabled -> {
      this.newUiEnabled = enabled;
    });
  }

  public boolean isNewUiEnabled() {
    return newUiEnabled;
  }
}
```

## Using avaje-simple-logger (optional)

If you want JSON structured logging with avaje-simple-logger instead of Logback:

**`pom.xml`:**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions>
    <exclusion>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
    </exclusion>
  </exclusions>
</dependency>
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-simple-logger</artifactId>
  <version>2.0-RC1</version>
</dependency>
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-jdk-platform-logging</artifactId>
  <version>2.0.17</version>
</dependency>
```

Exclude only `logback-classic` (not the entire `spring-boot-starter-logging`), so
`jul-to-slf4j` and other Spring Boot logging bridges remain in place. Spring Boot
auto-detects and falls back to `JavaLoggingSystem` when Logback is absent.

**`src/main/resources/avaje-logger.properties`:**
```properties
root.level=INFO
```

**`src/test/resources/avaje-logger-test.properties`:**
```properties
root.level=WARN
io.avaje.level=DEBUG
```

## Summary

| Concern | Pattern |
|---|---|
| Read static config | `Config.get("key")` |
| Read config with default | `Config.get("key", "default")` |
| Dynamic feature flag | `Config.onChangeBool("key", callback)` |
| Test database override | `@ConditionalOnMissingBean` + `@Import(TestDatabaseConfig.class)` |
| Disable AppConfig in tests | `aws.appconfig.enabled: false` in `application-test.yaml` |
| `@Value` with AppConfig values | Not supported by default — use `Config.get()` |
