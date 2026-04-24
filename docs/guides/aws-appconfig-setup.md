# Adding AWS AppConfig Support to avaje-config

This guide provides step-by-step instructions for integrating **AWS AppConfig** with avaje-config to enable dynamic configuration management. AWS AppConfig allows you to update application configuration without redeploying your service.

## What is AWS AppConfig?

[AWS AppConfig](https://docs.aws.amazon.com/appconfig/latest/userguide/what-is-appconfig.html) is an AWS service that helps you quickly and safely deploy application configurations across your infrastructure. With avaje-config's AWS AppConfig plugin, you can:

- Load configuration from AWS AppConfig on startup
- Automatically poll for configuration changes at runtime
- Dynamically update application behavior without redeployment
- Use YAML or properties format for configuration
- Maintain local defaults while leveraging remote configuration

## Prerequisites

- **AWS AppConfig Setup**: You must have:
  - An AWS AppConfig application configured
  - An environment within the application
  - A configuration profile with your configuration data
  - AWS credentials available to your application (via IAM roles, environment variables, or configuration)

- **Existing avaje-config Setup**: Follow the [Adding avaje-config to Your Project](adding-avaje-config.md) guide first.

## Step 1: Add AWS AppConfig Dependency

Add both dependencies to your `pom.xml`:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-aws-appconfig</artifactId>
  <version>1.7</version>
</dependency>
```

## Step 2: Configure AWS AppConfig

### Basic Configuration (YAML)

Add AWS AppConfig settings to your `src/main/resources/application.yaml`:

```yaml
aws.appconfig:
  enabled: true
  application: my-application
  environment: ${ENVIRONMENT:dev}
  configuration: default
```

**Configuration Parameters:**

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `aws.appconfig.enabled` | No | `true` | Enable/disable AWS AppConfig plugin |
| `aws.appconfig.application` | Yes* | — | AWS AppConfig application name |
| `aws.appconfig.environment` | Yes* | — | AWS AppConfig environment name |
| `aws.appconfig.configuration` | No | `default` | Configuration profile name in AppConfig |
| `aws.appconfig.pollingEnabled` | No | `true` | Enable periodic polling for changes |
| `aws.appconfig.pollingSeconds` | No | `45` | Poll interval in seconds |
| `aws.appconfig.refreshSeconds` | No | `pollingSeconds - 1` | How long to cache between refreshes |

*Required only if `aws.appconfig.enabled` is `true`

### Advanced Configuration (YAML)

For more control, use:

```yaml
aws.appconfig:
  enabled: true
  application: my-application
  environment: ${ENVIRONMENT:dev}
  configuration: default
  pollingEnabled: true
  pollingSeconds: 60
  refreshSeconds: 59
```

### Configuration with Properties Format

Alternatively, use `src/main/resources/application.properties`:

```properties
aws.appconfig.enabled=true
aws.appconfig.application=my-application
aws.appconfig.environment=${ENVIRONMENT:dev}
aws.appconfig.configuration=default
aws.appconfig.pollingEnabled=true
aws.appconfig.pollingSeconds=45
```

### Using Environment Variables

Use the `${VAR:defaultValue}` syntax to reference environment variables:

```yaml
aws.appconfig:
  enabled: true
  application: my-application
  environment: ${ENVIRONMENT:dev}
  configuration: ${CONFIG_PROFILE:default}
  pollingSeconds: ${POLLING_INTERVAL:45}
```

## Step 3: Disable AWS AppConfig in Tests

Create `src/test/resources/application-test.yaml` to disable AWS AppConfig during testing:

```yaml
# Disable AWS AppConfig in test environment
aws.appconfig:
  enabled: false
```

**Why this is needed:** In Kubernetes the avaje-aws-appconfig plugin connects to an
[AWS AppConfig Agent](https://docs.aws.amazon.com/appconfig/latest/userguide/appconfig-retrieving-managed-configuration-agent.html)
sidecar container running on `localhost:2772`. This sidecar is not present in
test or CI environments, so without `enabled: false` the plugin will fail on
startup trying to connect.

Additionally, because AppConfig is an additional configuration source loaded
_after_ file-based resources, it would override your `application-test.yaml`
values if left enabled. (Defeating the purpose of test configuration)

### Alternative with Properties Format

`src/test/resources/application-test.properties`:

```properties
aws.appconfig.enabled=false
```

## Step 4: Access Configuration from AWS AppConfig

AWS AppConfig configuration is automatically loaded and merged with your local configuration. Access it the same way as any other avaje-config properties:

```java
import io.avaje.config.Config;

public class AppService {

  public void start() {
    // AWS AppConfig values are accessed like any other properties
    String featureName = Config.get("features.name");
    int maxRetries = Config.getInt("service.max-retries", 3);
    boolean featureEnabled = Config.getBool("features.enabled", false);
  }
}
```

## How AWS AppConfig Loading Works

When your application starts:

1. **Local configuration** loads from `application.yaml` (local defaults)
2. **AWS AppConfig** plugin loads configuration from AWS AppConfig service
3. **AWS AppConfig values override local values** (if present)
4. **Polling starts** (if enabled) to check for configuration changes every `pollingSeconds`
5. **Configuration refreshes** at runtime when changes are detected

### Configuration Loading Order

```
1. src/main/resources/application.yaml (defaults)
2. AWS AppConfig (remote configuration - overrides defaults)
3. src/test/resources/application-test.yaml (test overrides)
4. System properties / command line arguments (final overrides)
```

## Dynamic Configuration Updates

### Polling for Changes

By default, AWS AppConfig polls for configuration changes every 45 seconds:

```yaml
aws.appconfig:
  enabled: true
  application: my-application
  environment: production
  pollingEnabled: true           # Enable polling
  pollingSeconds: 45              # Check every 45 seconds
  refreshSeconds: 44              # Cache locally for 44 seconds
```

When changes are detected:
- Configuration values are updated
- Change listeners are notified (see next section)
- Your application can react to configuration changes

### Listening to Configuration Changes

Register listeners to react to configuration updates from AWS AppConfig:

```java
import io.avaje.config.Config;

public class FeatureManager {

  public FeatureManager() {
    // React to feature flag changes
    Config.onChange("features.enabled", newValue -> {
      System.out.println("Feature toggled to: " + newValue);
    });

    Config.onChangeInt("service.max-retries", newValue -> {
      System.out.println("Max retries changed to: " + newValue);
    });

    Config.onChangeBool("debug.enabled", newValue -> {
      System.out.println("Debug mode: " + newValue);
    });
  }
}
```

### Manual Configuration Refresh

Force an immediate configuration refresh:

```java
import io.avaje.config.Config;
import io.avaje.config.Configuration;

Configuration config = Config.asConfiguration();
config.refresh();  // Immediately fetch from AWS AppConfig
```

## AWS AppConfig Configuration Format

AWS AppConfig stores configuration in YAML or properties format. The plugin automatically detects the format.

### YAML Format in AWS AppConfig

```yaml
service:
  name: PaymentService
  max-retries: 3
  timeout-seconds: 30

features:
  new-checkout: true
  advanced-analytics: false

database:
  pool-size: 20
```

### Properties Format in AWS AppConfig

```properties
service.name=PaymentService
service.max-retries=3
service.timeout-seconds=30

features.new-checkout=true
features.advanced-analytics=false

database.pool-size=20
```

## Complete Example

### Full Application Setup

**pom.xml:**
```xml
<dependencies>
  <dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-config</artifactId>
    <version>5.1</version>
  </dependency>
  <dependency>
    <groupId>io.avaje</groupId>
    <artifactId>avaje-aws-appconfig</artifactId>
    <version>1.0</version>
  </dependency>
</dependencies>
```

**src/main/resources/application.yaml:**
```yaml
app:
  name: MyService
  version: 1.0

aws.appconfig:
  application: my-service-config
  environment: ${ENVIRONMENT:dev}
  configuration: default
  pollingSeconds: 45

features:
  new-ui: false
  analytics: true
```

**src/test/resources/application-test.yaml:**
```yaml
aws.appconfig:
  enabled: false
```

**src/main/java/MyApplication.java:**
```java
import io.avaje.config.Config;
import io.avaje.config.Configuration;

public class MyApplication {

  private static final Configuration config = Config.asConfiguration();

  public static void main(String[] args) {
    String appName = Config.get("app.name");
    int port = Config.getInt("app.port", 8080);

    System.out.println("Starting " + appName);

    // Register listeners for dynamic updates
    Config.onChangeBool("features.new-ui", enabled -> {
      System.out.println("New UI feature: " + enabled);
    });

    startApplication();
  }

  private static void startApplication() {
    boolean analyticsEnabled = Config.getBool("features.analytics", true);
    System.out.println("Analytics enabled: " + analyticsEnabled);
  }
}
```

## GraalVM Native Image Support

AWS AppConfig works in GraalVM native images. The avaje-config library includes native image metadata, so no additional configuration is required.

### Building a Native Image with AWS AppConfig

Build your native image normally:

```bash
mvn clean package -Pnative
```

The native image will:
- Include application configuration files automatically
- Load AWS AppConfig configuration at startup
- Poll for changes using the configured polling interval

### Setting Environment at Runtime

Pass the environment as a system property to the native executable:

```bash
./target/my-app -DENVIRONMENT=prod
```

## Common Patterns

### Feature Flags from AWS AppConfig

```java
public class FeatureFlags {

  public static boolean isNewCheckoutEnabled() {
    return Config.enabled("features.checkout.new", false);
  }

  public static boolean isAdvancedSearchEnabled() {
    return Config.enabled("features.search.advanced", false);
  }
}

// Usage in code
if (FeatureFlags.isNewCheckoutEnabled()) {
  useNewCheckoutFlow();
} else {
  useOldCheckoutFlow();
}
```

### Dynamic Service Configuration

```java
public class ServiceConfig {

  private final Configuration config;

  public ServiceConfig() {
    this.config = Config.asConfiguration();
  }

  public int getMaxPoolSize() {
    return config.getInt("service.pool-size", 10);
  }

  public int getTimeoutSeconds() {
    return config.getInt("service.timeout-seconds", 30);
  }

  public void watchMaxPoolSize(IntConsumer listener) {
    config.onChangeInt("service.pool-size", listener);
  }
}
```

### Configuration Wrapper with Change Tracking

```java
public class DynamicConfig {

  private volatile int maxRetries;

  public DynamicConfig() {
    this.maxRetries = Config.getInt("service.max-retries", 3);

    // Update when AWS AppConfig changes
    Config.onChangeInt("service.max-retries", newValue -> {
      this.maxRetries = newValue;
      onConfigChanged();
    });
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  private void onConfigChanged() {
    System.out.println("Configuration updated from AWS AppConfig");
  }
}
```

## Troubleshooting

### AWS Credentials Not Found

**Error:** `Unable to connect to AWS AppConfig - no AWS credentials available`

**Solutions:**
- Ensure IAM role is attached to EC2 instance or ECS task
- Set `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables
- Configure AWS credentials in `~/.aws/credentials` (development only)
- Use IAM roles in AWS (recommended for production)

### AWS AppConfig Disabled in Production

**Problem:** AWS AppConfig is disabled but should be active

**Solution:** Verify `aws.appconfig.enabled` is not set to `false` in production configuration. Check that test configuration overrides don't apply to production:

```yaml
# src/main/resources/application.yaml - Production
aws.appconfig:
  enabled: true
  application: my-app
  environment: prod

# src/test/resources/application-test.yaml - Tests only
aws.appconfig:
  enabled: false
```

### Configuration Not Updating

**Problem:** Changes in AWS AppConfig don't appear in the application

**Solutions:**
- Verify `aws.appconfig.pollingEnabled` is `true`
- Check `aws.appconfig.pollingSeconds` - the default is 45 seconds
- Ensure the configuration profile exists in AWS AppConfig
- Manually trigger refresh with `configuration.refresh()`
- Check application logs for AWS AppConfig errors

### Performance Issues with Frequent Polling

**Problem:** Polling interval is too short and causes performance issues

**Solution:** Increase polling interval based on your needs:

```yaml
aws.appconfig:
  pollingSeconds: 120  # Poll every 2 minutes instead of 45 seconds
  refreshSeconds: 119
```

## Integration with Existing Setup

If you already have avaje-config configured:

1. Add `avaje-aws-appconfig` dependency
2. Add `aws.appconfig` configuration to `application.yaml`
3. AWS AppConfig values automatically override local defaults
4. Access values using existing `Config` API
5. No other code changes needed

AWS AppConfig is transparent to the rest of your application - it acts as an additional configuration source that's loaded after local files and before command-line overrides.

## Next Steps

- Set up AWS AppConfig application and configuration profiles in AWS Console
- Configure feature flags and service settings in AppConfig
- Test dynamic updates by modifying configuration in AWS Console
- Monitor polling and configuration refresh in application logs
- Review [avaje-config documentation](https://avaje.io/config/) for advanced features
