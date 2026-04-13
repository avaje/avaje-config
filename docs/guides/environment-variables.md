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
| server.port | SERVER_PORT |
| database.host | DATABASE_HOST |
| app.name | APP_NAME |

The convention is: `UPPERCASE_WITH_UNDERSCORES`

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
