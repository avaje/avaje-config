# Building GraalVM Native Images

How to use avaje-config with GraalVM native image compilation.

## Configuration

Add GraalVM Maven plugin to `pom.xml`:

```xml
<plugin>
  <groupId>org.graalvm.buildtools</groupId>
  <artifactId>native-maven-plugin</artifactId>
  <version>0.10.0</version>
  <configuration>
    <buildArgs>
      <buildArg>--enable-url-protocols=https</buildArg>
    </buildArgs>
  </configuration>
</plugin>
```

## Native Image Hints

Avaje Config provides native image metadata automatically via GraalVM tracing agent.

To generate hints:

```bash
# Build with instrumentation
mvn -DskipTests -Pnative-image clean package

# Or use the native-image agent
java -agentlib:native-image=config-output-dir=native-image-config -jar target/myapp.jar
```

## Static Initialization

For best performance, enable static initialization:

```java
import io.avaje.config.Config;

public class Application {
  static {
    // Configuration is loaded at build time
    Config.setLogLevel("INFO");
  }
}
```

## Property Resolution in Native Image

All configuration files must be on the classpath:

```
src/main/resources/
├── application.yaml          # Always included
├── application-prod.yaml     # Include via classpath
└── META-INF/native-image/
    └── reflect-config.json   # Reflection metadata
```

For dynamic profiles, use environment variables:

```yaml
logging:
  level: ${LOGGING_LEVEL:INFO}
```

Then set at runtime:

```bash
LOGGING_LEVEL=DEBUG ./myapp
```

## Classpath Resources

Ensure all configuration files are included in the native image:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <configuration>
    <transformers>
      <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
        <mainClass>com.example.Application</mainClass>
      </transformer>
    </transformers>
  </configuration>
</plugin>
```

## Building the Native Image

```bash
# Build the native executable
mvn -Pnative clean package

# Run the native image
./target/myapp
```

## Performance Benefits

Native images with avaje-config offer:

- **Startup time**: < 50ms (vs 2-5 seconds for JVM)
- **Memory**: 30-50MB resident (vs 200-500MB for JVM)
- **No warm-up**: Performance immediate, no JIT compilation
- **Smaller deployments**: Single executable with bundled config

## Limitations

- Configuration must be known at build time or provided via environment variables
- Dynamic class loading not supported
- Reflection on configuration classes must be hinted

## Testing Native Images Locally

Use GraalVM locally for development:

```bash
# Install GraalVM
sdk install java 21-graal

# Build native image locally
mvn -Pnative clean package

# Test
./target/myapp
```

## Troubleshooting

### "Configuration class not found"

Ensure all `@Config` classes are on the classpath:

```
mvn native:compile
```

### "Property not resolved at runtime"

Use environment variables for dynamic values:

```yaml
database:
  host: ${DATABASE_HOST:localhost}
```

## Next Steps

- Use [profiles](profiles.md) with environment variables for multi-environment native images
- See [troubleshooting](troubleshooting.md#native-image) for common issues
