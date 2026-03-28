# GraalVM Native Image

Avaje config has built-in support for GraalVM native image. No additional dependencies
or configuration files should be required.

## How it works

The jar ships a `META-INF/native-image/` resource configuration that tells the native
image compiler to include the standard configuration file patterns as resources:

- `application.properties` / `application-<profile>.properties`
- `application.yaml` / `application-<profile>.yaml`
- `application.yml` / `application-<profile>.yml`

This means avaje-config can locate and load your configuration files at runtime inside
a native executable without any additional setup.

Extensions loaded via `ServiceLoader` (`ConfigExtension`, `ConfigParser`,
`ConfigurationSource`, `ConfigurationPlugin`, etc.) also work in native image provided
the extension jars include their own native-image metadata (or you register them
manually).


## Profiles

Profiles work as normal in native image. Activate a profile at runtime by passing the
`avaje.profiles` system property to the native executable:

```shell
./target/my-app -Davaje.profiles=prod
```


## Custom resource file names

If you load configuration from non-standard file names (e.g. `my-service.yaml`), you
must register those as resources in your own native-image configuration so the compiler
includes them:

```
src/main/resources/META-INF/native-image/<groupId>/<artifactId>/resource-config.json
```

```json
{
  "resources": [
    { "pattern": "my-service.*yaml" }
  ]
}
```

The standard `application.*` patterns are already covered by avaje-config itself.

