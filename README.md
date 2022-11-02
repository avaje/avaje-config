[![Build](https://github.com/avaje/avaje-config/actions/workflows/build.yml/badge.svg)](https://github.com/avaje/avaje-config/actions/workflows/build.yml)
[![Maven Central : avaje-config](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-config/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.avaje/avaje-config)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/avaje/avaje-config/blob/master/LICENSE)


# Application configuration
This library loads properties files that can be used to configure
an application including "testing" and "local development" and
dynamic configuration (changes to configuration properties at runtime).

```xml
<dependency>
  <groupId>io.avaje</groupId>
  <artifactId>avaje-config</artifactId>
  <version>${avaje.config.version}</version>
</dependency>
```

## Documentation

Go to [avaje.io/config](https://avaje.io/config/) for user documentation.

## Typical use

- Put application.yaml into src/main/resources for properties that have reasonable defaults
- Put application-test.yaml into src/test/resources for properties used when running tests
- Specify external properties via command line arguments. These effectively override application.yaml properties.


## Config use

Getting property values
```java

// get a String property
String value = Config.get("myapp.foo");

// with a default value
String value = Config.get("myapp.foo", "withDefaultValue");

// also int, long and boolean with and without default values
int intVal = Config.getInt("bar");
long longVal = Config.getLong("bar");
boolean booleanVal = Config.getBool("bar");

```
Register callback on property change.
```java

Config.onChange("myapp.foo", newValue -> {
  // do something ...
});

Config.onChangeInt("myapp.foo", newIntValue -> {
  // do something ...
});

Config.onChangeLong("myapp.foo", newLongValue -> {
  // do something ...
});

Config.onChangeBool("myapp.foo", newBooleanValue -> {
  // do something ...
});

```


## Loading properties

Config loads properties from expected locations as well as via command line arguments.
Below is the how it looks for configuration properties.

- loads from main resources (if they exist)
    - application.yaml
    - application.properties

- loads files from the current working directory (if they exist)
    - application.yaml
    - application.properties

- loads via system property `props.file` or environment variable `PROPS_FILE` (if defined)

- loads via `load.properties` property.

We can define a `load.properties` property which has name of property file in resource folder, or path locations for other properties/yaml files to load

Example application.properties:
```
common.property=value
load.properties=application-extra.properties,path/to/prop/application-extra2.properties
```

- loads test resources (if they exist, nb: Test resources are only visible when running tests)
    - application-test.properties
    - application-test.yaml


If no test resources were loaded then it additionally loads from "local dev" and command line:

- loads from "local dev".

We can specify an `app.name` property and then put a properties/yaml file at: `${user.home}/.localdev/{appName}.yaml`
We do this to set/override properties when we want to run the application locally (aka main method)

- loads from command line arguments

Command line arguments starting with `-P` can specify properties/yaml files to load


When properties are loaded they are merged/overlayed.

