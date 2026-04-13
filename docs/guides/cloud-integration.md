# Cloud Integration

How to integrate avaje-config with cloud configuration services.

## AWS AppConfig

Integrate with AWS Systems Manager Parameter Store:

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-aws-appconfig</artifactId>
  <version>2.5</version>
</dependency>
```

Configure in `application.yaml`:

```yaml
avaje:
  config:
    appconfig:
      enabled: true
      application: myapp
      environment: prod
      profile: default
```

This automatically loads configuration from AWS AppConfig.

## Spring Cloud Config

For Spring Cloud Config compatibility:

```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888
      name: myapp
      profile: prod
```

Avaje Config can read from Spring Cloud Config servers.

## Environment Variables (Recommended for Cloud-Native)

The simplest cloud-native approach is using environment variables:

```yaml
database:
  host: ${DATABASE_HOST}
  port: ${DATABASE_PORT}
  username: ${DATABASE_USER}
  password: ${DATABASE_PASSWORD}
```

Set these in your deployment:

- Docker Compose
- Kubernetes ConfigMaps and Secrets
- CI/CD platform secrets
- Cloud provider parameter stores

## Docker Compose Example

```yaml
version: '3'
services:
  app:
    image: myapp:latest
    environment:
      - DATABASE_HOST=postgres
      - DATABASE_PORT=5432
      - DATABASE_USER=myuser
      - DATABASE_PASSWORD=mypassword
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:14
    environment:
      - POSTGRES_USER=myuser
      - POSTGRES_PASSWORD=mypassword
      - POSTGRES_DB=myapp

  redis:
    image: redis:7
```

## Kubernetes Example

Create a ConfigMap for non-sensitive data:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: myapp-config
data:
  application.yaml: |
    server:
      port: 8080
    app:
      name: MyApp
```

Create a Secret for sensitive data:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: myapp-secrets
type: Opaque
stringData:
  DATABASE_PASSWORD: secretpassword
  API_KEY: secretkey
```

Mount in your Pod:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp
spec:
  containers:
  - name: app
    image: myapp:latest
    envFrom:
    - configMapRef:
        name: myapp-config
    - secretRef:
        name: myapp-secrets
```

## Multi-Tenancy Configuration

For multi-tenant applications, scope configuration by tenant:

```yaml
tenants:
  acme:
    database:
      host: acme-db.example.com
      port: 5432
  widgetcorp:
    database:
      host: widgetcorp-db.example.com
      port: 5432
```

Access in code:

```java
String tenant = getTenantFromRequest();
String host = Config.get("tenants." + tenant + ".database.host");
```

## Configuration Precedence for Cloud

When using cloud configuration services:

1. Command-line system properties (highest)
2. Environment variables
3. Cloud service (AppConfig, Spring Cloud Config)
4. Local application.yaml
5. Application defaults (lowest)

This allows local development while respecting cloud-provided values in production.

## Next Steps

- Set up [change listeners](change-listeners.md) to react to cloud config updates
- See [troubleshooting](troubleshooting.md) for integration issues
