# avaje-dynamic-logback

A plugin for avaje-config that dynamically changes the logging levels.


## Configuring logging levels

Logging levels are configured by using `log.level.` as prefix, for example:

This plugin registers to listen to configuration changes the logging
level for configuration changes that start with `log.level`

When configuration is first loaded and then whenever it is changed
the logging level is changed.


#### yaml
```yaml
log.level:
  com.example: INFO
  com.example.sample: TRACE
```

#### properties
```properties
log.level.com.example=INFO
log.level.com.example.sample=TRACE
```


## Steps to use

### Add dependency

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-dynamic-logback</artifactId>
  <version>...</version>
</dependency>
```
