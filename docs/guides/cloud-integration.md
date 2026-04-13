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
aws.appconfig:
    enabled: true
    application: ${ENVIRONMENT_NAME:dev}-my-application
    environment: ${ENVIRONMENT_NAME:dev}
    configuration: default
```

This automatically loads configuration from AWS AppConfig.


Configure in `application-test.yaml`:

```yaml
aws.appconfig.enabled: false
```

To disable loading the AWS AppConfig when running tests.


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


## Next Steps

- Set up [change listeners](change-listeners.md) to react to cloud config updates
- See [troubleshooting](troubleshooting.md) for integration issues
