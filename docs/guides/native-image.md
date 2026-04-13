# Building GraalVM Native Images

avaje-config supports GraalVM native image compilation. 

We don't need to do anything extra. 


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

## Next Steps

- Use [profiles](profiles.md) with environment variables for multi-environment native images
- See [troubleshooting](troubleshooting.md#native-image) for common issues
