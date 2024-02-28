# avaje-aws-appconfig

This is a avaje-config ConfigurationSource that uses
[AWS AppConfig](https://docs.aws.amazon.com/appconfig/latest/userguide/what-is-appconfig.html)
as a source for configuration.

The AWS AppConfig content can either be in `yaml` or `properties`
format.

This plugin will load configuration from the AWS AppConfig agent on initialisation
and then additionally by polling the AppConfig agent (for changes to the config)
and also when `Configuration.refresh()` is called.

## Configuration

- aws.appconfig.enabled - defaults to `true`
- aws.appconfig.application - required
- aws.appconfig.environment - required
- aws.appconfig.configuration - defaults to `"default"`
- aws.appconfig.pollingEnabled - defaults to `true`
- aws.appconfig.pollingSeconds - defaults to `45` seconds
- aws.appconfig.refreshSeconds - defaults to `(pollingSeconds - 1)`


## Steps to use

### Add dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-aws-appconfig</artifactId>
  <version>...</version>
</dependency>
```

### Add to src/main/application.yaml

```yaml
aws.appconfig:
  application: my-application
  environment: ${ENVIRONMENT:dev}
  configuration: default
```

Or with more parameters like:

```yaml
aws.appconfig:
  enabled: true
  application: my-application
  environment: ${ENVIRONMENT:dev}
  configuration: default
  pollingEnabled: true
  pollingSeconds: 60
```


### Add to src/test/application-test.yml

Turn it off for testing. When running tests we generally don't wish to
pull configuration from AWS AppConfig.

```yaml
aws.appconfig.enabled: false
```
