# Using Environment Variables

How to configure your application using environment variables.

## Basic Usage

Reference environment variables in configuration files:

```yaml
server:
  port: ${SERVER_PORT:8080}

database:
  host: ${DATABASE_HOST:localhost}
  port: ${DATABASE_PORT:5432}
  user: ${DATABASE_USER}
  password: ${DATABASE_PASSWORD}
```

Format: `${ENV_VAR_NAME:default_value}`

If the environment variable is not set, uses the default value (or error if no default).

## Setting Environment Variables

**Linux/Mac**:
```bash
export SERVER_PORT=9000
export DATABASE_HOST=prod-db.example.com
java myapp.jar
```

**Docker**:
```dockerfile
ENV SERVER_PORT=9000
ENV DATABASE_HOST=prod-db.example.com
CMD ["java", "-jar", "myapp.jar"]
```

Or with `docker run`:
```bash
docker run -e SERVER_PORT=9000 -e DATABASE_HOST=prod-db.example.com myapp:latest
```

**Kubernetes**:
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp
spec:
  containers:
  - name: myapp
    image: myapp:latest
    env:
    - name: SERVER_PORT
      value: "9000"
    - name: DATABASE_HOST
      value: "prod-db.example.com"
```

## Convention: ENV_VAR_NAME

Environment variable names follow this convention:

| YAML Property | Environment Variable |
|---------------|----------------------|
| `server.port` | `SERVER_PORT` |
| `database.host` | `DATABASE_HOST` |
| `app.name` | `APP_NAME` |
| `my.foo-bar` | `MY_FOOBAR` |

The translation rule is: **dots → underscores, hyphens removed, uppercase**.

> **Hyphen note:** hyphens are dropped entirely, not converted to underscores.
> `my.foo-bar` → `MY_FOOBAR`, not `MY_FOO_BAR`.
> This differs from Spring Boot's relaxed binding where hyphens become underscores.
> To avoid ambiguity, prefer dot-only property names (e.g., `database.maxPoolSize`).

## Two Mechanisms: Explicit vs Automatic

avaje-config supports two distinct ways to use environment variables.

### 1. Explicit `${ENV_VAR:default}` in YAML (expression evaluation)

Use the `${VAR:default}` syntax inside YAML values. The expression is evaluated at
load time using the exact variable name you specify:

```yaml
database:
  host: ${DATABASE_HOST:localhost}
  port: ${DATABASE_PORT:5432}
  password: ${DATABASE_PASSWORD}      # No default. An env var is required
```

This form:
- Uses the **exact env var name** you specify
- Supports **default values** after the colon
- Supports **compound values** where the env var is part of a larger string:

```yaml
aws.appconfig:
  application: ${ENVIRONMENT_NAME:dev}-my-service   # e.g. "prod-my-service"
```

### 2. Automatic property override (no YAML changes needed)

For **every** property key avaje-config knows about, it automatically checks whether
a matching environment variable exists (using the `toEnvKey` translation rule above).
If one is set, it overrides the file-based value. No `${...}` in the YAML is needed.

```yaml
# application.yaml - no ${...} needed
database:
  host: localhost
  port: 5432
```

```bash
# This env var automatically overrides database.host → DATABASE_HOST
export DATABASE_HOST=prod-db.example.com
java myapp.jar
```

```java
// Still returns "prod-db.example.com" from env var
String host = Config.get("database.host");
```

The automatic check also acts as a **fallback for missing keys**: if a key is not
found in any configuration file, avaje-config will check the translated env var name.

## Accessing in Code

Read environment variables directly:

```java
import io.avaje.config.Config;

String dbHost = Config.get("database.host"); // Uses ${DATABASE_HOST}
int port = Config.getInt("server.port");     // Uses ${SERVER_PORT}
```

Or access the environment variable directly:

```java
String dbHost = System.getenv("DATABASE_HOST");
```

## Priority Order

Configuration values are resolved in this order (highest to lowest priority):

1. System properties: `java -Dserver.port=9000`
2. Environment variables: `export SERVER_PORT=9000`
3. Configuration file values: `application.yaml`
4. Defaults in code

## Secrets Management

For sensitive values (passwords, API keys), use environment variables:

```yaml
database:
  password: ${DATABASE_PASSWORD}

api:
  key: ${API_KEY}
  secret: ${API_SECRET}
```

**Never** commit these values to version control. Use:

- CI/CD secrets (GitHub Actions, Jenkins, GitLab CI)
- Secret management services (AWS Secrets Manager, HashiCorp Vault)
- .env files (development only, in .gitignore)

## Next Steps

- Use [profiles](profiles.md) for environment-specific configuration
- Set up [change listeners](change-listeners.md) to react to configuration changes
