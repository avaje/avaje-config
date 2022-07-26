module io.avaje.config {

  exports io.avaje.config;

  requires transitive io.avaje.applog;
  requires static org.yaml.snakeyaml;

  uses io.avaje.config.ConfigurationSource;
}
