module io.avaje.config {

  exports io.avaje.config;

  requires transitive io.avaje.lang;
  requires transitive io.avaje.applog;
  requires static org.yaml.snakeyaml;
  requires static io.avaje.inject;

  uses io.avaje.config.ConfigurationLog;
  uses io.avaje.config.ModificationEventRunner;
  uses io.avaje.config.ConfigurationSource;
  uses io.avaje.config.ResourceLoader;

  provides io.avaje.inject.spi.PropertyRequiresPlugin with io.avaje.config.InjectPropertiesPlugin;
}
